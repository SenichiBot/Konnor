package me.hechfx.konnor.command.economy.button

import kotlinx.datetime.Clock
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.Constants.DEFAULT_COLOR
import me.hechfx.konnor.util.Constants.ONE_DAY_IN_MILLISECONDS
import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.components.ButtonClickExecutorDeclaration
import net.perfectdreams.discordinteraktions.common.components.ButtonClickWithDataExecutor
import net.perfectdreams.discordinteraktions.common.components.ComponentContext
import net.perfectdreams.discordinteraktions.common.entities.User
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class GetDailyButtonExecutor : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(GetDailyButtonExecutor::class, "get_daily")

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (user.id.value != data.toULong()) return

        val authorProfile = newSuspendedTransaction {
            me.hechfx.konnor.database.dao.User[data.toLong()]
        }

        val quantity = when (authorProfile.premiumType) {
            1 -> Konnor.random.nextLong(3000, 5000)
            2 -> Konnor.random.nextLong(3000, 7000)
            3 -> Konnor.random.nextLong(3000, 9000)
            else -> Konnor.random.nextLong(1200, 3000)
        }

        context.deferUpdateMessage()

        context.updateMessage {
            embed {
                color = DEFAULT_COLOR
                title = "Your Daily Status"
                description = "You successfully received $quantity Souls from Daily!"
            }

            components = mutableListOf()
        }

        if (authorProfile.dailyTimeout != null && Clock.System.now().toEpochMilliseconds() <= authorProfile.dailyTimeout?.toEpochMilli()!!) {
            context.sendEphemeralMessage {
                content = "You can't get your daily right now! You can get your daily: <t:${authorProfile.dailyTimeout!!.epochSecond}:R>"
            }
        } else if (authorProfile.dailyTimeout == null || (authorProfile.dailyTimeout != null && Clock.System.now().toEpochMilliseconds() >= authorProfile.dailyTimeout!!.toEpochMilli())){
            newSuspendedTransaction {
                authorProfile.coins += quantity
                authorProfile.dailyTimeout = Instant.ofEpochMilli(Clock.System.now().toEpochMilliseconds() + ONE_DAY_IN_MILLISECONDS)
            }

            context.sendEphemeralMessage {
                content = "Yay, you have got $quantity Souls from daily!"
            }
        }
    }
}