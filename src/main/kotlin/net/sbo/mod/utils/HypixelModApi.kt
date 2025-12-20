package net.sbo.mod.utils

import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.error.ErrorReason
import net.hypixel.modapi.fabric.event.HypixelModAPICallback
import net.hypixel.modapi.fabric.event.HypixelModAPIErrorCallback
import net.hypixel.modapi.packet.ClientboundHypixelPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import net.sbo.mod.partyfinder.PartyFinderManager
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.game.DisconnectEvent
import kotlin.jvm.optionals.getOrNull

object HypixelModApi {
    var isOnHypixel: Boolean = false
    var isOnSkyblock: Boolean = false
    var isLeader: Boolean = false
    var isInParty: Boolean = false
    var partyMembers: List<String> = emptyList()
    var mode: String = ""

    // listeners
    private val partyInfoListeners = mutableListOf<(isInParty: Boolean, isLeader: Boolean, members: List<String>) -> Unit>()
    private val errorListeners = mutableListOf<(packetId: String) -> Unit>()


    fun init() {
        HypixelModAPICallback.EVENT.register { handlePacket(it) }
        HypixelModAPIErrorCallback.EVENT.register { id, reason -> onErrorPacket(id, reason) }
    }

    @SboEvent
    fun onDisconnect(event: DisconnectEvent) {
        isOnHypixel = false
        isOnSkyblock = false
        isLeader = false
        isInParty = false
        partyMembers = emptyList()
        mode = ""
    }

    private fun handlePacket(packet: ClientboundHypixelPacket) {
        when (packet) {
            is ClientboundHelloPacket -> onHelloPacket(packet)
            is ClientboundLocationPacket -> onLocationUpdatePacket(packet)
            is ClientboundPartyInfoPacket -> onPartyInfoPacket(packet)
            else -> {}
        }
    }

    private fun onLocationUpdatePacket(packet: ClientboundLocationPacket) {
        isOnSkyblock = packet.serverType?.getOrNull()?.name == "SKYBLOCK"
        mode = packet.mode.orElse("")
    }

    private fun onHelloPacket(packet: ClientboundHelloPacket) {
        isOnHypixel = true
        sendPartyInfoPacket()
    }

    private fun onPartyInfoPacket(packet: ClientboundPartyInfoPacket) {
        this.isInParty = packet.isInParty

        val membersList = packet.memberMap?.map { it.key.toString() }?.toMutableList() ?: mutableListOf()
        if (isInParty) {
            val leaderUUID = packet.memberMap?.entries?.find { it.value.toString() == "LEADER" }?.key.toString()

            membersList.remove(leaderUUID)
            membersList.add(0, leaderUUID)

            this.isLeader = packet.memberMap?.get(Player.getUUID())?.toString() == "LEADER"
        } else {
            this.isLeader = true
            membersList.add(Player.getUUIDString())
        }
        this.partyMembers = membersList

        partyInfoListeners.forEach { listener ->
            listener(this.isInParty, this.isLeader, this.partyMembers)
        }
    }

    fun onPartyInfo(listener: (isInParty: Boolean, isLeader: Boolean, members: List<String>) -> Unit) {
        partyInfoListeners.add(listener)
    }

    private fun onErrorPacket(id: String, reason: ErrorReason) {
        if (id == "location") {
            isOnSkyblock = false
            mode = ""
        }

        errorListeners.forEach { listener ->
            listener(id)
        }
    }

    fun onError(listener: (packetId: String) -> Unit) {
        errorListeners.add(listener)
    }

    fun sendPartyInfoPacket(createParty: Boolean = false) {
        try {
            if (isOnHypixel) {
                if (createParty) PartyFinderManager.creatingParty = true
                HypixelModAPI.getInstance().sendPacket(ServerboundPartyInfoPacket())
            } else {
                PartyFinderManager.creatingParty = false
                Chat.chat("§6[SBO] §eYou are not on Hypixel. You can only use this feature on Hypixel.")
            }
        } catch (_: Exception) {
            PartyFinderManager.creatingParty = false
        }
    }
}