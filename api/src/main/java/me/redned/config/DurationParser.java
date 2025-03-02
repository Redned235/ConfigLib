package me.redned.config;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DurationParser implements ConfigParser.Parser<Duration> {
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([yYmMdDhHwWsS])");

    DurationParser() {
    }

    @Override
    public Duration parse(Object object) throws ParseException {
        if (object instanceof Number number) {
            return Duration.ofSeconds(number.longValue());
        }

        if (object instanceof String contents) {
            return deserializeSingular(contents);
        }

        throw new ParseException("Invalid Duration for object: " + object)
                .cause(ParseException.Cause.INVALID_TYPE)
                .type(this.getClass())
                .userError();
    }

    public static Duration deserializeSingular(String contents) throws ParseException {
        if (contents.isBlank()) {
            throw new ParseException("Duration value was not provided!")
                    .cause(ParseException.Cause.MISSING_VALUE)
                    .type(DurationParser.class)
                    .userError();
        }

        // Define a regular expression to match the duration format
        Matcher matcher = DURATION_PATTERN.matcher(contents);

        long totalSeconds = 0;
        while (matcher.find()) {
            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "y":
                    totalSeconds += amount * 365 * 24 * 60 * 60; // assuming 365 days in a year
                    break;
                case "M":
                    totalSeconds += amount * 30 * 24 * 60 * 60; // assuming 30 days in a month
                    break;
                case "w":
                    totalSeconds += amount * 7 * 24 * 60 * 60; // 7 days in a week
                    break;
                case "d":
                    totalSeconds += amount * 24 * 60 * 60; // 24 hours in a day
                    break;
                case "h":
                    totalSeconds += amount * 60 * 60; // 60 minutes in an hour
                    break;
                case "m":
                    totalSeconds += amount * 60; // 60 seconds in a minute
                    break;
                case "s":
                    totalSeconds += amount;
                    break;
                default:
                    throw new ParseException("Invalid unit: " + unit)
                            .cause(ParseException.Cause.INVALID_TYPE)
                            .type(DurationParser.class)
                            .userError();
            }
        }

        if (totalSeconds == 0) {
            throw new ParseException("Failed to parse Duration from value " + contents)
                    .cause(ParseException.Cause.INVALID_VALUE)
                    .type(DurationParser.class)
                    .userError();
        }

        return Duration.ofSeconds(totalSeconds);
    }
}
