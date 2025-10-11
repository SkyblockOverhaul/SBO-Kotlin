package net.sbo.mod.utils.overlay

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.font.TextRenderer

import java.awt.Color

class OverlayTextLine(
    var text: String,
    var shadow: Boolean = true,
    var linebreak: Boolean = true
) {
    var mouseEnterAction: (() -> Unit)? = null
    var mouseLeaveAction: (() -> Unit)? = null
    var hoverAction: ((drawContext: DrawContext, textRenderer: TextRenderer) -> Unit)? = null
    var clickAction: (() -> Unit)? = null
    var isHovered: Boolean = false
    var x: Int = 0
    var y: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    var renderDebugBox: Boolean = false
    private var condition: () -> Boolean = { true }

    fun setCondition(condition: () -> Boolean): OverlayTextLine {
        this.condition = condition
        return this
    }

    fun checkCondition(): Boolean {
        return condition()
    }

    /**
     * Executes the mouse enter action when the mouse enters the text line.
     * @param action The action to execute when the mouse enters the text line.
     */
    fun onMouseEnter(action: () -> Unit): OverlayTextLine {
        mouseEnterAction = action
        return this
    }

    /**
     * Executes the mouse leave action when the mouse leaves the text line.
     * @param action The action to execute when the mouse leaves the text line.
     */
    fun onMouseLeave(action: () -> Unit): OverlayTextLine {
        mouseLeaveAction = action
        return this
    }

    /**
     * Executes the hover action when the mouse is over the text line.
     * warning: This action is executed in the render loop,
     * so it should be lightweight to avoid performance issues.
     * @param action The action to execute when the mouse hovers over the text line.
     */
    fun onHover(action: (drawContext: DrawContext, textRenderer: TextRenderer) -> Unit): OverlayTextLine {
        hoverAction = action
        return this
    }

    /**
     * Executes the click action when the text line is clicked.
     * @param action The action to execute when the text line is clicked.
     */
    fun onClick(action: () -> Unit): OverlayTextLine {
        clickAction = action
        return this
    }

    private fun mouseEnter() {
        mouseEnterAction?.invoke()
    }

    private fun mouseLeave() {
        mouseLeaveAction?.invoke()
    }

    private fun hover(drawContext: DrawContext, textRenderer: TextRenderer) {
        if (isHovered) hoverAction?.invoke(drawContext, textRenderer)
    }

    fun lineClicked(mouseX: Double, mouseY: Double, x: Float, y: Float, textRenderer: TextRenderer, scale: Float) {
        if (text.isEmpty() || clickAction == null) return
        if (isMouseOver(mouseX, mouseY, x, y, textRenderer, scale)) {
            clickAction?.invoke()
        }
    }

    private fun isMouseOver(mouseX: Double, mouseY: Double, x: Float, y: Float, textRenderer: TextRenderer, scale: Float): Boolean {
        val textWidth = textRenderer.getWidth(text) * scale
        val textHeight = (textRenderer.fontHeight + 1) * scale - 1

        return mouseX >= x && mouseX <= x + textWidth && mouseY >= y && mouseY <= y + textHeight
    }

    fun updateMouseInteraction(mouseX: Double, mouseY: Double, x: Float, y: Float, textRenderer: TextRenderer, scale: Float, drawContext: DrawContext) {
        if (text.isEmpty()) return
        if (mouseEnterAction == null && mouseLeaveAction == null && hoverAction == null) {
            return
        }
        val wasHovered = isHovered
        val isNowHovered = isMouseOver(mouseX, mouseY, x, y, textRenderer, scale)
        isHovered = isNowHovered

        if (isNowHovered && !wasHovered) {
            mouseEnter()
        } else if (!isNowHovered && wasHovered) {
            mouseLeave()
        }

        if (isNowHovered) {
            hover(drawContext, textRenderer)
        }
    }

    fun draw(drawContext: DrawContext, x: Int, y: Int, textRenderer: TextRenderer) {
        if (text.isEmpty()) return

        this.x = x
        this.y = y
        this.width = textRenderer.getWidth(text)
        this.height = textRenderer.fontHeight

        if (renderDebugBox) {
            drawContext.fill(x, y, x + width, y + height + 1, Color(128, 128, 128, 130).rgb)
        }

        drawContext.drawText(textRenderer, text, x, y, -1, shadow)
    }
}