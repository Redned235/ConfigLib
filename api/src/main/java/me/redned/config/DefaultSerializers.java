package me.redned.config;

import me.redned.config.types.Position;
import me.redned.config.types.PositionWithRotation;
import me.redned.config.types.Rotation;

import java.awt.Color;
import java.time.Duration;

final class DefaultSerializers {

    static void register() {
        ConfigSerializer.registerSerializerInternal(Position.class, (name, node, value) -> {
            node.getNode(name, "x").set(value.getX());
            node.getNode(name, "y").set(value.getY());
            node.getNode(name, "z").set(value.getZ());
        });

        ConfigSerializer.registerSerializerInternal(Rotation.class, (name, node, value) -> {
            node.getNode(name, "yaw").set(value.getYaw());
            node.getNode(name, "pitch").set(value.getPitch());
        });

        ConfigSerializer.registerSerializerInternal(PositionWithRotation.class, (name, node, value) -> {
            node.getNode(name, "x").set(value.getX());
            node.getNode(name, "y").set(value.getY());
            node.getNode(name, "z").set(value.getZ());
            node.getNode(name, "yaw").set(value.getYaw());
            node.getNode(name, "pitch").set(value.getPitch());
        });

        ConfigSerializer.registerSerializerInternal(Duration.class, (name, node, value) -> {
            long seconds = value.getSeconds();
            StringBuilder result = new StringBuilder();

            long years = seconds / (365 * 24 * 60 * 60);
            if (years > 0) {
                result.append(years).append("y");
                seconds %= (365 * 24 * 60 * 60);
            }

            long months = seconds / (30 * 24 * 60 * 60);
            if (months > 0) {
                result.append(months).append("M");
                seconds %= (30 * 24 * 60 * 60);
            }

            long weeks = seconds / (7 * 24 * 60 * 60);
            if (weeks > 0) {
                result.append(weeks).append("w");
                seconds %= (7 * 24 * 60 * 60);
            }

            long days = seconds / (24 * 60 * 60);
            if (days > 0) {
                result.append(days).append("d");
                seconds %= (24 * 60 * 60);
            }

            long hours = seconds / (60 * 60);
            if (hours > 0) {
                result.append(hours).append("h");
                seconds %= (60 * 60);
            }

            long minutes = seconds / 60;
            if (minutes > 0) {
                result.append(minutes).append("m");
                seconds %= 60;
            }

            if (seconds > 0) {
                result.append(seconds).append("s");
            }

            node.getNode(name).set(result.toString());
        });

        ConfigSerializer.registerSerializerInternal(Color.class, (name, node, value) -> {
            node.getNode(name).set("#" + Integer.toHexString(value.getRGB()).substring(2));
        });
    }
}
