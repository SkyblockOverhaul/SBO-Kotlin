package net.sbo.mod.diana.burrows

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.util.math.BlockPos
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.PacketReceiveEvent
import net.sbo.mod.utils.events.impl.PacketSendEvent
import net.sbo.mod.utils.events.impl.PlayerInteractEvent
import net.sbo.mod.utils.game.World
import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.utils.events.impl.WorldChangeEvent
import net.minecraft.particle.ParticleTypes as MCParticleTypes
import net.sbo.mod.utils.waypoint.Waypoint
import net.sbo.mod.utils.waypoint.WaypointManager
import net.sbo.mod.utils.waypoint.WaypointManager.getGuessWaypoints
import net.sbo.mod.utils.waypoint.WaypointManager.guessWp
import net.sbo.mod.utils.waypoint.WaypointManager.removeWaypoint
import java.awt.Color
import java.util.regex.Pattern
import net.minecraft.particle.ParticleTypes as MCParticleTypes

object BurrowDetector {
    internal var lastInteractedPos: BlockPos? = null
    internal val burrows = mutableMapOf<String, Burrow>()
    internal var removePos: SboVec = SboVec(0.0, 0.0, 0.0)
    internal val burrowsHistory = EvictingQueue<String>(2)

    fun init() {
        Register.command("sboclearburrows", "sbocb") {
            resetBurrows()
            Chat.chat("§6[SBO] §4Burrow Waypoints Cleared!")
        }

        Register.onChatMessageCancelable(
            Pattern.compile(
                "^§eYou (.*?) Griffin [Bb]urrow(.*?)$",
                Pattern.DOTALL
            )
        ) { message, matchResult ->
            if (!Diana.dianaBurrowDetect) return@onChatMessageCancelable true
            refreshBurrows()
            true
        }


        Register.onChatMessageCancelable(Pattern.compile("^ ☠ You (.*?)$", Pattern.DOTALL)) { message, matchResult ->
            if (World.getWorld() != "Hub") return@onChatMessageCancelable true
            refreshBurrows()
            true
        }
    }

    @SboEvent
    fun onWorldChange(event: WorldChangeEvent) {
        if (!Diana.dianaBurrowDetect) return
        resetBurrows()
    }

    @SboEvent
    fun onParticleReceive(event: PacketReceiveEvent) {
        val packet = event.packet
        if (packet !is ParticleS2CPacket) return
        if (!Diana.dianaBurrowDetect) return
        if (World.getWorld() != "Hub") return

        if (packet.parameters.type == MCParticleTypes.LARGE_SMOKE && packet.speed == 0.01f && packet.offsetX == 0.0f && packet.offsetY == 0.0f && packet.offsetZ == 0.0f) {
            val pos = SboVec(packet.x, packet.y, packet.z).roundLocationToBlock().down(1.0)
            WaypointManager.removeWaypointAt(pos, "burrow")
            WaypointManager.removeWaypointAt(pos, "inq")
        }
        burrowDetect(packet)
    }

    @SboEvent
    fun onPlayerActionSend(event: PacketSendEvent) {
        val packet = event.packet
        if (packet !is PlayerActionC2SPacket) return
        if (!Diana.dianaBurrowDetect) return
        if (World.getWorld() != "Hub") return

        if (packet.action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return

        val pos = packet.pos
        val posString = "${pos.x} ${pos.y} ${pos.z}"

        if (burrows.containsKey(posString)) {
            removePos = SboVec(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            lastInteractedPos = pos
        }
    }

    @SboEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!Diana.dianaBurrowDetect) return
        if (World.getWorld() != "Hub") return
        val action = event.action
        val pos = event.pos

        if (action != "useBlock") return
        if (pos == null) return

        val posString = "${pos.x} ${pos.y} ${pos.z}"

        if (burrows.containsKey(posString)) {
            removePos = SboVec(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            lastInteractedPos = pos
        }
    }

    private fun getRGB(type: String): Color {
        return when (type) {
            "Start" -> Color(Customization.StartColor)
            "Mob" -> Color(Customization.MobColor)
            "Treasure" -> Color(Customization.TreasureColor)
            else -> Color(255, 255, 255)
        }
    }

    private fun burrowDetect(packet: ParticleS2CPacket) {
        val particleType = ParticleTypes.getParticleType(packet) ?: return
        val pos = SboVec(packet.x, packet.y - 1.0, packet.z).roundLocationToBlock()
        val posString = "${pos.x.toInt()} ${pos.y.toInt()} ${pos.z.toInt()}"

        if (burrowsHistory.contains(posString)) return
        if (!burrows.containsKey(posString)) burrows[posString] = Burrow(pos)

        when (particleType) {
            "FOOTSTEP" -> burrows[posString]?.hasFootstep = true
            "ENCHANT" -> burrows[posString]?.hasEnchant = true
            "EMPTY" -> burrows[posString]?.type = "Start"
            "MOB" -> burrows[posString]?.type = "Mob"
            "TREASURE" -> burrows[posString]?.type = "Treasure"
        }

        val burrow = burrows[posString]
        if (burrow?.type != null && burrow.waypoint == null) {
            burrowsHistory.add(posString)
            val color = getRGB(burrow.type!!)
            burrow.waypoint = Waypoint(
                burrow.type!!,
                pos.x, pos.y, pos.z,
                color.red.toFloat() / 255, color.green.toFloat() / 255, color.blue.toFloat() / 255,
                type = "burrow"
            )
            WaypointManager.addWaypoint(burrow.waypoint!!)
            if (Diana.dianaMultiBurrowGuess) {
                getGuessWaypoints().removeIf { waypoint ->
                    if (waypoint.pos.distanceTo(pos) < 2) {
                        waypoint.hide()
                        removeWaypoint(waypoint)
                        true
                    } else false
                }
            }
        }
    }

    fun refreshBurrows() {
        WaypointManager.removeWaypointAt(removePos, "burrow")
        val playerPos = Player.getLastPosition()
        if (guessWp != null && guessWp!!.pos.distanceTo(playerPos) < 4) {
            guessWp?.hide()
        }

        if (!Diana.dianaMultiBurrowGuess) return
        val removedGuesses = mutableListOf<Waypoint>()
        getGuessWaypoints().forEach { waypoint ->
            if (waypoint.pos.distanceTo(playerPos) < 4) {
                removedGuesses.add(waypoint)
                guessWp?.hide()
            }
        }

        removedGuesses.forEach { waypoint ->
            removeWaypoint(waypoint)
        }
    }

    fun resetBurrows() {
        WaypointManager.removeAllOfType("burrow")
        WaypointManager.removeAllOfType("guess")
        burrows.clear()
        burrowsHistory.clear()
    }
}