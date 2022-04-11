package me.hechfx.konnor.command.social.button

import dev.kord.common.entity.TextInputStyle
import me.hechfx.konnor.command.social.modal.SubmitProfileChangesModalExecutor
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.modals.components.textInput
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ChangeProfileButtonExecutor: ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ChangeProfileButtonExecutor::class, "change_profile")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (user.id.value != data.toULong()) return

        val u = newSuspendedTransaction {
            me.hechfx.konnor.database.dao.User.getOrInsert(data.toLong())
        }

        context.sendModal(SubmitProfileChangesModalExecutor, data, "Edit your profile!") {
            actionRow {
                textInput(SubmitProfileChangesModalExecutor.options.bio, TextInputStyle.Short, "Your new about me") {
                    allowedLength = 5..100
                }
            }

            if (u.premium) {
                actionRow {
                    textInput(SubmitProfileChangesModalExecutor.options.color, TextInputStyle.Short, "Your new color profile") {
                        allowedLength = 3..7
                    }
                }
            }
        }
    }
}