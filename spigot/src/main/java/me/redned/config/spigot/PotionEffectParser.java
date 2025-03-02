package me.redned.config.spigot;

import me.redned.config.ConfigParser;
import me.redned.config.ParseException;
import me.redned.config.SingularValueParser;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

final class PotionEffectParser implements ConfigParser.Parser<PotionEffect> {

    @Override
    public PotionEffect parse(Object object) throws ParseException {
        if (object instanceof String contents) {
            return deserializeSingular(contents);
        }

        throw new ParseException("Invalid PotionEffect for object: " + object)
                .cause(ParseException.Cause.INVALID_TYPE)
                .type(PotionEffectParser.class)
                .userError();
    }

    public static PotionEffect deserializeSingular(String contents) throws ParseException {
        SingularValueParser.ArgumentBuffer buffer = SingularValueParser.parseNamed(contents, SingularValueParser.BraceStyle.CURLY, ';');
        if (!buffer.hasNext()) {
            throw new ParseException("No data found for PotionEffect")
                    .cause(ParseException.Cause.INVALID_TYPE)
                    .type(PotionEffectParser.class)
                    .userError();
        }

        PotionEffectType type;
        int duration = 20;
        int amplifier = 1;
        boolean ambient = true;
        boolean particles = true;
        
        SingularValueParser.Argument root = buffer.pop();
        if (root.key().equals("root")) {
            type = PotionEffectType.getByKey(NamespacedKey.fromString(root.value()));
            if (type == null) {
                throw new ParseException("Invalid potion effect type " + root.value())
                        .cause(ParseException.Cause.INVALID_VALUE)
                        .type(PotionEffectParser.class)
                        .userError();
            }
        } else {
            throw new ParseException("Invalid PotionEffect root tag " + root.key())
                    .cause(ParseException.Cause.INTERNAL_ERROR)
                    .type(PotionEffectParser.class);
        }

        while (buffer.hasNext()) {
            SingularValueParser.Argument effectArgument = buffer.pop();
            switch (effectArgument.key()) {
                case "duration" -> duration = Integer.parseInt(effectArgument.value()) * 20;
                case "amplifier" -> amplifier = Integer.parseInt(effectArgument.value()) - 1;
                case "ambient" -> ambient = Boolean.parseBoolean(effectArgument.value());
                case "particles" -> particles = Boolean.parseBoolean(effectArgument.value());
            }
        }
        
        return new PotionEffect(type, duration, amplifier, ambient, particles);
    }
}
