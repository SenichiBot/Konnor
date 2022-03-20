package me.hechfx.konnor.util

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.hocon.encodeToConfig
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random

object GeneralUtils {
    val RANDOM = Random(0L)
    val JSON = Json { ignoreUnknownKeys = true }

    inline fun <reified T> Hocon.encodeToString(value: T): String = decodeFromConfig(encodeToConfig(value))

    inline fun <reified T> Hocon.decodeFromFile(file: File): T = decodeFromConfig(ConfigFactory.parseFile(file).resolve())
}