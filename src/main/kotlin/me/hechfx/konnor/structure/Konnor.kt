package me.hechfx.konnor.structure

import dev.kord.common.entity.DiscordShard
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.start
import dev.kord.rest.service.RestClient
import me.hechfx.konnor.command.`fun`.*
import me.hechfx.konnor.command.economy.*
import me.hechfx.konnor.command.economy.button.ConfirmSoulsTransactionButtonExecutor
import me.hechfx.konnor.command.economy.button.DenySoulsTransactionButtonExecutor
import me.hechfx.konnor.command.economy.button.GetDailyButtonExecutor
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
import me.hechfx.konnor.database.task.ResetVipTask
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.discordinteraktions.platforms.kord.installDiscordInteraKTions
import kotlin.random.Random

class Konnor(val config: DiscordConfig) {
    lateinit var client: RestClient
    lateinit var gateway: DefaultGateway

    companion object {
        val random = Random(0L)
        val commandManager = CommandManager()
    }

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

        /**
         * gateway.on<GuildCreate> {
                val owner = client.user.getUser(guild.ownerId)
                val dm = client.user.createDM(DMCreateRequest(owner.id))
                client.channel.createMessage(dm.id) {
                    embed {
                        title = "Hello, I'm Sen'ichi"
                        description = "Hey ${owner.username}, how's going?\n\nThank you for adding me to you server (also called `${guild.name}` ^^)! If you need support just come by in [my server](https://discord.gg/Akw8UAd)!"
                    }
                }
            }
         */

        gateway.start(config.konnorConfig.token) {
            shard = DiscordShard(0, 2)
            presence {
                playing("Hello World!")
            }
        }
        ResetDailyTask().run()
        ResetVipTask().run()
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
        commandManager.register(
            SenichiCommand,
            BotPingCommandExecutor(this),
            BotInformationCommandExecutor(this)
        )

        // ==[ Economy ]==
        commandManager.register(
            SoulsCommand,
            SoulsCheckCommandExecutor(this),
            SoulsPayCommandExecutor(this),
            SoulsTopCommandExecutor(this),
            SoulsDailyCommandExecutor(this)
        )
        // ==[ Economy Components ]==
            commandManager.register(ConfirmSoulsTransactionButtonExecutor, ConfirmSoulsTransactionButtonExecutor())
            commandManager.register(DenySoulsTransactionButtonExecutor, DenySoulsTransactionButtonExecutor())
            commandManager.register(GetDailyButtonExecutor, GetDailyButtonExecutor())

        // ==[ Social }
        commandManager.register(
            ProfileCommand,
            ProfilePronounCommandExecutor(this),
            ProfileCheckCommandExecutor(this)
        )

            // ==[ Social Components ]==
            commandManager.register(PronounsMenuExecutor, PronounsMenuExecutor())
            commandManager.register(ChangeAboutMeButtonExecutor, ChangeAboutMeButtonExecutor())
            commandManager.register(SubmitAboutMeChangesModalExecutor, SubmitAboutMeChangesModalExecutor())

    }
}