repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    api(projects.api)

    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
}
