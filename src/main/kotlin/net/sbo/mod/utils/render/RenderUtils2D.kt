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

        val textLines = text.split("\n").map { Text.of(it) }

        //#if MC >= 1.21.7
        //$$ drawContext.matrices.pushMatrix()
        //#else
        drawContext.matrices.push()
        //#endif

        drawContext.drawTooltip(textRenderer, textLines, x.toInt(), y.toInt())

        //#if MC >= 1.21.7
        //$$ drawContext.matrices.popMatrix()
        //#else
        drawContext.matrices.pop()
        //#endif
    }
}
