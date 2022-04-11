package me.hechfx.konnor.command.social

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.NamedFile
import me.hechfx.konnor.command.social.button.ChangeProfileButtonExecutor
import me.hechfx.konnor.command.social.menu.PronounsMenuExecutor
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import me.hechfx.konnor.util.profile.ProfileGenerator
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.commands.*
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.components.interactiveButton
import net.perfectdreams.discordinteraktions.common.components.selectMenu
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO


object ProfileCommand: SlashCommandDeclarationWrapper {
    override fun declaration() = slashCommand("profile", "Profile Command") {
        subcommand("check", "Check your profile.") {
            executor = ProfileCheckCommandExecutor
        }

        subcommand("pronoun", "Check or change your in-bot pronoun") {
            executor = ProfilePronounCommandExecutor
        }
    }
}

class ProfileCheckCommandExecutor(val konnor: Konnor): SlashCommandExecutor() {
    companion object: SlashCommandExecutorDeclaration(ProfileCheckCommandExecutor::class) {
        object CommandOptions: ApplicationCommandOptions() {
            val user = optionalUser("user", "Check someone's profile.")
                .register()
        }

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val user = args[options.user] ?: context.sender

        val profile = newSuspendedTransaction {
            User.getOrInsert(user.id.value.toLong())
        }

        val image = ProfileGenerator(800, 600, konnor)
            .render(profile)

        val os = ByteArrayOutputStream()
        ImageIO.write(image, "png", os)
        val inputStream: InputStream = ByteArrayInputStream(os.toByteArray())


        context.sendMessage {
            files?.add(NamedFile("profile.png", inputStream))

            if (user.id.value == context.sender.id.value) {
                actionRow {
                    interactiveButton(ButtonStyle.Primary, ChangeProfileButtonExecutor, context.sender.id.value.toString()) {
                        label = "Edit Profile"
                    }
                }
            }
        }
    }
}

class ProfilePronounCommandExecutor(val konnor: Konnor): SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(ProfilePronounCommandExecutor::class) {
        object CommandOptions: ApplicationCommandOptions()

        override val options = CommandOptions
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val profile = newSuspendedTransaction {
            User.getOrInsert(context.sender.id.value.toLong())
        }

        context.sendMessage {
            embed {
                title = "Current Pronoun"
                description = "`${profile.pronoun ?: "Your pronoun has not yet been defined."}`"
                color = DEFAULT_COLOR

                field {
                    name = "Available Pronouns"
                    inline = false
                    value = "He/Him\nShe/Her\nThey/Them"
                }

                footer {
                    text = "Select your pronoun on the select menu below."
                }

                actionRow {
                    selectMenu(PronounsMenuExecutor, context.sender.id.value.toString()) {
                        option("He/Him", "he_him")
                        option("She/Her", "she_her")
                        option("They/Them", "they_them")
                    }
                }
            }
        }
    }
}