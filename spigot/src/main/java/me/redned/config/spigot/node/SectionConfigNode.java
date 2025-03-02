package me.redned.config.spigot.node;

import me.redned.config.ConfigNode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public class SectionConfigNode implements ConfigNode {
    private final ConfigurationSection section;

    public SectionConfigNode(ConfigurationSection section) {
        this.section = section;
    }

    @Override
    public ConfigNode getNode(String... path) {
        String bukkitPath = String.join(".", path);
        if (this.section.isConfigurationSection(bukkitPath)) {
            return new SectionConfigNode(this.section.getConfigurationSection(bukkitPath));
        } else {
            return new ValueConfigNode(this.section, bukkitPath);
        }
    }

    @Override
    public <T> T get(Class<T> type) throws IllegalArgumentException {
        if (Map.class.isAssignableFrom(type)) {
            return type.cast(this.section.getValues(false));
        } else if (ConfigurationSection.class.isAssignableFrom(type)) {
            return type.cast(this.section);
        } else {
            throw new IllegalArgumentException("Cannot convert ConfigurationSection to " + type.getSimpleName());
        }
    }

    @Override
    public void set(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                this.section.set(entry.getKey().toString(), entry.getValue());
            }
        } else if (value instanceof ConfigurationSection section) {
            for (String key : section.getKeys(false)) {
                this.section.set(key, section.get(key));
            }
        } else {
            throw new IllegalArgumentException("Cannot convert " + value.getClass().getSimpleName() + " to ConfigurationSection");
        }
    }

    public ConfigurationSection getSection() {
        return this.section;
    }
}
