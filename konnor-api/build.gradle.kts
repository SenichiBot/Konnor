plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "me.hechfx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.3.2")

    implementation("net.perfectdreams.discordinteraktions:gateway-kord:0.0.12-20220319.161935-20")
    implementation("dev.kord:kord-common:0.8.x-20220315.083129-149")
    implementation("dev.kord:kord-gateway:0.8.x-20220315.083129-149")
    implementation("dev.kord:kord-rest:0.8.x-20220315.083129-149")
}