package me.hechfx.konnor.command.dev

import dev.kord.rest.NamedFile
import kotlinx.datetime.Clock
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.Constants.ONE_DAY_IN_MILLISECONDS
import me.hechfx.konnor.util.profile.ProfileGenerator
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.File
import java.time.Instant
import javax.imageio.ImageIO

object OwnerCommand : SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("owner", "Owner-only commands.") {
        subcommand("vip", "Set vip to someone.") {

            executor = OwnerSetVipCommandExecutor
        }

        subcommand("souls_remove", "Remove souls from a user.") {
            executor = OwnerSoulsRemoveExecutor
        }

        subcommand("souls_add", "Add souls to a user.") {
            executor = OwnerSoulsAddExecutor
        }

        subcommand("profile_test", "test if the profile is redering well") {
            executor = ProfileRenderingTestCommandExecutor
        }
    }
}

class ProfileRenderingTestCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(ProfileRenderingTestCommandExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (!konnor.config.konnorConfig.owners.contains(context.sender.id.value.toLong())) return

        val profile = newSuspendedTransaction {
            User.getOrInsert(context.sender.id.value.toLong())
        }

        val image = ProfileGenerator(800, 600, konnor).render(profile)
        val file = File("profile.png")

        ImageIO.write(image, "png", file)

        context.sendMessage {
            files?.add(NamedFile("profile.png", file.inputStream()))
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

class OwnerSoulsRemoveExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(OwnerSoulsRemoveExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val target = user("target", "the user target.")
                .register()
            val amount = integer("amount", "the amount to be removed.")
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
            targetProfile.coins -= args[options.amount]
        }

        context.sendEphemeralMessage {
            content = "Removing ${args[options.amount]} Souls from <@${args[options.target].id.value}>!"
        }
    }
}

class OwnerSoulsAddExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(OwnerSoulsAddExecutor::class) {
        object CommandOptions : ApplicationCommandOptions() {
            val target = user("target", "the user target")
                .register()
            val amount = integer("amount", "souls to remove")
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
            targetProfile.coins += args[options.amount]
        }

        context.sendEphemeralMessage {
            content = "Adding ${args[OwnerSoulsRemoveExecutor.options.amount]} Souls to <@${args[OwnerSoulsRemoveExecutor.options.target].id.value}>!"
        }
    }
}