package net.sbo.mod.utils.render

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

object RenderUtils2D {
    /**
     * Draws a string with a background, positioned at the mouse coordinates.
     * This is useful for creating tooltips.
     *
     * @param drawContext The DrawContext instance for rendering.
     * @param text The string to be drawn.
     * @param x The x-coordinate of the mouse.
     * @param y The y-coordinate of the mouse.
     * @param textRenderer The TextRenderer instance for getting text dimensions.
     * @param padding The padding around the text inside the background box.
     */
    fun drawHoveringString(
        drawContext: DrawContext,
        text: String,
        x: Double,
        y: Double,
        textRenderer: TextRenderer,
        scale: Float = 1.0f,
        padding: Int = 2
    ) {
        if (text.isEmpty()) return

        val textWidth = textRenderer.getWidth(text)
        val textHeight = textRenderer.fontHeight

        val rectX1 = (x / scale).toInt()
        val rectY1 = (y / scale).toInt()

        val backgroundColor = 0x80404040.toInt() // Semi-transparent gray
        val borderColor = 0x80202020.toInt() // Semi-transparent dark gray
        val textLines = text.split("\n").map { Text.of(it) }

        drawContext.matrices.push()
        drawContext.matrices.translate(0.0, 0.0, 400.0)

        drawContext.drawTooltip(textRenderer, textLines, x.toInt(), y.toInt())

        drawContext.matrices.pop()
    }
}