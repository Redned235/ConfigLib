package me.redned.config.context;

import me.redned.config.ConfigNode;
import me.redned.config.ConfigOption;
import me.redned.config.ParseException;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Represents a context provider.
 * <p>
 * A context provider is a complex provider which allows for the creation of
 * custom types from a config node. This is particularly useful when seeking
 * to implement more complex logic when parsing a config node (i.e. validation),
 * or when the type is not a primitive type.
 *
 * @param <T> the type of the context
 */
public interface ContextProvider<T> {

    /**
     * Provides an instance of the context.
     *
     * @param sourceFile the source file
     * @param option the option
     * @param type the type
     * @param node the node
     * @param name the name
     * @param scope the scope
     * @return the instance
     * @throws ParseException if an error occurs while providing the instance
     */
    T provideInstance(@Nullable Path sourceFile, ConfigOption option, Class<?> type, ConfigNode node, String name, @Nullable Object scope) throws ParseException;
}
