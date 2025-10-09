package net.sbo.mod.utils.render

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

object RenderUtils2D {
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
        val mc = MinecraftClient.getInstance()
        val textLines = text.split("\n").map { Text.of(it) }

        drawContext.matrices.push()
        drawContext.matrices.translate(0.0, 0.0, 400.0)

        if (mc.currentScreen != null) {
            drawContext.drawTooltip(textRenderer, textLines, x.toInt(), y.toInt())
        } else {
            var yOffset = 0
            for (line in textLines) {
                drawContext.drawTextWithShadow(
                    textRenderer,
                    line,
                    (x + 8).toInt(),
                    (y + 8 + yOffset).toInt(),
                    0xFFFFFF
                )
                yOffset += textRenderer.fontHeight + padding
            }
        }
        drawContext.matrices.pop()
    }
}
