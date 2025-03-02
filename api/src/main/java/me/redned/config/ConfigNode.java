package me.redned.config;

/**
 * Represents a config node.
 */
public interface ConfigNode {

    /**
     * Gets a node from the path.
     *
     * @param path the path
     * @return the node
     */
    ConfigNode getNode(String... path);

    /**
     * Gets this node as a value.
     *
     * @param type the type
     * @return the value
     * @throws IllegalArgumentException if the value cannot be converted to the specified type
     */
    <T> T get(Class<T> type) throws IllegalArgumentException;

    /**
     * Gets this node as a string.
     *
     * @return the string
     */
    default String getString() {
        return get(String.class);
    }

    /**
     * Sets a value at this path.
     *
     * @param value the value
     */
    void set(Object value);
}
