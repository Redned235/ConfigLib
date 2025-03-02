import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    group = "me.redned.configlib"
    version = "1.0.0"

    java.sourceCompatibility = JavaVersion.VERSION_17
    java.targetCompatibility = JavaVersion.VERSION_17

    repositories {
        mavenCentral()
    }

     tasks.withType<ShadowJar> {
         from("src/main/java/resources") {
             include("*")
         }

         archiveBaseName.set("${rootProject.name}-${project.name}")
         archiveClassifier.set("")
     }

    tasks.jar {
        archiveClassifier.set("unshaded")
    }

    tasks.named("build") {
        dependsOn(tasks.shadowJar)
    }

    publishing {
        publications.create<MavenPublication>("library") {
            artifact(tasks.shadowJar)
        }
    }
}
