package me.hechfx.konnor.util.profile

import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import me.hechfx.konnor.database.dao.User
import me.hechfx.konnor.structure.Konnor
import java.awt.*
import java.awt.geom.RoundRectangle2D
import java.awt.image.*
import javax.imageio.ImageIO


class ProfileGenerator(private val width: Int, private val height: Int, val konnor: Konnor) {
    companion object {
        val CONVOLVE_OP: ConvolveOp = run {
            println("Creating Convolve Op")
            val radius = 20
            val size = radius * 2 + 1
            val weight = 1.0f / (size * size)
            val data = FloatArray(size * size)

            for (i in data.indices) {
                data[i] = weight
            }

            val kernel = Kernel(size, size, data)
            ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null)
        }
    }

    suspend fun render(user: User): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = (image.graphics as Graphics2D)
            .enableFontAntiAliasing()

        val asDiscordUser = konnor.client.getUser(Snowflake(user.userId))!!
        val avatarUrl = asDiscordUser.avatar?.getImage(Image.Format.PNG, Image.Size.Size2048)?.data ?: asDiscordUser.defaultAvatar.getImage(Image.Format.PNG, Image.Size.Size2048).data
        val avatar = ImageIO.read(avatarUrl.inputStream()).roundCorners(360*2)

        graphics.color = Color(32, 34, 37)
        graphics.fillRect(0, 0, 800, 600)

        if (user.premium) {
            val vipString = when (user.premiumType!!) {
                1 -> "VIP"
                2 -> "VIP+"
                3 -> "VIP++"
                else -> "None"
            }

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString(vipString, 60, 65)

            graphics.drawImage(avatar, width/2 - (256 / 2), 80, 256, 256, null)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString(asDiscordUser.username, 390, 50)

            graphics.color = Color.WHITE
            graphics.drawCenteredString(user.bio, 455, 30)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("Souls", 530, 50)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("${user.coins}", 585, 50)
        } else {
            graphics.drawImage(avatar, width/2 - (256 / 2), 50, 256, 256, null)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString(asDiscordUser.username, 360, 50)

            graphics.color = Color.WHITE
            graphics.drawCenteredString(user.bio, 425, 30)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("Souls", 500, 50)

            graphics.color = Color.decode(user.color)
            graphics.drawCenteredString("${user.coins}", 555, 50)
        }

        drawTextOverlay(image.width, image.height, graphics, user)

        // End graphics manipulation
        graphics.dispose()

        return image
    }

    private suspend fun drawTextOverlay(width: Int, height: Int, graphics: Graphics2D, user: User) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        val g2 = image.createGraphics()

        val asDiscordUser = konnor.client.getUser(Snowflake(user.userId))!!

        if (user.premium) {
            val vipString = when (user.premiumType!!) {
                1 -> "VIP"
                2 -> "VIP+"
                3 -> "VIP++"
                else -> "None"
            }

            g2.color = Color.WHITE
            g2.drawCenteredString(vipString, 60, 65)

            g2.color = Color.WHITE
            g2.drawCenteredString(asDiscordUser.username, 390, 50)

            g2.color = Color.BLACK
            g2.drawCenteredString(user.bio, 455, 30)

            g2.color = Color.WHITE
            g2.drawCenteredString("Souls", 530, 50)

            g2.color = Color.WHITE
            g2.drawCenteredString("${user.coins}", 585, 50)
        } else {
            g2.color = Color.WHITE
            g2.drawCenteredString(asDiscordUser.username, 360, 50)

            g2.color = Color.BLACK
            g2.drawCenteredString(user.bio, 425, 30)

            g2.color = Color.WHITE
            g2.drawCenteredString("Souls", 500, 50)

            g2.color = Color.WHITE
            g2.drawCenteredString("${user.coins}", 555, 50)
        }

        g2.dispose()

        graphics.drawImage(CONVOLVE_OP.filter(image, null), 0, 0, null)
    }

    private fun Graphics2D.drawCenteredString(str: String, y: Int, px: Int? = 10, family: String? = "arial", style: Int? = Font.PLAIN) {
        val font = Font(family, style!!, px!!)

        this.font = font

        val fm = fontMetrics
        val x = (width - fm.stringWidth(str)) / 2

        return this.drawString(str, x, y)
    }

    private fun BufferedImage.roundCorners(radius: Int): BufferedImage {
        val w = this.width
        val h = this.height
        val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

        val g2 = output.createGraphics()

        g2.composite = AlphaComposite.Src
        g2.color = Color.WHITE
        g2.fill(RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), radius.toFloat(), radius.toFloat()))
        g2.composite = AlphaComposite.SrcAtop
        g2.drawImage(this, 0, 0, null)
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )

        g2.dispose()

        return output
    }

    private fun Graphics2D.enableFontAntiAliasing(): Graphics2D {
        this.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        return this
    }
}