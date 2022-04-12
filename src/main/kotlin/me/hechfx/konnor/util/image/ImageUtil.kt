package me.hechfx.konnor.util.image

import java.awt.*
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.*


object ImageUtil {

    /**
     * Rounds the border of an image.
     * @return A BufferedImage.
     * @param radius The radius of the border.
     */
    fun BufferedImage.roundCorners(radius: Int): BufferedImage {
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

    fun Graphics2D.applyTransparency(value: Float) {
        val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, value)
        return this.setComposite(ac)
    }

    fun Image.toBufferedImage(): BufferedImage {
        if (this is BufferedImage) {
            return this
        }

        // Create a buffered image with transparency
        val bimage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        // Draw the image on to the buffered image
        val bGr = bimage.createGraphics()
        bGr.drawImage(this, 0, 0, null)
        bGr.dispose()

        // Return the buffered image
        return bimage
    }

    private fun createBuffered(pWidth: Int, pHeight: Int, pType: Int, pTransparency: Int): BufferedImage? {
        return createBuffered(pWidth, pHeight, pType, pTransparency, DEFAULT_CONFIGURATION)
    }

    private val BI_TYPE_ANY = -1

    private var VM_SUPPORTS_ACCELERATION = false

    fun createBuffered(
        pWidth: Int, pHeight: Int, pType: Int, pTransparency: Int,
        pConfiguration: GraphicsConfiguration?
    ): BufferedImage? {
        if (VM_SUPPORTS_ACCELERATION && pType == BI_TYPE_ANY) {
            val env = GraphicsEnvironment.getLocalGraphicsEnvironment()
            if (supportsAcceleration(env)) {
                return getConfiguration(pConfiguration)?.createCompatibleImage(pWidth, pHeight, pTransparency)
            }
        }
        return BufferedImage(pWidth, pHeight, getImageType(pType, pTransparency))
    }

    private fun getConfiguration(pConfiguration: GraphicsConfiguration?): GraphicsConfiguration? {
        return pConfiguration ?: DEFAULT_CONFIGURATION
    }

    private fun getDefaultGraphicsConfiguration(): GraphicsConfiguration? {
        try {
            val env = GraphicsEnvironment.getLocalGraphicsEnvironment()
            if (!env.isHeadlessInstance) {
                return env.defaultScreenDevice.defaultConfiguration
            }
        } catch (e: LinkageError) {
            // Means we are not in a 1.4+ VM, so skip testing for headless again
            VM_SUPPORTS_ACCELERATION = false
        }
        return null
    }

    private val DEFAULT_CONFIGURATION: GraphicsConfiguration? = getDefaultGraphicsConfiguration()

    private fun getImageType(pType: Int, pTransparency: Int): Int {
        // TODO: Handle TYPE_CUSTOM?
        return if (pType != BI_TYPE_ANY) {
            pType
        } else {
            when (pTransparency) {
                Transparency.OPAQUE -> BufferedImage.TYPE_INT_RGB
                Transparency.BITMASK, Transparency.TRANSLUCENT -> BufferedImage.TYPE_INT_ARGB
                else -> throw IllegalArgumentException("Unknown transparency type: $pTransparency")
            }
        }
    }

    private fun supportsAcceleration(pEnv: GraphicsEnvironment): Boolean {
        VM_SUPPORTS_ACCELERATION = try {
            // Acceleration only supported in non-headless environments, on 1.4+ VMs
            return  /*VM_SUPPORTS_ACCELERATION &&*/!pEnv.isHeadlessInstance
        } catch (ignore: LinkageError) {
            // Means we are not in a 1.4+ VM, so skip testing for headless again
            false
        }

        // If the invocation fails, assume no accelleration is possible
        return false
    }

}


/**
 * This class implements a convolution from the source
 * to the destination.
 *
 * @author [Harald Kuhr](mailto:harald.kuhr@gmail.com)
 * @author last modified by $Author: haku $
 * @version $Id: //depot/branches/personal/haraldk/twelvemonkeys/release-2/twelvemonkeys-core/src/main/java/com/twelvemonkeys/image/ConvolveWithEdgeOp.java#1 $
 *
 * @see java.awt.image.ConvolveOp
 */
class ConvolveWithEdgeOp @JvmOverloads constructor(
    pKernel: Kernel,
    pEdgeCondition: Int = EDGE_ZERO_FILL,
    pHints: RenderingHints? = null
) :
    BufferedImageOp, RasterOp {
    private val kernel: Kernel

    /**
     * Returns the edge condition.
     * @return the edge condition of this `ConvolveOp`.
     * @see .EDGE_NO_OP
     *
     * @see .EDGE_ZERO_FILL
     *
     * @see .EDGE_REFLECT
     *
     * @see .EDGE_WRAP
     */
    val edgeCondition: Int
    private val convolve: ConvolveOp

    init {
        // Create convolution operation
        val edge: Int
        edge =
            when (pEdgeCondition) {
                EDGE_REFLECT, EDGE_WRAP -> ConvolveOp.EDGE_NO_OP
                else -> pEdgeCondition
            }
        kernel = pKernel
        edgeCondition = pEdgeCondition
        convolve = ConvolveOp(pKernel, edge, pHints)
    }

    override fun filter(pSource: BufferedImage?, pDestination: BufferedImage?): BufferedImage {
        if (pSource == null) {
            throw NullPointerException("source image is null")
        }
        require(!(pSource === pDestination)) { "source image cannot be the same as the destination image" }
        val borderX = kernel.width / 2
        val borderY = kernel.height / 2
        val original = addBorder(pSource, borderX, borderY)

        // Workaround for what seems to be a Java2D bug:
        // ConvolveOp needs explicit destination image type for some "uncommon"
        // image types. However, TYPE_3BYTE_BGR is what javax.imageio.ImageIO
        // normally returns for color JPEGs... :-/
        var destination = pDestination
        if (original.type == BufferedImage.TYPE_3BYTE_BGR) {
            destination = ImageUtil.createBuffered(
                pSource.width, pSource.height,
                pSource.type, pSource.colorModel.transparency,
                null
            )!!
        }

        // Do the filtering (if destination is null, a new image will be created)
        destination = convolve.filter(original, destination)
        if (pSource !== original) {
            // Remove the border
            destination = destination.getSubimage(borderX, borderY, pSource.width, pSource.height)
        }
        return destination
    }

    private fun addBorder(pOriginal: BufferedImage, pBorderX: Int, pBorderY: Int): BufferedImage {
        if (edgeCondition and 2 == 0) {
            return pOriginal
        }

        // TODO: Might be faster if we could clone raster and stretch it...
        val w = pOriginal.width
        val h = pOriginal.height
        val cm = pOriginal.colorModel
        val raster = cm.createCompatibleWritableRaster(w + 2 * pBorderX, h + 2 * pBorderY)
        val bordered = BufferedImage(cm, raster, cm.isAlphaPremultiplied, null)
        val g = bordered.createGraphics()
        try {
            g.composite = AlphaComposite.Src
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE)

            // Draw original in center
            g.drawImage(pOriginal, pBorderX, pBorderY, null)
            when (edgeCondition) {
                EDGE_REFLECT -> {
                    // Top/left (empty)
                    g.drawImage(pOriginal, pBorderX, 0, pBorderX + w, pBorderY, 0, 0, w, 1, null) // Top/center
                    // Top/right (empty)
                    g.drawImage(
                        pOriginal,
                        -w + pBorderX,
                        pBorderY,
                        pBorderX,
                        h + pBorderY,
                        0,
                        0,
                        1,
                        h,
                        null
                    ) // Center/left
                    // Center/center (already drawn)
                    g.drawImage(
                        pOriginal,
                        w + pBorderX,
                        pBorderY,
                        2 * pBorderX + w,
                        h + pBorderY,
                        w - 1,
                        0,
                        w,
                        h,
                        null
                    ) // Center/right

                    // Bottom/left (empty)
                    g.drawImage(
                        pOriginal,
                        pBorderX,
                        pBorderY + h,
                        pBorderX + w,
                        2 * pBorderY + h,
                        0,
                        h - 1,
                        w,
                        h,
                        null
                    ) // Bottom/center
                }
                EDGE_WRAP -> {
                    g.drawImage(pOriginal, -w + pBorderX, -h + pBorderY, null) // Top/left
                    g.drawImage(pOriginal, pBorderX, -h + pBorderY, null) // Top/center
                    g.drawImage(pOriginal, w + pBorderX, -h + pBorderY, null) // Top/right
                    g.drawImage(pOriginal, -w + pBorderX, pBorderY, null) // Center/left
                    // Center/center (already drawn)
                    g.drawImage(pOriginal, w + pBorderX, pBorderY, null) // Center/right
                    g.drawImage(pOriginal, -w + pBorderX, h + pBorderY, null) // Bottom/left
                    g.drawImage(pOriginal, pBorderX, h + pBorderY, null) // Bottom/center
                    g.drawImage(pOriginal, w + pBorderX, h + pBorderY, null) // Bottom/right
                }
                else -> throw IllegalArgumentException("Illegal edge operation $edgeCondition")
            }
        } finally {
            g.dispose()
        }
        return bordered
    }

    override fun filter(pSource: Raster, pDestination: WritableRaster): WritableRaster {
        return convolve.filter(pSource, pDestination)
    }

    override fun createCompatibleDestImage(pSource: BufferedImage, pDesinationColorModel: ColorModel): BufferedImage {
        return convolve.createCompatibleDestImage(pSource, pDesinationColorModel)
    }

    override fun createCompatibleDestRaster(pSource: Raster): WritableRaster {
        return convolve.createCompatibleDestRaster(pSource)
    }

    override fun getBounds2D(pSource: BufferedImage): Rectangle2D {
        return convolve.getBounds2D(pSource)
    }

    override fun getBounds2D(pSource: Raster): Rectangle2D {
        return convolve.getBounds2D(pSource)
    }

    override fun getPoint2D(pSourcePoint: Point2D, pDestinationPoint: Point2D): Point2D {
        return convolve.getPoint2D(pSourcePoint, pDestinationPoint)
    }

    override fun getRenderingHints(): RenderingHints {
        return convolve.renderingHints
    }

    fun getKernel(): Kernel {
        return convolve.kernel
    }

    companion object {
        /**
         * Alias for [ConvolveOp.EDGE_ZERO_FILL].
         * @see .EDGE_REFLECT
         */
        const val EDGE_ZERO_FILL = ConvolveOp.EDGE_ZERO_FILL

        /**
         * Alias for [ConvolveOp.EDGE_NO_OP].
         * @see .EDGE_REFLECT
         */
        const val EDGE_NO_OP = ConvolveOp.EDGE_NO_OP

        /**
         * Adds a border to the image while convolving. The border will reflect the
         * edges of the original image. This is usually a good default.
         * Note that while this mode typically provides better quality than the
         * standard modes `EDGE_ZERO_FILL` and `EDGE_NO_OP`, it does so
         * at the expense of higher memory consumption and considerable more computation.
         */
        const val EDGE_REFLECT = 2 // as JAI BORDER_REFLECT

        /**
         * Adds a border to the image while convolving. The border will wrap the
         * edges of the original image. This is usually the best choice for tiles.
         * Note that while this mode typically provides better quality than the
         * standard modes `EDGE_ZERO_FILL` and `EDGE_NO_OP`, it does so
         * at the expense of higher memory consumption and considerable more computation.
         * @see .EDGE_REFLECT
         */
        const val EDGE_WRAP = 3 // as JAI BORDER_WRAP
    }
}