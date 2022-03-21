package me.hechfx.konnor.util

import dev.kord.common.Color
import dev.kord.rest.service.RestClient
import io.ktor.client.*
import net.perfectdreams.discordinteraktions.common.entities.User

object Constants {
    val DEFAULT_COLOR = Color(252, 123, 3)

    suspend fun buildBadges(user: User, rest: RestClient): String {
        val u = rest.user.getUser(user.id)
        println("[buildBadges] Fetching ${user.id.value} badges: ${u.publicFlags.value?.flags}")

        val badges = hashMapOf(
            "VerifiedBotDeveloper" to "<:v_developer:779939662336229376>",
            "HouseBalance" to "<:h_balance:779939028446740540>",
            "HouseBrilliance" to "<:h_brilliance:779938851731800115>",
            "HouseBravery" to "<:h_bravery:779938896413982750>",
            "EarlySupporter" to "<:d_earlysup:779940716134269000>"
        )
        var output = ""

        u.publicFlags.value?.flags?.forEach {
            output += " ${badges[it.name]}"
        }

        return output
    }
}