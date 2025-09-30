package net.sbo.mod.utils.events.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld

/**
 * Called when the world is changed (e.g., when joining a new server or switching dimensions).
 * @param mc The Minecraft client instance.
 * @param world The new ClientWorld instance.
 */
class WorldChangeEvent(val mc: MinecraftClient, val world: ClientWorld)