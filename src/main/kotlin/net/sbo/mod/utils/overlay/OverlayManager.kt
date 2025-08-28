package net.sbo.mod.utils.overlay

import net.minecraft.util.Identifier
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.game.World

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
        registerMouseLeftClick()

        Register.command("sboguis", "sbomoveguis", "sbomove") {
            mc.send {
                mc.setScreen(OverlayEditScreen())
            }
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
            ScreenEvents.beforeRender(screen).register { renderScreen, drawContext, mouseX, mouseY, tickDelta ->
                if (renderScreen !is OverlayEditScreen) {
                    postRender(drawContext, renderScreen)
                }
            }
        }
        HudLayerRegistrationCallback.EVENT.register(HudLayerRegistrationCallback { layeredDrawer ->
            layeredDrawer.attachLayerAfter(IdentifiedLayer.MISC_OVERLAYS, Identifier.of("sbo-kotlin", "overlay_renderer")) { context, tickCounter ->
                if (Helper.currentScreen is OverlayEditScreen) return@attachLayerAfter
                val renderScreen = mc.currentScreen?.title?.string ?: ""
                render(context, renderScreen)
            }
        })
    }

    fun registerMouseLeftClick() {
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            ScreenMouseEvents.afterMouseClick(screen).register { clickedScreen, mouseX, mouseY, button ->
                if (clickedScreen !is OverlayEditScreen && button == 0) {
                    for (overlay in overlays) {
                        overlay.overlayClicked(mouseX, mouseY)
                    }
                }
            }
        }
    }
}