package me.redned.config.spigot.node;

import me.redned.config.ConfigNode;
import org.bukkit.configuration.ConfigurationSection;

public class ValueConfigNode implements ConfigNode {
    private final ConfigurationSection section;
    private final String path;

    public ValueConfigNode(ConfigurationSection section, String path) {
        this.section = section;
        this.path = path;
    }

    @Override
    public ConfigNode getNode(String... path) {
        return new ValueConfigNode(this.section, String.join(".", path));
    }

    @Override
    public <T> T get(Class<T> type) throws IllegalArgumentException {
        Object value = this.section.get(this.path);
        if (value == null) {
            return null;
        }

        // Handle primitive numbers
        if (value instanceof Number number) {
            if (type == Byte.class || type == byte.class) {
                return type.cast(number.byteValue());
            } else if (type == Short.class || type == short.class) {
                return type.cast(number.shortValue());
            } else if (type == Integer.class || type == int.class) {
                return type.cast(number.intValue());
            } else if (type == Long.class || type == long.class) {
                return type.cast(number.longValue());
            } else if (type == Float.class || type == float.class) {
                return type.cast(number.floatValue());
            } else if (type == Double.class || type == double.class) {
                return type.cast(number.doubleValue());
            }
        }

        if (type.isInstance(value)) {
            return type.cast(value);
        }

        // If value is string, just return it
        if (type == String.class) {
            return type.cast(value.toString());
        }

        throw new IllegalArgumentException("Cannot convert " + value.getClass().getSimpleName() + " to " + type.getSimpleName());
    }

    @Override
    public void set(Object value) {
        this.section.set(this.path, value);
    }
}
