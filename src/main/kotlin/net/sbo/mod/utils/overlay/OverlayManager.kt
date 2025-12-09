package net.sbo.mod.utils.overlay

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.game.World
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.render.RenderEvent
import net.sbo.mod.utils.events.impl.guis.GuiMouseClickAfter
//#if MC >= 1.21.7
//$$ import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
//#else
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
//#endif

object OverlayManager {
    val overlays = mutableListOf<Overlay>()

    fun init() {
//        example:
//        val testOverlay = Overlay("test1",50.0f, 10.0f, 2.0f, "both")
//        val textline = OverlayTextLine("Enjoy using SBO-Kotlin!")
//        textline.onHover { drawContext, textRenderer ->
//            val scaleFactor = mc.window.scaleFactor
//            val mouseX = mc.mouse.x / scaleFactor
//            val mouseY = mc.mouse.y / scaleFactor
//            RenderUtils2D.drawHoveringString(drawContext, "this is hovered text", mouseX, mouseY, textRenderer, testOverlay.scale)
//        }

        registerRenderer()

        Register.command("sboguis", "sbomoveguis", "sbomove") {
            mc.send {
                mc.setScreen(OverlayEditScreen())
            }
        }
    }

    @SboEvent
    fun onMouseClickAfter(event: GuiMouseClickAfter) {
        if (event.screen !is OverlayEditScreen && event.button == 0) {
            overlays.forEach { it.overlayClicked(event.mouseX, event.mouseY) }
        }
    }

    fun render(drawContext: DrawContext, renderScreen: String = "") {
        if (!World.isInSkyblock()) return
        val scaleFactor = mc.window.scaleFactor
        val mouseX = mc.mouse.x / scaleFactor
        val mouseY = mc.mouse.y / scaleFactor
        for (overlay in overlays.toList()) {
            if (renderScreen == "")
                overlay.render(drawContext, mouseX, mouseY)
        }
    }

    fun postRender(drawContext: DrawContext, renderScreen: Screen) {
        if (!World.isInSkyblock()) return
        val scaleFactor = mc.window.scaleFactor
        val mouseX = mc.mouse.x / scaleFactor
        val mouseY = mc.mouse.y / scaleFactor
        for (overlay in overlays.toList()) {
            if (renderScreen.title.string in overlay.allowedGuis)
                overlay.render(drawContext, mouseX, mouseY)
        }
    }

    fun registerRenderer() {
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            ScreenEvents.afterRender(screen).register { renderScreen, drawContext, mouseX, mouseY, tickDelta ->
                if (renderScreen !is OverlayEditScreen) {
                    postRender(drawContext, renderScreen)
                }
            }
        }
    }

    @SboEvent
    fun onRender(event: RenderEvent) {
        render(event.context,  mc.currentScreen?.title?.string ?: "")
    }
}