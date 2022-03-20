plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "me.hechfx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    // ==[ Kord && DiscordInteraktions repositories ]==
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    // ==[ "Failed to load class org.slf4j.impl.StaticLoggerBinder" And shit ]==
    implementation("org.slf4j:slf4j-simple:1.7.36")

    // ==[ Discord ]==
    implementation("net.perfectdreams.discordinteraktions:gateway-kord:0.0.12-20220319.161935-20")
    implementation("dev.kord:kord-common:0.8.x-20220315.083129-149")
    implementation("dev.kord:kord-gateway:0.8.x-20220315.083129-149")
    implementation("dev.kord:kord-rest:0.8.x-20220315.083129-149")
    // ==[ Database ]==
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.3.3")
    implementation("net.perfectdreams.exposedpowerutils:postgres-java-time:1.0.0")
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")

    // ==[ Serialization ]==
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.3.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        manifest {
            attributes["Main-Class"] = "me.hechfx.konnor.Main"
        }
    }
}

application {
    mainClassName = "me.hechfx.konnor.Main"
}