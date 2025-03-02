package me.redned.config;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class Config {
    private static ConfigProvider provider;

    private static ConfigProvider provider() {
        if (provider != null) {
            return provider;
        }

        Iterator<ConfigProvider> iterator = ServiceLoader.load(ConfigProvider.class, Config.class.getClassLoader()).iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("No ConfigProvider found");
        }

        provider = iterator.next();
        return provider;
    }

    public static <C> ConfigParser<C> parser() {
        return provider().parser();
    }

    public static <C> ConfigSerializer<C> serializer() {
        return provider().serializer();
    }

    static void onError(String message) {
        provider().onError(message);
    }
}
