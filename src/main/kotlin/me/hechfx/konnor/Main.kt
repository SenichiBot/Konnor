package me.hechfx.konnor

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.hocon.encodeToConfig
import me.hechfx.konnor.config.DiscordConfig
import me.hechfx.konnor.config.KonnorConfig
import me.hechfx.konnor.config.database.DatabaseConfig
import me.hechfx.konnor.database.PostgreSQL
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
            PostgreSQL.startDatabase(config.databaseConfig)
            runBlocking {
                Konnor(config).start()
            }
        } else {
            val content = DiscordConfig(
                KonnorConfig(
                    "insert your token here",
                    123L
                ),
                DatabaseConfig(
                    "localhost",
                    "5432",
                    "konnor",
                    "postgres",
                    "youshallnotpass"
                )
            )

            configFile.writeBytes(Hocon.encodeToString(content).toByteArray())

            throw Exception("Cannot retrieve the config file, generating a new one...")
        }
    }
}