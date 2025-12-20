package net.sbo.mod.utils.events.impl.game

import net.minecraft.client.MinecraftClient

/**
 * Event triggered on each game tick.
 *
 * @param client The Minecraft client instance.
 */
class TickEvent(val client: MinecraftClient)