package me.redned.config.spigot;

import com.google.gson.JsonObject;
import me.redned.config.ConfigParser;
import me.redned.config.ParseException;
import me.redned.config.spigot.node.SectionConfigNode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

final class ItemStackParser implements ConfigParser.Parser<ItemStack> {

    @Override
    public ItemStack parse(Object object) throws ParseException {
        if (object instanceof String string) {
            return deserializeSingular(string);
        }

        if (object instanceof ConfigurationSection section) {
            return deserializeNode(section);
        }

        if (object instanceof SectionConfigNode node) {
            return deserializeNode(node.getSection());
        }

        throw new ParseException("Invalid ItemStack for object: " + object)
                .cause(ParseException.Cause.INVALID_TYPE)
                .type(this.getClass())
                .userError();
    }

    public static ItemStack deserializeSingular(String contents) throws ParseException {
        return Bukkit.getItemFactory().createItemStack(contents);
    }

    private static ItemStack deserializeNode(ConfigurationSection section) throws ParseException {
        if (!ConfigUtil.isMethodAvailable(UnsafeValues.class, "deserializeItemFromJson", JsonObject.class)) {
            return new ItemStack(Material.AIR);
        }

        Map<String, Object> values = section.getValues(false);
        JsonObject object = ConfigUtil.GSON.toJsonTree(values).getAsJsonObject();

        try {
            return (ItemStack) UnsafeValues.class.getMethod("deserializeItemFromJson", JsonObject.class).invoke(Bukkit.getUnsafe(), object);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ParseException("Failed to deserialize item from json", e)
                    .type(ItemStackParser.class)
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .userError();
        }
    }
}
