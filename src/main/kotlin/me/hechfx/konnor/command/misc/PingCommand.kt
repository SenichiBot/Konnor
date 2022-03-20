package me.hechfx.konnor.command.misc

import me.hechfx.konnor.util.MessageUtils.reply
import me.hechfx.konnor.structure.Konnor
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.entities.messages.editMessage

object PingCommand: SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("ping", "Check my ping!") {
        executor = PingCommandExecutor
    }
}

class PingCommandExecutor(val konnor: Konnor) : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(PingCommandExecutor::class) {
        object Options : ApplicationCommandOptions()

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val ms = System.currentTimeMillis()
        val message = context.reply(
            null,
            "API: `...` | BOT: `${konnor.gateway.ping.value}`"
        )

        message.editMessage {
            val diff = (System.currentTimeMillis() - ms)
            content = "\uD83D\uDD39 • API: `${diff}ms` | BOT: `${konnor.gateway.ping.value}`"
        }
    }
}