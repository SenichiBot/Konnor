package me.hechfx.konnor.structure

import dev.kord.common.entity.Snowflake
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.start
import dev.kord.rest.service.RestClient
import me.hechfx.konnor.command.`fun`.*
import me.hechfx.konnor.command.economy.*
import me.hechfx.konnor.command.minecraft.MinecraftCommand
import me.hechfx.konnor.command.minecraft.MinecraftServerCommandExecutor
import me.hechfx.konnor.command.minecraft.MinecraftUserCommandExecutor
import me.hechfx.konnor.command.misc.*
import me.hechfx.konnor.command.social.*
import me.hechfx.konnor.command.social.button.*
import me.hechfx.konnor.command.social.menu.*
import me.hechfx.konnor.command.social.modal.*
import me.hechfx.konnor.config.DiscordConfig
import me.hechfx.konnor.database.task.ResetDailyTask
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.discordinteraktions.platforms.kord.installDiscordInteraKTions

class Konnor(val config: DiscordConfig) {
    lateinit var client: RestClient
    lateinit var gateway: DefaultGateway

    val commandManager = CommandManager()

    suspend fun start() {
        val applicationId = Snowflake(config.konnorConfig.applicationId)
        client = RestClient(config.konnorConfig.token)
        gateway = DefaultGateway {}

        val registry = KordCommandRegistry(
            applicationId,
            client,
            commandManager
        )

        loadCommands(commandManager)

        registry.updateAllGlobalCommands(true)

        gateway.installDiscordInteraKTions(
            applicationId,
            client,
            commandManager
        )

        gateway.start(config.konnorConfig.token)
        ResetDailyTask().run()
    }

    private fun loadCommands(commandManager: CommandManager) {
        // ==[ Minecraft ]==
        commandManager.register(
            MinecraftCommand,
            MinecraftUserCommandExecutor(),
            MinecraftServerCommandExecutor()
        )

        // ==[ Fun ]==
        commandManager.register(RobloxCommand, RobloxUserCommandExecutor())

        // ==[ Misc ]==
        commandManager.register(PingCommand, PingCommandExecutor(this))

        // ==[ Economy ]==
        commandManager.register(SoulsCommand, SoulsCheckCommandExecutor(this))

        // ==[ Social }
        commandManager.register(
            ProfileCommand,
            ProfilePronounCommandExecutor(this),
            ProfileCheckCommandExecutor(this)
        )

            // ==[ Social Buttons/Modals/Menus ]==
            commandManager.register(PronounsMenuExecutor, PronounsMenuExecutor())
            commandManager.register(ChangeAboutMeButtonExecutor, ChangeAboutMeButtonExecutor())
            commandManager.register(SubmitAboutMeChangesModalExecutor, SubmitAboutMeChangesModalExecutor())

    }
}