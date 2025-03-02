package me.redned.config.spigot;

import me.redned.config.ConfigOption;
import me.redned.config.ConfigSerializer;
import me.redned.config.ParseException;
import me.redned.config.spigot.node.SectionConfigNode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpigotConfigSerializer extends ConfigSerializer<ConfigurationSection> {
    private static final SpigotConfigSerializer INSTANCE = new SpigotConfigSerializer();

    static {
        DefaultSpigotSerializers.register();
    }

    @Override
    public <T> void saveConfig(Path configPath, T instance) throws ParseException {
        try {
            if (Files.notExists(configPath)) {
                Files.createFile(configPath);
            }
        } catch (IOException e) {
            throw new ParseException("An error occurred when creating file for config!")
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .context("File location", configPath.toString())
                    .type(SpigotConfigSerializer.class);
        }

        try {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(Files.newBufferedReader(configPath));
            saveConfig(configuration, instance);

            configuration.save(configPath.toFile());
        } catch (IOException e) {
            throw new ParseException("An error occurred when processing file for config!")
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .context("File location", configPath.toString())
                    .type(SpigotConfigSerializer.class);
        }
    }

    @Override
    public <T> void saveConfig(ConfigurationSection configuration, T instance) throws ParseException {
        serializeFields(instance, configuration);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void serializeFields(Object instance, ConfigurationSection configuration) throws ParseException {
        for (Field field : ConfigUtil.getAllFields(instance.getClass())) {
            field.setAccessible(true);

            if (!field.isAnnotationPresent(ConfigOption.class)) {
                continue;
            }

            // Get the type from the configuration
            Class<?> type = field.getType();

            ConfigOption configOption = field.getDeclaredAnnotation(ConfigOption.class);
            String name = configOption.name();

            Serializer serializer = SERIALIZERS.get(type);
            if (serializer != null) {
                try {
                    serializer.serialize(name, new SectionConfigNode(configuration), field.get(instance));
                } catch (Exception e) {
                    throw new ParseException("Failed to serialize custom object field " + field.getName() + " in class " + instance.getClass().getName(), e);
                }
                continue;
            }

            // First, let's check if our parameter is a primitive
            if (type.isPrimitive()) {
                serializePrimitive(name, type, field, instance, configuration);
            } else if (type == String.class) {
                try {
                    configuration.set(name, field.get(instance));
                } catch (Exception e) {
                    throw new ParseException("Failed to serialize string field " + field.getName() + " in class " + instance.getClass().getName(), e);
                }
            } else if (type.isEnum()) {
                try {
                    Enum<?> enumValue = (Enum<?>) field.get(instance);
                    configuration.set(name, enumValue.name().toLowerCase(Locale.ROOT));
                } catch (Exception e) {
                    throw new ParseException("Failed to serialize enum field " + field.getName() + " in class " + instance.getClass().getName(), e);
                }
            } else {
                // No object, let's serialize for lists/maps now
                if (List.class.isAssignableFrom(type)) {
                    try {
                        List<?> list = (List<?>) field.get(instance);
                        if (list == null) {
                            continue;
                        }

                        List<ConfigurationSection> sections = toMemorySections(list);
                        if (sections.isEmpty() && !list.isEmpty()) {
                            // Sections are empty, but the list is not, so assume that
                            // the list is a list of primitives
                            configuration.set(name, list);
                        } else {
                            configuration.set(name, sections);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Failed to serialize list field " + field.getName() + " in class " + instance.getClass().getName(), e);
                    }
                } else if (Map.class.isAssignableFrom(type)) {
                    try {
                        Map<?, ?> map = (Map<?, ?>) field.get(instance);
                        if (map == null) {
                            continue;
                        }

                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            // We do not support non-primitive keys
                            if (!(entry.getKey() instanceof String) && !entry.getKey().getClass().isPrimitive()) {
                                throw new ParseException("Non-primitive keys are not supported!");
                            }

                            String key = entry.getKey().toString();
                            Object value = entry.getValue();

                            ConfigurationSection section = configuration.createSection(name + "." + key);
                            serializeFields(value, section);
                        }
                    } catch (Exception e) {
                        throw new ParseException("Failed to serialize map field " + field.getName() + " in class " + instance.getClass().getName(), e);
                    }
                } else {
                    // Assume that the object is a custom object
                    ConfigurationSection section = new MemoryConfiguration();
                    try {
                        Object object = field.get(instance);
                        if (object == null) {
                            continue;
                        }

                        serializeFields(object, section);
                        if (section.getKeys(false).isEmpty()) {
                            continue;
                        }

                        configuration.set(name, section);
                    } catch (Exception e) {
                        throw new ParseException("Failed to serialize object field " + field.getName() + " in class " + instance.getClass().getName(), e);
                    }
                }
            }
        }
    }

    private static void serializePrimitive(String name, Class<?> type, Field field, Object instance, ConfigurationSection configuration) throws ParseException {
        if (type == boolean.class) {
            try {
                configuration.set(name, field.getBoolean(instance));
            } catch (Exception e) {
                throw new ParseException("Failed to serialize field " + field.getName() + " in class " + instance.getClass().getName(), e);
            }
        } else if (type == int.class) {
            try {
                configuration.set(name, field.getInt(instance));
            } catch (Exception e) {
                throw new ParseException("Failed to serialize field " + field.getName() + " in class " + instance.getClass().getName(), e);
            }
        } else if (type == double.class) {
            try {
                configuration.set(name, field.getDouble(instance));
            } catch (Exception e) {
                throw new ParseException("Failed to serialize field " + field.getName() + " in class " + instance.getClass().getName(), e);
            }
        } else if (type == float.class) {
            try {
                configuration.set(name, field.getFloat(instance));
            } catch (Exception e) {
                throw new ParseException("Failed to serialize field " + field.getName() + " in class " + instance.getClass().getName(), e);
            }
        } else if (type == long.class) {
            try {
                configuration.set(name, field.getLong(instance));
            } catch (Exception e) {
                throw new ParseException("Failed to serialize field " + field.getName() + " in class " + instance.getClass().getName(), e);
            }
        } else if (type == short.class) {
            try {
                configuration.set(name, field.getShort(instance));
            } catch (Exception e) {
                throw new ParseException("Failed to serialize field " + field.getName() + " in class " + instance.getClass().getName(), e);
            }
        } else if (type == byte.class) {
            try {
                configuration.set(name, field.getByte(instance));
            } catch (Exception e) {
                throw new ParseException("Failed to serialize field " + field.getName() + " in class " + instance.getClass().getName(), e);
            }
        } else {
            throw new ParseException("Unknown primitive type " + type.getName());
        }
    }

    private static List<ConfigurationSection> toMemorySections(List<?> list) throws ParseException {
        List<ConfigurationSection> sections = new LinkedList<>();
        for (Object object : list) {
            if (object.getClass().isPrimitive() || object.getClass().isEnum() || object.getClass() == String.class) {
                continue;
            }

            ConfigurationSection section = new MemoryConfiguration();
            serializeFields(object, section);

            if (section.getKeys(false).isEmpty()) {
                continue;
            }

            sections.add(section);
        }

        return sections;
    }

    public static SpigotConfigSerializer get() {
        return INSTANCE;
    }
}
