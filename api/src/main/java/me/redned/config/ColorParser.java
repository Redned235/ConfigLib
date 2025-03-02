package me.redned.config;

import java.awt.Color;

public final class ColorParser implements ConfigParser.Parser<Color> {

    ColorParser() {
    }

    @Override
    public Color parse(Object object) throws ParseException {
        if (!(object instanceof String value)) {
            throw new ParseException("Color must be a string!")
                    .context("Provided color", object == null ? "null" : object.toString())
                    .cause(ParseException.Cause.INVALID_TYPE)
                    .type(this.getClass())
                    .userError();
        }

        return deserializeSingular(value);
    }

    public static Color deserializeSingular(String contents) throws ParseException {
        if (contents.startsWith("#")) {
            return Color.decode(contents);
        } else if (contents.contains(",")) {
            String[] split = contents.split(",");
            if (split.length != 3) {
                throw new ParseException("Color must have 3 values!")
                        .context("Provided color", contents)
                        .context("Expected format", "r,g,b")
                        .cause(ParseException.Cause.INVALID_VALUE)
                        .userError();
            }

            return new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        } else {
            return Color.getColor(contents);
        }
    }
}
