package me.redned.config.spigot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.google.gson.ToNumberPolicy;
import me.redned.config.ConfigParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class ConfigUtil {
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .registerTypeHierarchyAdapter(ConfigurationSection.class,
                    (JsonSerializer<ConfigurationSection>) (src, type, ctx) -> ctx.serialize(src.getValues(false)))
            .create();

    public static List<Field> getAllFields(Class<?> cls) {
        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }

        return allFields;
    }

    @SuppressWarnings("unchecked")
    public static List<ConfigurationSection> toMemorySections(List<?> list) {
        List<ConfigurationSection> sections = new LinkedList<>();
        for (Object object : list) {
            if (object instanceof Map<?, ?> map) {
                MemoryConfiguration memoryConfig = new MemoryConfiguration();
                memoryConfig.addDefaults((Map<String, Object>) map);

                sections.add(memoryConfig);
            }
        }

        return sections;
    }

    public static ConfigurationSection toMemorySection(Map<String, Object> map) {
        MemoryConfiguration memoryConfig = new MemoryConfiguration();
        memoryConfig.addDefaults(map);
        return memoryConfig;
    }

    public static <T> ConfigParser.Parser<T> parseString(Function<String, T> parser) {
        return configValue -> {
            if (!(configValue instanceof String value)) {
                return null;
            }

            return parser.apply(value);
        };
    }

    public static boolean isMethodAvailable(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            clazz.getDeclaredMethod(methodName, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
