package net.sbo.mod.utils.overlay

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.sbo.mod.utils.data.SboDataObject
import net.sbo.mod.utils.data.SboDataObject.overlayData
import org.lwjgl.glfw.GLFW
//#if MC >= 1.21.9
//$$ import net.minecraft.client.gui.Click
//$$ import net.minecraft.client.input.KeyInput
//#endif

class OverlayEditScreen : Screen(Text.literal("SBO_Overlay_Editor")) {
    private var selectedOverlay: Overlay? = null
    private var isDragging = false
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        this.renderDarkening(context)
        OverlayManager.render(context)
    }

    //#if MC >= 1.21.9
    //$$ override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
    //$$     val mouseX = click.x
    //$$     val mouseY = click.y
    //$$     val button = click.buttonInfo().button
    //#else
    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
    //#endif
        if (button == 0) {
            selectedOverlay = OverlayManager.overlays.firstOrNull { it.isOverOverlay(mouseX, mouseY) }

            if (selectedOverlay != null) {
                isDragging = true
                lastMouseX = mouseX
                lastMouseY = mouseY

                OverlayManager.overlays.forEach {
                    it.selected = (it == selectedOverlay)
                }
            } else {
                OverlayManager.overlays.forEach { it.selected = false }
            }
            return true
        }
        //#if MC >= 1.21.9
        //$$ return super.mouseClicked(click, doubled)
        //#else
        return super.mouseClicked(mouseX, mouseY, button)
        //#endif
    }

    //#if MC >= 1.21.9
    //$$  override fun mouseDragged(click: Click, deltaX: Double, deltaY: Double): Boolean {
    //#else
    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
    //#endif
        if (isDragging && selectedOverlay != null) {
            selectedOverlay?.x = (selectedOverlay?.x ?: 0f) + deltaX.toFloat()
            overlayData.overlays[selectedOverlay!!.name]?.x = selectedOverlay?.x ?: 0f
            selectedOverlay?.y = (selectedOverlay?.y ?: 0f) + deltaY.toFloat()
            overlayData.overlays[selectedOverlay!!.name]?.y = selectedOverlay?.y ?: 0f
            return true
        }
        //#if MC >= 1.21.9
        //$$ return super.mouseDragged(click, deltaX, deltaY)
        //#else
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
        //#endif
    }

    //#if MC >= 1.21.9
    //$$ override fun mouseReleased(click: Click): Boolean {
    //$$     val mouseX = click.x
    //$$     val mouseY = click.y
    //$$     val button = click.buttonInfo().button
    //#else
    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
    //#endif
        if (button == 0) {
            isDragging = false
            return true
        }
        //#if MC >= 1.21.9
        //$$ return super.mouseReleased(click)
        //#else
        return super.mouseReleased(mouseX, mouseY, button)
        //#endif
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        selectedOverlay?.let {
            it.scale = (it.scale + verticalAmount * 0.1f).coerceIn(0.5, 5.0).toFloat()
            overlayData.overlays[it.name]?.scale = it.scale
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    //#if MC >= 1.21.9
    //$$ override fun keyPressed(keyInput: KeyInput): Boolean {
    //$$    val keyCode = keyInput.key
    //#else
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
    //#endif
        selectedOverlay?.let {
            val step = 1f
            when (keyCode) {
                GLFW.GLFW_KEY_UP -> {
                    it.y -= step
                    overlayData.overlays[it.name]?.y = it.y
                }
                GLFW.GLFW_KEY_DOWN -> {
                    it.y += step
                    overlayData.overlays[it.name]?.y = it.y
                }
                GLFW.GLFW_KEY_LEFT -> {
                    it.x -= step
                    overlayData.overlays[it.name]?.x = it.x
                }
                GLFW.GLFW_KEY_RIGHT -> {
                    it.x += step
                    overlayData.overlays[it.name]?.x = it.x
                }
                //#if MC >= 1.21.9
                //$$ else -> return super.keyPressed(keyInput)
                //#else
                else -> return super.keyPressed(keyCode, scanCode, modifiers)
                //#endif
            }
            return true
        }
        //#if MC >= 1.21.9
        //$$ return super.keyPressed(keyInput)
        //#else
        return super.keyPressed(keyCode, scanCode, modifiers)
        //#endif
    }

    override fun removed() {
        super.removed()
        OverlayManager.overlays.forEach { it.selected = false }
        SboDataObject.save("OverlayData")
    }
}