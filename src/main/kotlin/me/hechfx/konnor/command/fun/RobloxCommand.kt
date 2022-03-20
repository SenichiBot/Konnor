package me.hechfx.konnor.command.`fun`

import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import me.hechfx.api.roblox

object RobloxCommand: SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("roblox", "Roblox Commands") {
        subcommand("user", "Fetch informations from a user") {
            executor
        }
    }
}

class RobloxUserCommandExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(RobloxUserCommandExecutor::class) {
        object CommandOptions: ApplicationCommandOptions() {
            val user = string("user", "Roblox user name")
                .register()
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val robloxInstance = RobloxInstance
    }
}