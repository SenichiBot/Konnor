package me.hechfx.konnor.command.social.menu

import me.hechfx.konnor.util.Constants
import me.hechfx.konnor.database.table.Users
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.components.SelectMenuExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.SelectMenuWithDataExecutor
import net.perfectdreams.discordinteraktions.common.entities.User
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class PronounsMenuExecutor: SelectMenuWithDataExecutor {
    companion object : SelectMenuExecutorDeclaration(PronounsMenuExecutor::class, "select_pronouns")

    override suspend fun onSelect(user: User, context: ComponentContext, data: String, values: List<String>) {
        context.deferUpdateMessage()

        val profile = newSuspendedTransaction {
            me.hechfx.konnor.database.dao.User[data.toLong()]
        }

        if (user.id.value != data.toULong()) return

        context.updateMessage {
            components = mutableListOf()
            embed {
                title = "Current Pronoun"
                description = "`${profile.pronoun ?: "Your pronoun has not yet been defined."}`"
                color = Constants.DEFAULT_COLOR

                field {
                    name = "Available Pronouns"
                    inline = false
                    value = "He/Him\nShe/Her\nThey/Them"
                }
            }
        }

        val fancy = hashMapOf(
            "he_him" to "He/Him",
            "she_her" to "She/Her",
            "they_them" to "They/Them"
        )

        val fancyValue = fancy[values[0]]

        newSuspendedTransaction {
            if (profile.pronoun == fancyValue) {
                context.sendEphemeralMessage {
                    content = "This is already your pronoun."
                }
            } else {
                Users.update({ Users.id eq data.toLong() }) {
                    it[pronoun] = fancyValue
                }

                context.updateMessage {
                    embed {
                        title = "Current Pronoun"
                        description = "`${fancyValue}`"
                        color = Constants.DEFAULT_COLOR

                        field {
                            name = "Available Pronouns"
                            inline = false
                            value = "He/Him\nShe/Her\nThey/Them"
                        }
                    }
                }
            }
        }
    }
}