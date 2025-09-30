package net.sbo.mod.utils.events.impl.guis

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext

/**
 * Event fired after a GUI screen has been rendered.
 * @param client The Minecraft client instance.
 * @param screen The screen that has been rendered.
 * @param context The drawing context used for rendering.
 * @param mouseX The current X position of the mouse.
 * @param mouseY The current Y position of the mouse.
 * @param delta The partial tick time.
 */
class GuiPostRenderEvent(
    val client: MinecraftClient,
    val screen: Screen,
    val context: DrawContext,
    val mouseX: Int,
    val mouseY: Int,
    val delta: Float
)