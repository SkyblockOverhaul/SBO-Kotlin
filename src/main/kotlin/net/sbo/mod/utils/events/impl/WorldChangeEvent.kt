package net.sbo.mod.utils.events.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld

class WorldChangeEvent (val mc: MinecraftClient, val world: ClientWorld)