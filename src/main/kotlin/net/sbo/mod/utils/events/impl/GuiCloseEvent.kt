package net.sbo.mod.utils.events.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen

class GuiCloseEvent(
    val client: MinecraftClient,
    val screen: Screen,
    val scaledWidth: Int,
    val scaledHeight: Int
)