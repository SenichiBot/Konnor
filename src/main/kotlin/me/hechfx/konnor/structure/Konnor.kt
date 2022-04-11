package me.hechfx.konnor.structure

import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import me.hechfx.konnor.command.dev.*
import me.hechfx.konnor.command.economy.*
import me.hechfx.konnor.command.economy.button.*
import me.hechfx.konnor.command.minecraft.*
import me.hechfx.konnor.command.misc.*
import me.hechfx.konnor.command.roblox.RobloxCommand
import me.hechfx.konnor.command.roblox.RobloxUserCommandExecutor
import me.hechfx.konnor.command.social.*
import me.hechfx.konnor.command.social.button.*
import me.hechfx.konnor.command.social.menu.*
import me.hechfx.konnor.command.social.modal.*
import me.hechfx.konnor.config.DiscordConfig
import me.hechfx.konnor.database.table.Users
import net.perfectdreams.discordinteraktions.common.commands.CommandManager
import net.perfectdreams.discordinteraktions.platforms.kord.commands.KordCommandRegistry
import net.perfectdreams.discordinteraktions.platforms.kord.installDiscordInteraKTions
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.security.SecureRandom
import java.time.Instant

class Konnor(val config: DiscordConfig) {
    lateinit var client: Kord

    companion object {
        val random = SecureRandom()
        val commandManager = CommandManager()
    }

    suspend fun retrieveUser(snowflake: Snowflake): DiscordUser {
        return this.client.rest.user.getUser(snowflake)
    }

    suspend fun start() {
        val applicationId = Snowflake(config.konnorConfig.applicationId)
        client = Kord(config.konnorConfig.token)

        val registry = KordCommandRegistry(
            applicationId,
            client.rest,
            commandManager
        )

        loadCommands(commandManager)

        client.gateway.gateways.forEach {
            it.value.installDiscordInteraKTions(
                applicationId,
                client.rest,
                commandManager
            )
        }

        client.on<ReadyEvent> {
            println("Initializing Sen'ichi; ${client.guilds.toList().size} Servers.")
        }

        // TODO: Do a message when the user adds the bot to a server.

        // ==[ Reset ]==
        GlobalScope.launch {
            newSuspendedTransaction {
                Users.update({ Users.premiumDuration lessEq Instant.now() }) {
                    it[premium] = false
                    it[premiumType] = null
                    it[premiumDuration] = null
                }

                Users.update({ Users.dailyTimeout lessEq Instant.now() }) {
                    it[dailyTimeout] = null
                }
            }
        }

        registry.updateAllGlobalCommands(true)

        client.login {
            presence {
                playing("Hello World!")
            }
        }
    }

    private fun loadCommands(commandManager: CommandManager) {
        // ==[ Owners ]==
        commandManager.register(
            OwnerCommand,
            OwnerSetVipCommandExecutor(this),
            OwnerSoulsAddExecutor(this),
            OwnerSoulsRemoveExecutor(this),
            ProfileRenderingTestCommandExecutor(this)
        )

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
            SoulsDailyCommandExecutor(this),
            SoulsBetCommandExecutor(this)
        )
        // ==[ Economy Components ]==
            commandManager.register(ConfirmSoulsTransactionButtonExecutor, ConfirmSoulsTransactionButtonExecutor())
            commandManager.register(DenySoulsTransactionButtonExecutor, DenySoulsTransactionButtonExecutor())
            commandManager.register(GetDailyButtonExecutor, GetDailyButtonExecutor())
            commandManager.register(AcceptSoulsGamblingTransactionButtonExecutor, AcceptSoulsGamblingTransactionButtonExecutor(this))

        // ==[ Social }
        commandManager.register(
            ProfileCommand,
            ProfilePronounCommandExecutor(this),
            ProfileCheckCommandExecutor(this)
        )

            // ==[ Social Components ]==
            commandManager.register(PronounsMenuExecutor, PronounsMenuExecutor())
            commandManager.register(ChangeProfileButtonExecutor, ChangeProfileButtonExecutor())
            commandManager.register(SubmitProfileChangesModalExecutor, SubmitProfileChangesModalExecutor())

    }
}