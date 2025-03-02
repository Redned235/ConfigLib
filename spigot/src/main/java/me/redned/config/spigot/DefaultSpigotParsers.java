package me.redned.config.spigot;

import me.redned.config.spigot.node.SectionConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

final class DefaultSpigotParsers {

    static void register() {
        SpigotConfigParser.get().registerProvider(ItemStack.class, new ItemStackParser());
        SpigotConfigParser.get().registerProvider(PotionEffect.class, new PotionEffectParser());
        SpigotConfigParser.get().registerProvider(Location.class, configValue -> {
            ConfigurationSection section = null;
            if (configValue instanceof ConfigurationSection locationSection) {
                section = locationSection;
            }

            if (configValue instanceof SectionConfigNode node) {
                section = node.getSection();
            }

            if (section == null) {
                return null;
            }

            World world = Bukkit.getWorld(section.getString("world"));
            if (world == null) {
                throw new IllegalArgumentException("World " + section.getString("world") + " does not exist");
            }

            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw");
            float pitch = (float) section.getDouble("pitch");
            return new Location(world, x, y, z, yaw, pitch);
        });

        SpigotConfigParser.get().registerProvider(BlockData.class, ConfigUtil.parseString(Bukkit::createBlockData));
    }
}
