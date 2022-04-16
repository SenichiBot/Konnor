package me.hechfx.konnor.structure

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import dev.kord.common.entity.*
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import me.hechfx.konnor.command.dev.*
import me.hechfx.konnor.command.economy.*
import me.hechfx.konnor.command.economy.button.*
import me.hechfx.konnor.command.minecraft.*
import me.hechfx.konnor.command.moderation.*
import me.hechfx.konnor.command.moderation.button.*
import me.hechfx.konnor.command.senichi.*
import me.hechfx.konnor.command.roblox.*
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
import java.io.File
import java.security.SecureRandom
import java.time.Instant

class Konnor(val config: DiscordConfig) {
    lateinit var client: Kord

    companion object {
        val random = SecureRandom()
        val publicCommands = CommandManager()
        val privateCommands = CommandManager()

        fun getTranslation(locale: String): JsonNode {
            return ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).readTree(File("./translation/$locale/general.yml"))
        }
    }

    suspend fun retrieveUser(snowflake: Snowflake): DiscordUser {
        return this.client.rest.user.getUser(snowflake)
    }

    suspend fun start() {
        val applicationId = Snowflake(config.konnorConfig.applicationId)
        client = Kord(config.konnorConfig.token)

        val publicRegistry = KordCommandRegistry(
            applicationId,
            client.rest,
            publicCommands
        )

        val privateRegistry = KordCommandRegistry(
            applicationId,
            client.rest,
            privateCommands
        )

        loadCommands(publicCommands)
        loadPrivateCommands(privateCommands)

        client.gateway.gateways.forEach {
            it.value.installDiscordInteraKTions(
                applicationId,
                client.rest,
                publicCommands
            )
        }

        client.gateway.gateways.forEach {
            it.value.installDiscordInteraKTions(
                applicationId,
                client.rest,
                privateCommands
            )
        }

        client.on<ReadyEvent> {
            println("Initializing Sen'ichi; ${client.guilds.toList().size} Servers.")
        }

        // TODO: Do a message when the user adds the bot to a server.

        coroutineScope {
            // Reset VIP
            launch {
                newSuspendedTransaction {
                    Users.update({ Users.premiumDuration lessEq Instant.now() }) {
                        it[premium] = false
                        it[premiumType] = null
                        it[premiumDuration] = null
                    }
                }
            }

            // Reset Daily
            launch {
                newSuspendedTransaction {
                    Users.update({ Users.dailyTimeout lessEq Instant.now() }) {
                        it[dailyTimeout] = null
                    }
                }
            }
        }

        publicRegistry.updateAllGlobalCommands()
        privateRegistry.updateAllCommandsInGuild(Snowflake(928492632521449522L))

        client.login {
            presence {
                status = PresenceStatus.DoNotDisturb
                playing("Hello World!")
            }
        }
    }

    private fun loadPrivateCommands(commandManager: CommandManager) {
        // ==[ Owners ]==
        commandManager.register(
            OwnerCommand,
            OwnerSetVipCommandExecutor(this),
            OwnerSoulsAddExecutor(this),
            OwnerSoulsRemoveExecutor(this),
            ProfileRenderingTestCommandExecutor(this)
        )
    }

    private fun loadCommands(commandManager: CommandManager) {
        // ==[ Moderation ]==
        commandManager.register(
            ModerationCommand,
            ModerationBanCommandExecutor(this),
            ModerationTimeoutCommandExecutor(this)
        )

            // ==[ Moderation Components ]==
            commandManager.register(ConfirmBanButtonExecutor, ConfirmBanButtonExecutor(this))
            commandManager.register(DenyPunishmentButtonExecutor, DenyPunishmentButtonExecutor())
            commandManager.register(ConfirmTimeoutButtonExecutor, ConfirmTimeoutButtonExecutor(this))

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
            EconomyCommand,
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

        // ==[ Social ]==
        commandManager.register(
            SocialCommand,
            ProfilePronounCommandExecutor(this),
            ProfileCheckCommandExecutor(this)
        )

            // ==[ Social Components ]==
            commandManager.register(PronounsMenuExecutor, PronounsMenuExecutor())
            commandManager.register(ChangeProfileButtonExecutor, ChangeProfileButtonExecutor())
            commandManager.register(SubmitProfileChangesModalExecutor, SubmitProfileChangesModalExecutor())
    }
}