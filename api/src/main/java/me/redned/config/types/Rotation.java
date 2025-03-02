package me.redned.config.types;

import me.redned.config.ConfigOption;

/**
 * Represents a rotation.
 */
public class Rotation {
    @ConfigOption(name = "yaw", description = "The yaw of the spawn.")
    private float yaw;
    @ConfigOption(name = "pitch", description = "The pitch of the spawn.")
    private float pitch;

    public Rotation() {
    }

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public String toString() {
        return this.yaw + "," + this.pitch;
    }

    public static Rotation fromString(String str) {
        String[] split = str.split(",");
        if (split.length != 2) {
            return null;
        }

        float yaw = Float.parseFloat(split[0]);
        float pitch = Float.parseFloat(split[1]);
        return new Rotation(yaw, pitch);
    }
 }
