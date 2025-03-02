package me.redned.config.spigot;

import me.redned.config.ConfigParser;
import me.redned.config.ConfigProvider;
import me.redned.config.ConfigSerializer;
import org.bukkit.Bukkit;

public class SpigotConfigProvider implements ConfigProvider {

    @Override
    public <C> ConfigParser<C> parser() {
        return (ConfigParser<C>) SpigotConfigParser.get();
    }

    @Override
    public <C> ConfigSerializer<C> serializer() {
        return (ConfigSerializer<C>) SpigotConfigSerializer.get();
    }

    @Override
    public void onError(String message) {
        Bukkit.getLogger().severe(message);
    }
}
