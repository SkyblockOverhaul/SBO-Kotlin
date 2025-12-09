package net.sbo.mod.utils.render

import net.fabricmc.fabric.api.client.rendering.v1.*
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.*
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.waypoint.WaypointManager
import net.sbo.mod.mixin.accessor.BeaconBlockEntityRendererInvoker
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.utils.math.SboVec
import java.awt.Color
import kotlin.math.max

object WaypointRenderer : WorldRenderEvents.AfterTranslucent {
    override fun afterTranslucent(context: WorldRenderContext) {
        WaypointManager.renderAllWaypoints(context)
    }
}

object RenderUtil {
    fun renderWaypoint(
        context: WorldRenderContext,
        text: String,
        pos: SboVec,
        colorComponents: FloatArray,
        hexColor: Int,
        alpha: Float,
        throughWalls: Boolean,
        drawLine: Boolean,
        lineWidth: Float,
        renderBeam: Boolean
    ) {
        drawFilledBox(
            context,
            pos,
            1.0,
            1.0,
            1.0,
            colorComponents,
            alpha,
            throughWalls
        )

        if (drawLine) {
            drawLineFromCursor(
                context,
                pos,
                colorComponents,
                lineWidth,
                throughWalls,
                alpha
            )
        }

        if (renderBeam) {
            renderBeaconBeam(
                context,
                pos,
                1,
                colorComponents
            )

        }

        if (text.isNotEmpty() && text != "§7") {
            drawString(
                context,
                pos,
                1.5,
                text,
                hexColor,
                Customization.waypointTextShadow,
                Customization.waypointTextScale/100.0,
                throughWalls
            )
        }
    }

    /**
     * Draws a filled box at the specified world coordinates.
     * @param context The world render context.
     * @param pos The position in the world where the box should be drawn.
     * @param width The width of the box.
     * @param height The height of the box.
     * @param depth The depth of the box.
     * @param colorComponents The RGB color components as a FloatArray (0.0 to 1.0).
     * @param alpha The alpha value for transparency (0.0 to 1.0).
     * @param throughWalls Whether the box should be drawn through walls.
     */
    fun drawFilledBox(
        context: WorldRenderContext,
        pos: SboVec,
        width: Double,
        height: Double,
        depth: Double,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        val matrices = context.matrixStack()
        val cameraPos = context.camera().pos

        matrices!!.push()
        matrices.translate(pos.x + 0.5 - cameraPos.x, pos.y - cameraPos.y, pos.z + 0.5 - cameraPos.z)

        val consumers = context.consumers()!!

        val renderLayer = if (throughWalls) SboRenderLayers.FILLED_BOX_THROUGH_WALLS else SboRenderLayers.FILLED_BOX
        val buffer = consumers.getBuffer(renderLayer)

        val minX = -width / 2.0
        val minZ = -depth / 2.0
        val maxX = width / 2.0
        val maxZ = depth / 2.0

        val minY = 0.0
        val maxY = height

        VertexRendering.drawFilledBox(
            matrices, buffer,
            minX, minY, minZ,
            maxX, maxY, maxZ,
            colorComponents[0], colorComponents[1], colorComponents[2], alpha
        )

        matrices.pop()
    }

    /**
     * Draws a string in the 3D world that always faces the player.
     * @param context The matrix stack for transformations.
     * @param pos The position in the world where the text should be drawn.
     * @param text The text to draw.
     * @param color The color of the text in ARGB format.
     * @param shadow Whether to draw the text with a shadow.
     * @param scale The scale of the text.
     */
    fun drawString(
        context: WorldRenderContext,
        pos: SboVec,
        yOffset: Double,
        text: String,
        color: Int,
        shadow: Boolean,
        scale: Double,
        throughWalls: Boolean
    ) {
        val matrices = context.matrixStack()
        val camera = context.camera()
        val cameraPos = camera.pos
        val cameraYaw = camera.yaw
        val cameraPitch = camera.pitch
        val textRenderer = mc.textRenderer

        matrices!!.push()

        val textWorldPos = Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
        val distance = cameraPos.distanceTo(textWorldPos)
        val dynamicScale = max(distance, 2.5) * scale

        matrices.translate(pos.x + 0.5 - cameraPos.x, pos.y + yOffset + - cameraPos.y, pos.z + 0.5 - cameraPos.z)

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch))

        matrices.scale(-dynamicScale.toFloat(), -dynamicScale.toFloat(), dynamicScale.toFloat())

        val textWidth = textRenderer.getWidth(text)
        val xOffset = -textWidth / 2f

        val consumers = context.consumers()!!

        val layerType = if (throughWalls) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL

        textRenderer.draw(
            text,
            xOffset,
            0f,
            color,
            shadow,
            matrices.peek().positionMatrix,
            consumers,
            layerType,
            0,
            0xF000F0
        )

        matrices.pop()
    }

    /**
     * Renders a beacon beam at the given location.
     * @param context The world render context.
     * @param pos The position in the world where the beacon beam should be rendered.
     * @param colorComponents The RGB color components as a FloatArray (0.0 to
     */
    fun renderBeaconBeam( // todo: add through walls option
        context: WorldRenderContext,
        pos: SboVec,
        yOffset: Int,
        colorComponents: FloatArray,
    ) {
        val matrices = context.matrixStack()!!
        val cameraPos = context.camera().pos
        val world = context.world()

        matrices.push()
        matrices.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z)

        val consumers = context.consumers()!!
        val partialTicks = context.tickCounter().getTickProgress(true)
        val worldAge = world.time

        val beamHeight = context.world().height
        val beamColor = floatArrayOf(colorComponents[0], colorComponents[1], colorComponents[2], 1.0f)


        BeaconBlockEntityRendererInvoker.renderBeam(
            matrices,
            consumers,
            partialTicks,
            1.0f,
            worldAge,
            yOffset,
            beamHeight,
            Color(beamColor[0], beamColor[1], beamColor[2]).rgb
        )

        matrices.pop()
    }

    /**
     * Draws a line from the player's eyes to a target point in the world.
     * @param context The world render context.
     * @param target The target position in the world.
     * @param color The RGB color of the line as a FloatArray (0.0 to 1.0).
     * @param lineWidth The width of the line.
     * @param throughWalls Whether the line should be drawn through walls.
     * @param alpha The alpha value for transparency (0.0 to 1.0).
     */
    fun drawLineFromCursor(
        context: WorldRenderContext,
        target: SboVec,
        color: FloatArray,
        lineWidth: Float,
        throughWalls: Boolean,
        alpha: Float = 0.5f
    ) {
        val camera = context.camera()
        val cameraPos = camera.pos
        val matrices = context.matrixStack()!!

        matrices.push()
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

        val consumers = context.consumers()!!
        val startPos = cameraPos.add(Vec3d.fromPolar(camera.pitch, camera.yaw))
        val endPos = target.center().toVec3d().add(0.0, 0.5, 0.0)

        val lineDir = endPos.subtract(startPos)
        val viewDir = startPos.subtract(cameraPos)

        val sideVec = lineDir.crossProduct(viewDir).normalize()

        val upVec = sideVec.crossProduct(lineDir).normalize()

        val nx = upVec.x.toFloat()
        val ny = upVec.y.toFloat()
        val nz = upVec.z.toFloat()

        val renderLayer = SboRenderLayers.getLines(lineWidth.toDouble(), throughWalls)
        val buffer = consumers.getBuffer(renderLayer)
        val matrixEntry = matrices.peek()

        buffer.vertex(matrixEntry, startPos.x.toFloat(), startPos.y.toFloat(), startPos.z.toFloat())
            .normal(matrixEntry, nx, ny, nz)
            .color(color[0], color[1], color[2], alpha)


        buffer.vertex(matrixEntry, endPos.x.toFloat(), endPos.y.toFloat(), endPos.z.toFloat())
            .normal(matrixEntry, nx, ny, nz)
            .color(color[0], color[1], color[2], alpha)

        matrices.pop()
    }
}