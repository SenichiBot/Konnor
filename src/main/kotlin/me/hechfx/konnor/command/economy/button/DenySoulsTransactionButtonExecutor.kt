package me.hechfx.konnor.command.economy.button

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User

class DenySoulsTransactionButtonExecutor : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(DenySoulsTransactionButtonExecutor::class, "deny_souls_transactions")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (user.id != Snowflake(data)) return

        context.updateMessage {
            content = "Transaction cancelled."
            components = mutableListOf()
        }
    }
}