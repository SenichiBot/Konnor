package me.hechfx.konnor.util.profile

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.UserFlag
import dev.kord.rest.Image
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.structure.Konnor
import me.hechfx.konnor.util.image.ConvolveWithEdgeOp
import me.hechfx.konnor.util.image.ImageUtil.applyTransparency
import java.awt.*
import me.hechfx.konnor.util.image.ImageUtil.roundCorners
import me.hechfx.konnor.util.image.ImageUtil.toBufferedImage
import me.hechfx.konnor.util.profile.ProfileGenerator.Companion.drawBadge
import mu.KotlinLogging
import java.awt.image.*
import java.net.URL
import java.util.logging.Logger
import javax.imageio.ImageIO


class ProfileGenerator(private val width: Int, private val height: Int, val konnor: Konnor) {
    companion object {
        val logger = KotlinLogging.logger("ProfileGenerator")

        private val CONVOLVE_OP: List<ConvolveWithEdgeOp> = run {
            val radiuses = listOf(20, 15, 10, 5)

            radiuses.map {
                val size = it * 2 + 1
                val weight = 1.0f / (size * size)
                val data = FloatArray(size * size)

                for (i in data.indices) {
                    data[i] = weight
                }

                val kernel = Kernel(size, size, data)


                ConvolveWithEdgeOp(kernel, ConvolveWithEdgeOp.EDGE_REFLECT,
                    RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                    )
                )
            }
        }

        private fun Graphics2D.drawBadge(name: String, xPos: Int, yPos: Int): Boolean {
            val badges = hashMapOf(
                "VerifiedBotDeveloper" to "https://cdn.discordapp.com/emojis/800732882901270598.png?size=2048",
                "HouseBalance" to "https://cdn.discordapp.com/emojis/799682794997284904.png?size=2048",
                "HouseBrilliance" to "https://cdn.discordapp.com/emojis/779938851731800115.png?size=2048",
                "HouseBravery" to "https://cdn.discordapp.com/emojis/799682822750077018.png?size=2048",
                "EarlySupporter" to "https://cdn.discordapp.com/emojis/800720404543963166.png?size=2048",
                "VerifiedBot" to "https://cdn.discordapp.com/emojis/961117195256094770.png?size=2048",
                "DiscordPartner" to "https://cdn.discordapp.com/emojis/855849398746349578.png?size=2048",
                "BugHunterLevel1" to "https://cdn.discordapp.com/emojis/799682709957247037.png?size=2048",
                "BugHunterLevel2" to "https://cdn.discordapp.com/emojis/858341743376465950.png?size=2048",
                "DiscordEmployee" to "https://cdn.discordapp.com/emojis/855849429064220682.png?size=2048",
                "HypeSquad" to "https://cdn.discordapp.com/emojis/799682574829223996.png?size=2048",
                "BotHttpInteractions" to "https://cdn.discordapp.com/emojis/751143575185784944.png?size=2048",
                "DiscordNitro" to "https://cdn.discordapp.com/emojis/799682750377492482.png?size=2048"
            )

            val asUrl = URL(badges[name])
            val badgeBuffer = ImageIO.read(asUrl.readBytes().inputStream())
                .getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH)

            return this.drawImage(badgeBuffer, xPos, yPos, 64, 64, null)
        }

        private fun Graphics2D.enableFontAntiAliasing(): Graphics2D {
            this.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )
            return this
        }
    }

    suspend fun render(user: User): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = (image.graphics as Graphics2D)
            .enableFontAntiAliasing()

        val asDiscordUser = konnor.client.getUser(Snowflake(user.userId))!!
        val avatarUrl = asDiscordUser.avatar?.getImage(Image.Format.PNG, Image.Size.Size2048)?.data ?: asDiscordUser.defaultAvatar.getImage(Image.Format.PNG, Image.Size.Size2048).data
        val avatarBuffer = ImageIO.read(avatarUrl.inputStream())
            .roundCorners(360*2)
            .getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH)

        if (user.premium) {
            if (user.backgroundUrl != null) {
                val background = ImageIO.read(URL(user.backgroundUrl).readBytes().inputStream())
                    .getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH)
                    .toBufferedImage()

                background.createGraphics().applyTransparency(0.5f)
                graphics.drawImage(background, 0, 0, 800, 600, null)
            } else {
                graphics.color = Color(32, 34, 37)
                graphics.fillRect(0, 0, 800, 600)
            }
        } else {
            graphics.color = Color(32, 34, 37)
            graphics.fillRect(0, 0, 800, 600)
        }

        drawTextOverlay(image.width, image.height, graphics, user)

        val badges = mutableListOf<String>()

        if (asDiscordUser.publicFlags != null && asDiscordUser.publicFlags?.flags != null) {
            asDiscordUser.publicFlags!!.flags.forEach {
                badges.add(it.name)
            }
        }

        if (asDiscordUser.avatar != null && asDiscordUser.avatar?.animated == true || asDiscordUser.getBannerUrl(Image.Format.WEBP) != null) {
            logger.info { "The user has an animated avatar or banner! Adding DiscordNitro flag." }
            badges.add("DiscordNitro")
        }

        if (badges.isNotEmpty()) {

            val basePos = 10
            graphics.drawBadge(badges.removeFirst(), 715, basePos)
            var pos = 15
            badges.forEach {
                pos += 75
                graphics.drawBadge(it, 715, pos)
            }
        }


        if (user.premium) {
            val vipString = when (user.premiumType!!) {
                1 -> "VIP"
                2 -> "VIP+"
                3 -> "VIP++"
                else -> "None"
            }

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString(vipString, 60, 65)

            graphics.drawImage(avatarBuffer, width/2 - (256 / 2), 80, 256, 256, null)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString(asDiscordUser.username, 390, 50)

            graphics.color = Color.WHITE
            graphics.drawCenteredString(user.pronoun ?: "He/Him", 420, 20)

            graphics.color = Color.WHITE
            graphics.drawCenteredString(user.bio, 465, 30)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("Souls", 530, 50)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("${user.coins}", 585, 50)
        } else {
            graphics.drawImage(avatarBuffer, width/2 - (256 / 2), 50, 256, 256, null)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString(asDiscordUser.username, 360, 50)

            graphics.color = Color.WHITE
            graphics.drawCenteredString(user.pronoun ?: "He/Him", 390, 20)

            graphics.color = Color.WHITE
            graphics.drawCenteredString(user.bio, 435, 30)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("Souls", 500, 50)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("${user.coins}", 555, 50)
        }

        // End graphics manipulation
        graphics.dispose()

        return image
    }

    private suspend fun drawTextOverlay(width: Int, height: Int, graphics: Graphics2D, user: User) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val asDiscordUser = konnor.client.getUser(Snowflake(user.userId))!!

        val g2 = image.createGraphics()

        val badges = mutableListOf<String>()

        if (asDiscordUser.publicFlags != null && asDiscordUser.publicFlags?.flags != null) {
            asDiscordUser.publicFlags!!.flags.forEach {
                badges.add(it.name)
            }
        }

        if (asDiscordUser.avatar != null && asDiscordUser.avatar?.animated == true || asDiscordUser.getBannerUrl(Image.Format.WEBP) != null) {
            logger.info { "The user has an animated avatar! Adding DiscordNitro flag." }
            badges.add("DiscordNitro")
        }

        if (badges.isNotEmpty()) {
            val basePos = 10
            g2.drawBadge(badges.removeFirst(), 715, basePos)
            var pos = 15
            badges.forEach {
                pos += 75
                g2.drawBadge(it, 715, pos)
            }
        }


        if (user.premium) {
            val vipString = when (user.premiumType!!) {
                1 -> "VIP"
                2 -> "VIP+"
                3 -> "VIP++"
                else -> "None"
            }

            g2.color = Color.decode(user.color)
            g2.drawCenteredString(vipString, 60, 65)

            g2.color = Color.decode(user.color)
            g2.drawCenteredString(asDiscordUser.username, 390, 50)

            g2.color = Color.BLACK
            g2.drawCenteredString(user.pronoun ?: "He/Him", 420, 20)

            g2.color = Color.BLACK
            g2.drawCenteredString(user.bio, 465, 30)

            g2.color = Color.decode(user.color)
            g2.drawCenteredString("Souls", 530, 50)

            g2.color = Color.decode(user.color)
            g2.drawCenteredString("${user.coins}", 585, 50)
        } else {
            g2.color = Color.decode(user.color)
            g2.drawCenteredString(asDiscordUser.username, 360, 50)

            g2.color = Color.BLACK
            g2.drawCenteredString(user.pronoun ?: "He/Him", 390, 20)

            g2.color = Color.BLACK
            g2.drawCenteredString(user.bio, 435, 30)

            g2.color = Color.decode(user.color)
            g2.drawCenteredString("Souls", 500, 50)

            g2.color = Color.decode(user.color)
            g2.drawCenteredString("${user.coins}", 555, 50)
        }

        g2.dispose()

        for (convolve in CONVOLVE_OP) {
            graphics.drawImage(convolve.filter(image, null), 0, 0, null)
        }
    }

    private fun Graphics2D.drawCenteredString(str: String, y: Int, px: Int? = 10, family: String? = "arial", style: Int? = Font.PLAIN) {
        val font = Font(family, style!!, px!!)

        this.font = font

        val fm = fontMetrics
        val x = (width - fm.stringWidth(str)) / 2

        return this.drawString(str, x, y)
    }
}