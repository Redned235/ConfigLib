package me.redned.config;

import me.redned.config.types.Position;
import me.redned.config.types.PositionWithRotation;
import me.redned.config.types.Rotation;

import java.awt.Color;
import java.time.Duration;

final class DefaultParsers {

    static void register() {
        ConfigParser.registerProviderInternal(Color.class, new ColorParser());
        ConfigParser.registerProviderInternal(Duration.class, new DurationParser());
        ConfigParser.registerProviderInternal(Position.class, configValue -> {
            if (!(configValue instanceof ConfigNode positionNode)) {
                return Position.fromString(configValue.toString());
            }

            double x = positionNode.getNode("x").get(Double.class);
            double y = positionNode.getNode("y").get(Double.class);
            double z = positionNode.getNode("z").get(Double.class);
            return new Position(x, y, z);
        });

        ConfigParser.registerProviderInternal(Rotation.class, configValue -> {
            if (!(configValue instanceof ConfigNode rotationNode)) {
                return Rotation.fromString(configValue.toString());
            }

            float yaw = rotationNode.getNode("yaw").get(Float.class);
            float pitch = rotationNode.getNode("pitch").get(Float.class);
            return new Rotation(yaw, pitch);
        });

        ConfigParser.registerProviderInternal(PositionWithRotation.class, configValue -> {
            if (!(configValue instanceof ConfigNode node)) {
                return PositionWithRotation.fromString(configValue.toString());
            }

            double x = node.getNode("x").get(Double.class);
            double y = node.getNode("y").get(Double.class);
            double z = node.getNode("z").get(Double.class);
            float yaw = node.getNode("yaw").get(Float.class);
            float pitch = node.getNode("pitch").get(Float.class);
            return new PositionWithRotation(x, y, z, yaw, pitch);
        });

        ConfigParser.registerProviderInternal(Color.class, new ColorParser());
    }
}
