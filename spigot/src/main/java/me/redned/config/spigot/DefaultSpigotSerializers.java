package me.redned.config.spigot;

import org.bukkit.Location;
import org.bukkit.UnsafeValues;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

final class DefaultSpigotSerializers {

    static void register() {
        SpigotConfigSerializer.get().registerSerializer(Location.class, (name, node, value) -> {
            node.getNode(name, "world").set(value.getWorld().getName());
            node.getNode(name, "x").set(value.getX());
            node.getNode(name, "y").set(value.getY());
            node.getNode(name, "z").set(value.getZ());
            node.getNode(name, "yaw").set(value.getYaw());
            node.getNode(name, "pitch").set(value.getPitch());
        });

        // Can only serialize items if the JSON method is available
        if (ConfigUtil.isMethodAvailable(UnsafeValues.class, "serializeItemAsJson", ItemStack.class)) {
            SpigotConfigSerializer.get().registerSerializer(ItemStack.class, new ItemStackSerializer());
        }

        SpigotConfigSerializer.get().registerSerializer(BlockData.class, (name, node, value) -> {
            node.getNode(name).set(value.getAsString());
        });
    }
}
