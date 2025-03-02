# ConfigLib

A dead simple configuration library.

This is a dead-simple configuration library which allows for you to easily handle and parse values from configuration files. It is primarily POJO-focused, working similarly to Gson. While primarily designed to wrap around Spigot's configuration system, wrappers can be created for any platform, and contributions are most certainly welcome.

## Usage
To start, this library is broken into two parts: The [`ConfigParser`](https://github.com/Redned235/ConfigLib/blob/master/api/src/main/java/me/redned/config/ConfigParser.java) and the [`ConfigSerializer`](https://github.com/Redned235/ConfigLib/blob/master/api/src/main/java/me/redned/config/ConfigSerializer.java).

### ConfigParser
The `ConfigParser` is the primary entrypoint to read configuration files. This can simply be retrieved by running `Config.parser()`, which allows for loading configuration files.

### ConfigSerializer
The `ConfigSerializer` is the primary entrypoint to write configuration files. This can simply be retrieved by running `Config.serializer()`, which allows for saving configuration files.

### Creating a config
A config itself is just represented as a POJO, and does not need to implement anything. However, this is where the [`@ConfigOption`](https://github.com/Redned235/ConfigLib/blob/master/api/src/main/java/me/redned/config/ConfigOption.java) annotation comes into play, which is used over a parameter to denote that it should be retrieved from the configuration file.

Here is an example
```java
public class ExampleConfig {

    @ConfigOption(name = "name", description = "The name of the config.", required = true)
    public String name;

    @ConfigOption(name = "time", description = "The time of day.", required = true)
    public Duration time;
}
```

The `@ConfigOption` annotation has the following parameters:
- `name`: The name of the key in the configuration file.
- `description`: A description of the key.
- `required`: Whether the key is required. If it is, and it is not found, an exception will be thrown.
- `contextProvider`: A provider that allows for more complex parsing of the value.

### Loading the config
To load the config, you can simply run the following code:
```java
Path configPath = ...;
try {
    ExampleConfig config = Config.parser().loadConfig(configPath, ExampleConfig.class);
} catch (ParseException e) {
    ParseException.handle(e);
}
```

### Saving the config
To save the config, you can simply run the following code:
```java
Path configPath = ...;
ExampleConfig config = ...;
try {
    Config.serializer().saveConfig(configPath, config);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Adding custom types
This library allows for easy support for implementing custom types, allowing you to parse custom objects. This can be done by running `Config.parser().registerProvider(Class<T>, Parser<T>)`. Examples for default types can be found [here](https://github.com/Redned235/ConfigLib/blob/master/api/src/main/java/me/redned/config/DefaultParsers.java). Serializers can be registered in a similar way, by running `Config.serializer().registerSerializer(Class<T>, Serializer<T>)`, with examples found [here](https://github.com/Redned235/ConfigLib/blob/master/api/src/main/java/me/redned/config/DefaultSerializers.java).

## Platforms
Currently, this project works with Spigot 1.19.4+ (with additional support for Paper), but can easily be expanded to support other config libraries, such as Configurate or BungeeCord's config. This also adds support for serializing various Minecraft types, such as `ItemStack`s, `BlockData` and `Location`s.

## Repository
Currently, the only platform support is Spigot, so the repository is as follows:

### Gradle
```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.Redned235.ConfigLib:spigot:master-SNAPSHOT")
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.Redned235.ConfigLib</groupId>
        <artifactId>spigot</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>
</dependencies>
```

## Links
- Donate: https://patreon.com/redned
