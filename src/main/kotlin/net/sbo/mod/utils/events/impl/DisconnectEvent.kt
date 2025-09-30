package net.sbo.mod.utils.events.impl

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler

/**
 * Called when the client disconnects from a server.
 * @param handler The ClientPlayNetworkHandler instance.
 * @param mc The MinecraftClient instance.
 */
class DisconnectEvent(val handler: ClientPlayNetworkHandler, val mc: MinecraftClient)