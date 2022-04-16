package me.hechfx.konnor

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.hocon.Hocon
import me.hechfx.konnor.config.DiscordConfig
import me.hechfx.konnor.database.DatabaseService
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.GeneralUtils.logger
import me.hechfx.konnor.util.GeneralUtils.decodeFromFile
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
            logger.info { "Trying to create a new config file..." }
            val content = """
                konnorConfig {
                    token = "insert your token here"
                    applicationId = 123
                    owners = [1, 2]
                }
                databaseConfig {
                    host = "localhost"
                    port = "5432"
                    database = "konnor"
                    user = "postgres"
                    password = "youshallnotpass"
                }
                
                riotConfig = {
                    apiKey = "cool api key here"
                }
            """.trimIndent()

            configFile.writeBytes(content.toByteArray())

            throw Exception("Cannot retrieve the config file, generating a new one...")
        }
    }
}