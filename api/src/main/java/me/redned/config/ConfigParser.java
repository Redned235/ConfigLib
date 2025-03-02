package me.redned.config;

import me.redned.config.context.ContextProvider;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a parser for a config.
 *
 * @param <C> the configuration type
 */
public abstract class ConfigParser<C> {
    protected static final Map<Class<?>, ContextProvider<?>> CONTEXT_PROVIDERS = new HashMap<>();
    protected static final Map<Class<?>, Supplier<?>> INSTANCE_SUPPLIERS = new HashMap<>();
    protected static final Map<Class<?>, Parser<Object>> OBJECT_PROVIDERS = new HashMap<>();

    static {
        DefaultParsers.register();
    }

    /**
     * Loads a config from a path.
     *
     * @param dataPath the data path
     * @param type the type
     * @return the config
     * @param <T> the type
     * @throws ParseException if an error occurs while loading the config
     */
    public abstract <T> T loadConfig(Path dataPath, Class<T> type) throws ParseException;

    /**
     * Loads a config from a path.
     *
     * @param dataPath the data path
     * @param type the type
     * @param scope the scope
     * @return the config
     * @param <T> the type
     * @throws ParseException if an error occurs while loading the config
     */
    public abstract <T> T loadConfig(Path dataPath, Class<T> type, @Nullable Object scope, @Nullable Object id) throws ParseException;

    /**
     * Creates a new instance of the type from the configuration class.
     *
     * @param sourceFile the source file
     * @param type the type
     * @param configuration the configuration
     * @return the instance
     * @param <T> the type
     * @throws ParseException if an error occurs while creating the instance
     */
    public abstract <T> T newInstance(@Nullable Path sourceFile, Class<T> type, C configuration) throws ParseException;

    /**
     * Creates a new instance of the type from the configuration class.
     *
     * @param sourceFile the source file
     * @param type the type
     * @param configuration the configuration
     * @param scope the scope
     * @return the instance
     * @param <T> the type
     * @throws ParseException if an error occurs while creating the instance
     */
    public abstract <T> T newInstance(@Nullable Path sourceFile, Class<T> type, C configuration, @Nullable Object scope) throws ParseException;

    /**
     * Creates a new instance of the type from the configuration class.
     *
     * @param type the type
     * @param configuration the configuration
     * @param scope the scope
     * @param id the id
     * @return the instance
     * @param <T> the type
     * @throws ParseException if an error occurs while creating the instance
     */
    public abstract <T> T newInstance(Class<T> type, C configuration, @Nullable Object scope, @Nullable Object id) throws ParseException;

    /**
     * Creates a new instance of the type from the configuration class.
     *
     * @param sourceFile the source file
     * @param type the type
     * @param configuration the configuration
     * @param scope the scope
     * @param id the id
     * @return the instance
     * @param <T> the type
     * @throws ParseException if an error occurs while creating the instance
     */
    public abstract <T> T newInstance(@Nullable Path sourceFile, Class<T> type, C configuration, @Nullable Object scope, @Nullable Object id) throws ParseException;

    /**
     * Registers a factory for the type.
     *
     * @param clazz the class
     * @param supplier the supplier
     * @param <T> the type
     */
    public <T> void registerFactory(Class<T> clazz, Supplier<T> supplier) {
        INSTANCE_SUPPLIERS.put(clazz, supplier);
    }

    /**
     * Registers a provider for the type.
     *
     * @param clazz the class
     * @param provider the provider
     * @param <T> the type
     */
    public <T> void registerProvider(Class<T> clazz, Parser<T> provider) {
        registerProviderInternal(clazz, provider);
    }

    @SuppressWarnings("unchecked")
    static <T> void registerProviderInternal(Class<T> clazz, Parser<T> provider) {
        OBJECT_PROVIDERS.put(clazz, (Parser<Object>) provider);
    }

    /**
     * Registers a context provider.
     *
     * @param clazz the class
     * @param provider the provider
     * @param <T> the type
     */
    public <T extends ContextProvider<?>> void registerContextProvider(Class<T> clazz, T provider) {
        CONTEXT_PROVIDERS.put(clazz, provider);
    }

    /**
     * A parser for an object.
     *
     * @param <T> the type
     */
    public interface Parser<T> {

        /**
         * Parses an object.
         *
         * @param object the object
         * @return the parsed object
         * @throws ParseException if an error occurs while parsing the object
         */
        T parse(Object object) throws ParseException;
    }
}
