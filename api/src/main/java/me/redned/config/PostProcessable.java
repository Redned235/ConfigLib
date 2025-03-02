package me.redned.config;

/**
 * An interface implemented over a class which requires post-processing.
 * <p>
 * This allows for more complex logic once the object has been loaded from
 * the configuration file.
 */
public interface PostProcessable {

    /**
     * Post-processes the object.
     */
    void postProcess();
}
