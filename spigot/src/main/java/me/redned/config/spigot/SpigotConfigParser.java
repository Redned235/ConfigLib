package me.redned.config.spigot;

import me.redned.config.ConfigHolder;
import me.redned.config.ConfigOption;
import me.redned.config.ConfigParser;
import me.redned.config.Id;
import me.redned.config.ParseException;
import me.redned.config.PostProcessable;
import me.redned.config.Scoped;
import me.redned.config.spigot.node.SectionConfigNode;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpigotConfigParser extends ConfigParser<ConfigurationSection> {
    private static final SpigotConfigParser INSTANCE = new SpigotConfigParser();

    static {
        DefaultSpigotParsers.register();
    }

    @Override
    public <T> T loadConfig(Path dataPath, Class<T> type) throws ParseException {
        try {
            Configuration config = YamlConfiguration.loadConfiguration(Files.newBufferedReader(dataPath));
            return newInstance(dataPath, type, config);
        } catch (IOException e) {
            throw new ParseException("Failed to load configuration file " + dataPath, e)
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .sourceFile(dataPath);
        }
    }

    @Override
    public <T> T loadConfig(Path dataPath, Class<T> type, @Nullable Object scope, @Nullable Object id) throws ParseException {
        try {
            Configuration config = YamlConfiguration.loadConfiguration(Files.newBufferedReader(dataPath));
            return newInstance(dataPath, type, config, scope, id);
        } catch (IOException e) {
            throw new ParseException("Failed to load configuration file " + dataPath, e)
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .sourceFile(dataPath);
        }
    }

    @Override
    public <T> T newInstance(@Nullable Path sourceFile, Class<T> type, ConfigurationSection configuration) throws ParseException {
        return newInstance(sourceFile, type, configuration, null);
    }

    @Override
    public <T> T newInstance(@Nullable Path sourceFile, Class<T> type, ConfigurationSection configuration, @Nullable Object scope) throws ParseException {
        return newInstance(sourceFile, type, configuration, scope, null);
    }

    @Override
    public <T> T newInstance(Class<T> type, ConfigurationSection configuration, @Nullable Object scope, @Nullable Object id) throws ParseException {
        return newInstance(null, type, configuration, scope, id);
    }

    @Override
    public <T> T newInstance(@Nullable Path sourceFile, Class<T> type, ConfigurationSection configuration, @Nullable Object scope, @Nullable Object id) throws ParseException {
        T instance = newClassInstance(sourceFile, type);

        try {
            populateFields(sourceFile, instance, configuration, scope, id);
            if (instance instanceof PostProcessable postProcessable) {
                postProcessable.postProcess();
            }

            return instance;
        } catch (ParseException e) {
            throw e;
        } catch (Throwable t) {
            throw new ParseException("Failed to post-process instance of class " + type.getName(), t)
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .type(type)
                    .sourceFile(sourceFile);
        }
    }

    private void populateFields(@Nullable Path sourceFile, Object instance, ConfigurationSection configuration, @Nullable Object scope, @Nullable Object id) throws ParseException {
        List<String> sectionsParsed = new ArrayList<>();
        for (Field field : ConfigUtil.getAllFields(instance.getClass())) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Scoped.class)) {
                if (scope == null) {
                    throw new ParseException("Scope annotation found on field " + field.getName() + " in class " + instance.getClass().getName() + " but no scope was provided.")
                            .cause(ParseException.Cause.MISSING_VALUE)
                            .sourceFile(sourceFile);
                }

                try {
                    field.set(instance, scope);
                } catch (IllegalAccessException e) {
                    throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                            .cause(ParseException.Cause.INVALID_VALUE)
                            .sourceFile(sourceFile);
                }

                continue;
            }

            if (field.isAnnotationPresent(Id.class)) {
                if (id == null) {
                    // Check if we are in a nested node and if so, use the last part of the path
                    // as the id
                    String path = configuration.getCurrentPath();
                    if (path != null && !path.isEmpty()) {
                        if (path.contains(".")) {
                            path = path.substring(path.lastIndexOf('.') + 1);
                        }

                        id = path;
                    }
                }

                if (id == null) {
                    throw new ParseException("Id annotation found on field " + field.getName() + " in class " + instance.getClass().getName() + " but no id was provided (scope: " + scope + ")")
                            .cause(ParseException.Cause.MISSING_VALUE)
                            .sourceFile(sourceFile);
                }

                try {
                    field.set(instance, id);
                } catch (IllegalAccessException e) {
                    throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName() + " (scope: " + scope + ")", e)
                            .cause(ParseException.Cause.INVALID_VALUE)
                            .sourceFile(sourceFile);
                }

                continue;
            }

            if (!field.isAnnotationPresent(ConfigOption.class)) {
                continue;
            }

            ConfigOption configOption = field.getDeclaredAnnotation(ConfigOption.class);
            String name = configOption.name();
            boolean required = configOption.required();

            // Ensure we have an instance of this parameter
            if (required && !configuration.contains(name)) {
                String error = configuration.getName().isBlank() ? "configuration file" : "configuration section " + configuration.getName();
                throw new ParseException("Required option " + name + " not found in " + error)
                        .cause(ParseException.Cause.MISSING_VALUE)
                        .context("Option name", configOption.name())
                        .context("Option description", configOption.description())
                        .type(field.getType())
                        .userError()
                        .sourceFile(sourceFile);
            }

            // Get the type from the configuration
            populateType(sourceFile, field, configOption, instance, configuration, scope);

            sectionsParsed.add(name);
        }

        // Check if there are any sections that were not parsed
        if (instance instanceof ConfigHolder holder) {
            for (String key : configuration.getKeys(false)) {
                if (!sectionsParsed.contains(key) && configuration.isConfigurationSection(key)) {
                    holder.getConfig().put(key, new SectionConfigNode(configuration.getConfigurationSection(key)));
                }
            }
        }
    }

    private void populateType(@Nullable Path sourceFile, Field field, ConfigOption configOption, Object instance, ConfigurationSection configuration, @Nullable Object scope) throws ParseException {
        Class<?> type = field.getType();
        String name = configOption.name();

        // Check if we have a context provider for this field
        if (CONTEXT_PROVIDERS.containsKey(configOption.contextProvider())) {
            try {
                field.set(instance, CONTEXT_PROVIDERS.get(configOption.contextProvider())
                        .provideInstance(sourceFile, configOption, type, new SectionConfigNode(configuration), name, scope));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_FORMAT)
                        .context("Option name", configOption.name())
                        .context("Option description", configOption.description())
                        .sourceFile(sourceFile);
            }

            return;
        }

        // First, let's check if our parameter is a primitive
        if (type.isPrimitive()) {
            populatePrimitive(sourceFile, name, configOption.required(), type, field, instance, configuration);
        } else if (type == String.class) {
            try {
                field.set(instance, configuration.getString(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set string field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Option name", configOption.name())
                        .context("Option description", configOption.description())
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else if (type.isEnum()) {
            try {
                if (configuration.contains(name)) {
                    field.set(instance, Enum.valueOf((Class<? extends Enum>) field.getGenericType(), configuration.getString(name).toUpperCase(Locale.ROOT)));
                }
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set enum field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_OPTION)
                        .context("Provided", configuration.getString(name))
                        .context("Valid options", String.join(", ", Arrays.stream(((Class<? extends Enum>) field.getGenericType()).getEnumConstants())
                                .map(e2 -> e2.name().toLowerCase(Locale.ROOT))
                                .toArray(String[]::new)
                        ))
                        .context("Option name", configOption.name())
                        .context("Option description", configOption.description())
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else {
            // Value is not a primitive, let's check to see if we have a provider for it
            if (OBJECT_PROVIDERS.containsKey(type) && configuration.contains(name)) {
                try {
                    field.set(instance, parseObject(type, configuration.get(name)));
                } catch (ParseException e) {
                    throw e.sourceFile(sourceFile);
                } catch (IllegalAccessException e) {
                    throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                            .cause(ParseException.Cause.INVALID_TYPE)
                            .context("Option name", configOption.name())
                            .context("Option description", configOption.description())
                            .sourceFile(sourceFile);
                }
            } else {
                // No object, let's parse for lists/maps now
                if (List.class.isAssignableFrom(type)) {
                    try {
                        List<Object> objectList = parseList(sourceFile, instance, name, configuration, field.getGenericType());
                        if (objectList == null) {
                            return;
                        }

                        try {
                            field.set(instance, objectList);
                        } catch (IllegalAccessException e) {
                            throw new ParseException("Failed to set list field " + field.getName() + " in class " + instance.getClass().getName(), e)
                                    .cause(ParseException.Cause.INVALID_TYPE)
                                    .context("Option name", configOption.name())
                                    .context("Option description", configOption.description())
                                    .userError()
                                    .sourceFile(sourceFile);
                        }
                    } catch (ParseException e) {
                        throw e.sourceFile(sourceFile);
                    }
                } else if (Map.class.isAssignableFrom(type)) {
                    try {
                        Map<String, Object> map = parseMap(sourceFile, instance, type, configuration, name, field.getGenericType());
                        if (map == null) {
                            return;
                        }

                        try {
                            field.set(instance, map);
                        } catch (IllegalAccessException e) {
                            throw new ParseException("Failed to set map field " + field.getName() + " in class " + instance.getClass().getName(), e)
                                    .cause(ParseException.Cause.INVALID_TYPE)
                                    .context("Option name", configOption.name())
                                    .context("Option description", configOption.description())
                                    .userError()
                                    .sourceFile(sourceFile);
                        }
                    } catch (ParseException e) {
                        throw e.sourceFile(sourceFile);
                    }
                } else {
                    // Unknown object! Let's try to parse it
                    ConfigurationSection configurationSection = configuration.getConfigurationSection(name);
                    if (configurationSection == null) {
                        if (configuration.get(name) instanceof Map<?, ?> map) {
                            configurationSection = ConfigUtil.toMemorySection((Map<String, Object>) map);
                        } else if (configuration.contains(name)) {
                            throw new ParseException("Invalid object " + name + " in configuration section " + configuration.getName())
                                    .cause(ParseException.Cause.INVALID_TYPE)
                                    .context("Option name", configOption.name())
                                    .context("Option description", configOption.description())
                                    .context("Configured value", !configuration.contains(name) ? "null" : configuration.get(name).toString())
                                    .context("Expected value", field.getType().getSimpleName())
                                    .type(type)
                                    .userError()
                                    .sourceFile(sourceFile);
                        } else {
                            return;
                        }
                    }

                    try {
                        field.set(instance, newInstance(sourceFile, type, configurationSection, instance));
                    } catch (IllegalAccessException e) {
                        throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                                .cause(ParseException.Cause.INVALID_TYPE)
                                .context("Option name", configOption.name())
                                .context("Option description", configOption.description())
                                .type(type)
                                .userError()
                                .sourceFile(sourceFile);
                    }
                }
            }
        }
    }

    private void populatePrimitive(@Nullable Path sourceFile, String name, boolean required, Class<?> type, Field field, Object instance, ConfigurationSection configuration) throws ParseException {
        if (!required && !configuration.contains(name)) {
            // Don't bother setting anything if the config does
            // not contain the field. Primitives may have default values
            // inside the Java code, so we don't want to override them.
            return;
        }

        if (type == boolean.class) {
            try {
                field.set(instance, configuration.getBoolean(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Provided", configuration.getString(name))
                        .context("Expected", "boolean")
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else if (type == int.class) {
            try {
                field.set(instance, configuration.getInt(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Provided", configuration.getString(name))
                        .context("Expected", "int")
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else if (type == double.class) {
            try {
                field.set(instance, configuration.getDouble(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Provided", configuration.getString(name))
                        .context("Expected", "double")
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else if (type == float.class) {
            try {
                field.set(instance, (float) configuration.getDouble(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Provided", configuration.getString(name))
                        .context("Expected", "float")
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else if (type == long.class) {
            try {
                field.set(instance, configuration.getLong(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Provided", configuration.getString(name))
                        .context("Expected", "long")
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else if (type == short.class) {
            try {
                field.set(instance, (short) configuration.getInt(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Provided", configuration.getString(name))
                        .context("Expected", "short")
                        .userError()
                        .sourceFile(sourceFile);
            }
        } else if (type == byte.class) {
            try {
                field.set(instance, (byte) configuration.getInt(name));
            } catch (IllegalAccessException e) {
                throw new ParseException("Failed to set field " + field.getName() + " in class " + instance.getClass().getName(), e)
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Provided", configuration.getString(name))
                        .context("Expected", "byte")
                        .userError()
                        .sourceFile(sourceFile);
            }
        }
    }

    @Nullable
    private List<Object> parseList(@Nullable Path sourceFile, Object instance, String name, ConfigurationSection configuration, Type genericType) throws ParseException {
        List<?> list = configuration.getList(name);
        if (list == null) {
            if (configuration.contains(name)) {
                throw new ParseException("Failed to parse list from configuration with key " + name + " in class " + instance.getClass().getSimpleName())
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Key", name)
                        .context("Value", !configuration.contains(name) ? "null" : configuration.get(name).toString())
                        .userError()
                        .type(instance.getClass())
                        .sourceFile(sourceFile);
            } else {
                return null;
            }
        }

        List<ConfigurationSection> sections = ConfigUtil.toMemorySections(list);
        if (sections.isEmpty() && !list.isEmpty()) {
            // Sections are empty, but the list is not, so let's go through
            // the list and check if we have any providers for the objects
            List<Object> objectList = new ArrayList<>(list.size());

            // Get the primitive type of the list
            Class<?> listType;
            Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
            if (types[0] instanceof ParameterizedType) {
                listType = (Class<?>) ((ParameterizedType) types[0]).getRawType();
            } else {
                listType = (Class<?>) types[0];
            }

            if (OBJECT_PROVIDERS.containsKey(listType)) {
                Parser<Object> objectProvider = OBJECT_PROVIDERS.get(listType);
                for (Object object : list) {
                    objectList.add(parseObject(objectProvider, object));
                }
            } else {
                // Assume it's a primitive
                objectList.addAll(list);
            }

            return objectList;
        } else {
            // Parse as object
            List<Object> objects = new ArrayList<>(sections.size());
            for (ConfigurationSection section : sections) {
                objects.add(newInstance(sourceFile, (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0], section, instance));
            }

            return objects;
        }
    }

    @Nullable
    private Map<String, Object> parseMap(@Nullable Path sourceFile, Object instance, Class<?> type, ConfigurationSection configuration, String name, Type genericType) throws ParseException {
        ConfigurationSection configurationSection = configuration.getConfigurationSection(name);
        if (configurationSection == null) {
            if (configuration.contains(name)) {
                throw new ParseException("Failed to parse map from configuration with key " + name + " in class " + instance.getClass().getSimpleName() + "! Expected configuration section")
                        .cause(ParseException.Cause.INVALID_TYPE)
                        .context("Key", name)
                        .context("Value", !configuration.contains(name) ? "null" : configuration.get(name).toString())
                        .type(instance.getClass())
                        .userError()
                        .sourceFile(sourceFile);
            } else {
                return null;
            }
        }

        Map<String, Object> map = new HashMap<>();
        for (String key : configurationSection.getKeys(false)) {
            ConfigurationSection section = null;
            if (configurationSection.isConfigurationSection(key)) {
                section = configurationSection.getConfigurationSection(key);
            }

            ParameterizedType mapType = (ParameterizedType) genericType;
            Type value = mapType.getActualTypeArguments()[1];
            Class<?> valueClass;
            if (value instanceof ParameterizedType parameterizedType) {
                valueClass = (Class<?>) parameterizedType.getRawType();
                section = configurationSection; // Use the parent section in this case
            } else {
                valueClass = (Class<?>) value;
            }

            Object configValue;
            // Check if the value is contained in our object deserializers
            if (section == null && OBJECT_PROVIDERS.containsKey(valueClass)) {
                map.put(key, parseObject(valueClass, configurationSection.get(key)));
            } else if ((configValue = configurationSection.get(key)) != null && valueClass.isAssignableFrom(configValue.getClass())) {
                map.put(key, configValue);
            } else {
                if (section == null) {
                    throw new ParseException("Failed to parse map field " + type.getSimpleName() + " in class " + instance.getClass().getName())
                            .cause(ParseException.Cause.INVALID_TYPE)
                            .context("Key", key)
                            .context("Value", !configurationSection.contains(key) ? "null" : configurationSection.get(key).toString())
                            .userError()
                            .sourceFile(sourceFile);
                }

                try {
                    if (List.class.isAssignableFrom(valueClass)) {
                        List<Object> list = parseList(sourceFile, instance, key, section, value);
                        if (list != null) {
                            map.put(key, list);
                            continue;
                        }
                    } else if (Map.class.isAssignableFrom(valueClass)) {
                        Map<String, Object> innerMap = parseMap(sourceFile, instance, valueClass, section, key, value);
                        if (innerMap != null) {
                            map.put(key, innerMap);
                            continue;
                        }
                    }

                    map.put(key, newInstance(sourceFile, valueClass, section, instance));
                } catch (ParseException e) {
                    throw e.sourceFile(sourceFile);
                }
            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private <T> T newClassInstance(@Nullable Path sourceFile, Class<T> clazz) throws ParseException {
        if (INSTANCE_SUPPLIERS.containsKey(clazz)) {
            return (T) INSTANCE_SUPPLIERS.get(clazz).get();
        }

        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new ParseException("Failed to instantiate class " + clazz.getName() + "! Did you forget a no-args constructor?", e)
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .sourceFile(sourceFile);
        }
    }

    private static Object parseObject(Class<?> type, Object value) throws ParseException {
        return parseObject(OBJECT_PROVIDERS.get(type), value);
    }

    private static Object parseObject(Parser<?> parser, Object value) throws ParseException {
        Object toParse = value;
        if (value instanceof ConfigurationSection section) {
            toParse = new SectionConfigNode(section);
        }

        return parser.parse(toParse);
    }

    public static SpigotConfigParser get() {
        return INSTANCE;
    }
}
