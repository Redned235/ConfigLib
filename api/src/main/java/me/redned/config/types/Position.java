package me.redned.config.types;

import me.redned.config.ConfigOption;

/**
 * Represents a position.
 */
public class Position {
    @ConfigOption(name = "x", description = "The X position of the spawn.", required = true)
    private double x;
    @ConfigOption(name = "y", description = "The Y position of the spawn.", required = true)
    private double y;
    @ConfigOption(name = "z", description = "The Z position of the spawn.", required = true)
    private double z;

    public Position() {
    }

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public String toString() {
        return this.x + "," + this.y + "," + this.z;
    }

    public static Position fromString(String str) {
        String[] split = str.split(",");
        if (split.length != 3) {
            return null;
        }

        double x = Double.parseDouble(split[0]);
        double y = Double.parseDouble(split[1]);
        double z = Double.parseDouble(split[2]);
        return new Position(x, y, z);
    }
 }
