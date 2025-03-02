package me.redned.config.spigot;

import com.google.gson.JsonObject;
import me.redned.config.ConfigNode;
import me.redned.config.ConfigSerializer;
import me.redned.config.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

final class ItemStackSerializer implements ConfigSerializer.Serializer<ItemStack> {

    @Override
    public void serialize(String name, ConfigNode node, ItemStack value) throws ParseException {
        if (value.getType() == Material.AIR) {
            return; // Do not serialize empty ItemStack
        }

        try {
            JsonObject object = (JsonObject) UnsafeValues.class.getMethod("serializeItemAsJson", ItemStack.class).invoke(Bukkit.getUnsafe(), value);

            Map<?, ?> values = ConfigUtil.GSON.fromJson(object, Map.class);
            values.forEach((k, v) -> node.getNode(name, k.toString()).set(v));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ParseException("Failed to serialize item as json")
                    .context("Item", value.toString())
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .type(this.getClass());
        }
    }
}
