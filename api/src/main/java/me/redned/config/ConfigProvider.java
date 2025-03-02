package me.redned.config;

/**
 * Represents a config provider.
 */
public interface ConfigProvider {

    /**
     * Gets the parser.
     *
     * @param <C> the type of the parser
     * @return the parser
     */
    <C> ConfigParser<C> parser();

    /**
     * Gets the serializer.
     *
     * @param <C> the type of the serializer
     * @return the serializer
     */
    <C> ConfigSerializer<C> serializer();

    /**
     * Called when an error occurs.
     *
     * @param message the message
     */
    void onError(String message);
}
