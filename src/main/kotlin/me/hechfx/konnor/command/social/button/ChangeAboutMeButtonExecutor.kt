package me.hechfx.konnor.command.social.button

import dev.kord.common.entity.TextInputStyle
import me.hechfx.konnor.command.social.modal.SubmitAboutMeChangesModalExecutor
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.modals.components.textInput

class ChangeAboutMeButtonExecutor: ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(ChangeAboutMeButtonExecutor::class, "change_about_me")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (user.id.value != data.toULong()) return

        context.sendModal(SubmitAboutMeChangesModalExecutor, data, "Insert Your New About Me!") {
            actionRow {
                textInput(SubmitAboutMeChangesModalExecutor.options.bio, TextInputStyle.Short, "your new about me") {
                    allowedLength = 5..100
                }
            }
        }
    }
}