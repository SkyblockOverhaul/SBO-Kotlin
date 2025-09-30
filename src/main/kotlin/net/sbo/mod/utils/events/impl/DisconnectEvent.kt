package net.sbo.mod.utils.events.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler

class DisconnectEvent(val handler: ClientPlayNetworkHandler, val mc: MinecraftClient)