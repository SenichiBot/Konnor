package me.hechfx.konnor

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.hocon.Hocon
import me.hechfx.konnor.config.DiscordConfig
import me.hechfx.konnor.database.DatabaseService
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.GeneralUtils.decodeFromFile
import me.hechfx.konnor.util.GeneralUtils.encodeToString
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val configFile = File("discord.conf")

        if (configFile.exists()) {
            val config: DiscordConfig = Hocon.decodeFromFile(configFile)
            DatabaseService.connect(config.databaseConfig)
            runBlocking {
                Konnor(config).start()
            }
        } else {
            val content = DiscordConfig(
                DiscordConfig.KonnorConfig(
                    "insert your token here",
                    123L
                ),
                DiscordConfig.DatabaseConfig(
                    "localhost",
                    "5432",
                    "konnor",
                    "postgres",
                    "youshallnotpass"
                ),
                DiscordConfig.ValorantConfig(
                    "cool-api-key-here"
                )
            )

            configFile.writeBytes(Hocon.encodeToString(content).toByteArray())

            throw Exception("Cannot retrieve the config file, generating a new one...")
        }
    }
}