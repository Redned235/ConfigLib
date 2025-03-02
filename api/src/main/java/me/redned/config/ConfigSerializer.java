package me.redned.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a serializer for a config.
 *
 * @param <C> the configuration type
 */
public abstract class ConfigSerializer<C> {
    protected static final Map<Class<?>, Serializer<?>> SERIALIZERS = new HashMap<>();

    static {
        DefaultSerializers.register();
    }

    /**
     * Saves a config to disk from an instance.
     *
     * @param configPath the configuration path
     * @param instance the instance
     * @param <T> the type
     * @throws ParseException if an error occurs while saving the config
     */
    public abstract <T> void saveConfig(Path configPath, T instance) throws ParseException;

    /**
     * Saves a config from an instance.
     *
     * @param configuration the configuration
     * @param instance the instance
     * @param <T> the type
     * @throws ParseException if an error occurs while saving the config
     */
    public abstract <T> void saveConfig(C configuration, T instance) throws ParseException;

    /**
     * Registers a serializer.
     *
     * @param type the type
     * @param serializer the serializer
     * @param <T> the type
     */
    public <T> void registerSerializer(Class<T> type, Serializer<T> serializer) {
        registerSerializerInternal(type, serializer);
    }

    static <T> void registerSerializerInternal(Class<T> type, Serializer<T> serializer) {
        SERIALIZERS.put(type, serializer);
    }

    /**
     * A serializer for a value.
     *
     * @param <T> the type
     */
    public interface Serializer<T> {

        /**
         * Serializes a value.
         *
         * @param name the name
         * @param node the node
         * @param value the value
         */
        void serialize(String name, ConfigNode node, T value) throws ParseException;
    }
}
