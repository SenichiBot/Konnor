package me.hechfx.konnor.util

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.json.Json
import java.io.File
import java.util.logging.Logger
import kotlin.random.Random
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GeneralUtils {
    val JSON = Json { ignoreUnknownKeys = true }
    val logger: Logger = Logger.getLogger("main")


    inline fun <reified T> Hocon.decodeFromFile(file: File): T = decodeFromConfig(ConfigFactory.parseFile(file).resolve())
}