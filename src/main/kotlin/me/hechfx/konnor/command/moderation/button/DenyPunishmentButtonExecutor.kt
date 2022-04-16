package me.hechfx.konnor.command.moderation.button

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User

class DenyPunishmentButtonExecutor : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(DenyPunishmentButtonExecutor::class, "deny_punishment")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (user.id != Snowflake(data)) return

        context.updateMessage {
            content = "The punishment was been cancelled."
            components = mutableListOf()
        }
    }
}