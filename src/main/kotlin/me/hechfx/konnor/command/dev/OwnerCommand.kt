package me.hechfx.konnor.command.dev

import kotlinx.datetime.Clock
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.Constants.ONE_DAY_IN_MILLISECONDS
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

object OwnerCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("owner", "Owner-only commands.") {
        subcommand("vip", "Set vip to someone.") {
            executor = OwnerSetVipCommandExecutor
        }
    }
}

class OwnerSetVipCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(OwnerSetVipCommandExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val target = user("target", "target to apply the vip")
                .register()
            val type = integer("type", "type of the vip to apply")
                .choice(1, "VIP")
                .choice(2, "VIP+")
                .choice(3, "VIP++")
                .register()
            val days = integer("days", "days to apply the vip")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (!konnor.config.konnorConfig.owners.contains(context.sender.id.value.toLong())) return

        val targetProfile = newSuspendedTransaction {
            User.getOrInsert(args[options.target].id.value.toLong())
        }

        newSuspendedTransaction {
            targetProfile.premium = true
            targetProfile.premiumType = args[options.type].toInt()
            targetProfile.premiumDuration = Instant.ofEpochMilli(Clock.System.now().toEpochMilliseconds() + (args[options.days] * ONE_DAY_IN_MILLISECONDS))
        }

        context.sendEphemeralMessage {
            content = "Giving ${args[options.type]} VIP to <@${args[options.target].id.value}> for ${args[options.days]} Days"
        }
    }
}