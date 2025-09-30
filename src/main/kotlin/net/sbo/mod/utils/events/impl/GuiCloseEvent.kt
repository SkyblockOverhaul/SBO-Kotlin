package net.sbo.mod.utils.events.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen

/**
 * Event fired when a GUI screen is closed.
 * @param client The Minecraft client instance.
 * @param screen The screen that is being closed.
 * @param scaledWidth The width of the screen in scaled pixels.
 * @param scaledHeight The height of the screen in scaled pixels.
 */
class GuiCloseEvent(
    val client: MinecraftClient,
    val screen: Screen,
    val scaledWidth: Int,
    val scaledHeight: Int
)