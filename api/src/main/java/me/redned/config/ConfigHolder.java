package me.redned.config;

import java.util.Map;

/**
 * A holder for a set of config sections.
 * <p>
 * This is typically implemented by classes in which
 * there are unparsed config sections, typically accessed
 * by third parties, that need to be stored and accessed
 * in a less verbose manner.
 */
public interface ConfigHolder {

    /**
     * Gets the config sections stored in this holder.
     *
     * @return the config sections stored in this holder
     */
    Map<String, ConfigNode> getConfig();
}
