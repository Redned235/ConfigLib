package me.redned.config.types;

import me.redned.config.ConfigOption;

/**
 * Represents a position with a rotation.
 */
public class PositionWithRotation {
    @ConfigOption(name = "x", description = "The X position of the spawn.", required = true)
    private double x;
    @ConfigOption(name = "y", description = "The Y position of the spawn.", required = true)
    private double y;
    @ConfigOption(name = "z", description = "The Z position of the spawn.", required = true)
    private double z;
    @ConfigOption(name = "yaw", description = "The yaw of the spawn.")
    private float yaw;
    @ConfigOption(name = "pitch", description = "The pitch of the spawn.")
    private float pitch;

    public PositionWithRotation() {
    }

    public PositionWithRotation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
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

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public String toString() {
        return this.x + "," + this.y + "," + this.z + "," + this.yaw + "," + this.pitch;
    }

    public static PositionWithRotation fromString(String str) {
        String[] split = str.split(",");
        if (split.length != 5) {
            return null;
        }

        double x = Double.parseDouble(split[0]);
        double y = Double.parseDouble(split[1]);
        double z = Double.parseDouble(split[2]);
        float yaw = Float.parseFloat(split[3]);
        float pitch = Float.parseFloat(split[4]);
        return new PositionWithRotation(x, y, z, yaw, pitch);
    }
 }
