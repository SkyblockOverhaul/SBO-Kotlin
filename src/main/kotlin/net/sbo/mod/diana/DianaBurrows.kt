package net.sbo.mod.diana

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.util.math.BlockPos
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.events.Register
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.chat.Chat
import net.minecraft.particle.ParticleTypes as MCParticleTypes
import net.sbo.mod.utils.waypoint.Waypoint
import java.awt.Color
import net.sbo.mod.utils.waypoint.WaypointManager
import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.utils.World
import net.sbo.mod.utils.waypoint.WaypointManager.getGuessWaypoints
import net.sbo.mod.utils.waypoint.WaypointManager.guessWp
import net.sbo.mod.utils.waypoint.WaypointManager.removeWaypoint
import net.sbo.mod.utils.waypoint.WaypointManager.waypoints
import java.util.regex.Pattern

internal class EvictingQueue<T>(internal val maxSize: Int) {
    internal val queue = mutableListOf<T>()

    fun add(item: T) {
        if (queue.size >= maxSize) {
            queue.removeFirst()
        }
        queue.add(item)
    }

    fun contains(item: T): Boolean {
        return queue.contains(item)
    }

    fun clear() {
        queue.clear()
    }
}

internal data class ParticleCheck(val typeCheck: (packet: ParticleS2CPacket) -> Boolean)

internal object ParticleTypes {
    private const val FLOAT_EPSILON = 0.001f

    internal val PARTICLE_CHECKS = mutableMapOf(
        "ENCHANT" to ParticleCheck { packet ->
            packet.parameters.type == MCParticleTypes.ENCHANT &&
            packet.count == 5 &&
            packet.speed == 0.05f &&
            packet.offsetX == 0.5f &&
            packet.offsetY == 0.4f &&
            packet.offsetZ == 0.5f
        },
        "EMPTY" to ParticleCheck { packet ->
            packet.parameters.type == MCParticleTypes.ENCHANTED_HIT &&
            packet.count == 4 &&
            packet.speed == 0.01f &&
            packet.offsetX == 0.5f &&
            packet.offsetY == 0.1f &&
            packet.offsetZ == 0.5f
        },
        "MOB" to ParticleCheck { packet ->
            packet.parameters.type == MCParticleTypes.CRIT &&
            packet.count == 3 &&
            packet.speed == 0.01f &&
            packet.offsetX == 0.5f &&
            packet.offsetY == 0.1f &&
            packet.offsetZ == 0.5f
        },
        "TREASURE" to ParticleCheck { packet ->
            packet.parameters.type == MCParticleTypes.DRIPPING_LAVA &&
            packet.count == 2 &&
            packet.speed == 0.01f &&
            packet.offsetX == 0.35f &&
            packet.offsetY == 0.1f &&
            packet.offsetZ == 0.35f
        },
        "FOOTSTEP" to ParticleCheck { packet ->
            packet.parameters.type == MCParticleTypes.CRIT &&
            packet.count == 1 &&
            packet.speed == 0.0f &&
            packet.offsetX == 0.05f &&
            packet.offsetY == 0.0f &&
            packet.offsetZ == 0.05f
        }
    )

    fun getParticleType(packet: ParticleS2CPacket): String? {
        PARTICLE_CHECKS.forEach { (type, check) ->
            if (check.typeCheck(packet)) {
                return type
            }
        }
        return null
    }
}

internal data class Burrow(
    val pos: SboVec,
    var hasFootstep: Boolean = false,
    var hasEnchant: Boolean = false,
    var type: String? = null,
    var waypoint: Waypoint? = null
)

object BurrowDetector {
    internal var lastInteractedPos: BlockPos? = null
    internal val burrows = mutableMapOf<String, Burrow>()
    internal var removePos: SboVec = SboVec(0.0, 0.0, 0.0)
    internal val burrowsHistory = EvictingQueue<String>(2)

    fun init() {
        Register.onPacketReceived(ParticleS2CPacket::class.java) { packet ->
            if (!Diana.dianaBurrowDetect) return@onPacketReceived
            if (World.getWorld() != "Hub") return@onPacketReceived
            smokeRemove(packet)
            burrowDetect(packet)
        }
        Register.onPacketSent(PlayerActionC2SPacket::class.java) { packet ->
            if (!Diana.dianaBurrowDetect) return@onPacketSent
            if (World.getWorld() != "Hub") return@onPacketSent
            playerDigBlock(packet)
        }
        Register.onWorldChange {
            if (!Diana.dianaBurrowDetect) return@onWorldChange
            resetBurrows()
        }
        Register.onPlayerInteract { action, pos, event ->
            if (!Diana.dianaBurrowDetect) return@onPlayerInteract
            if (World.getWorld() != "Hub") return@onPlayerInteract
            rightClickBlock(action, pos)
        }
        Register.command("sboclearburrows", "sbocb") {
            resetBurrows()
            Chat.chat("§6[SBO] §4Burrow Waypoints Cleared!")
        }

        Register.onChatMessageCancable(Pattern.compile("§eYou dug out a Griffin Burrow! (.*?)", Pattern.DOTALL)) { message, matchResult ->
            if (!Diana.dianaBurrowDetect) return@onChatMessageCancable true
            refreshBurrows()
            true
        }

        Register.onChatMessageCancable(Pattern.compile("§eYou finished the Griffin burrow chain!(.*?)", Pattern.DOTALL)) { message, matchResult ->
            if (!Diana.dianaBurrowDetect) return@onChatMessageCancable true
            refreshBurrows()
            true
        }

        Register.onChatMessageCancable(Pattern.compile(" ☠ You (.*?)", Pattern.DOTALL)) { message, matchResult ->
            if (World.getWorld() != "Hub") return@onChatMessageCancable true
            refreshBurrows()
            true
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
            if(Diana.dianaMultiBurrowGuess) {
                val removedGuesses = mutableListOf<Waypoint>()
                getGuessWaypoints().forEach { waypoint ->
                    if (waypoint.pos.distanceTo(pos) < 2) {
                        waypoint.hide()
                        removedGuesses.add(waypoint)
                    }
                }
                removedGuesses.forEach {
                    removeWaypoint(it)
                }
            }
        }
    }

    fun playerDigBlock(packet: PlayerActionC2SPacket) {
        if (packet.action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return

        val pos = packet.pos
        val posString = "${pos.x} ${pos.y} ${pos.z}"

        if (burrows.containsKey(posString)) {
            removePos = SboVec(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            lastInteractedPos = pos
        }
    }

    fun rightClickBlock(action: String, pos: BlockPos?) {
        if (action != "useBlock") return
        if (pos == null) return

        val posString = "${pos.x} ${pos.y} ${pos.z}"

        if (burrows.containsKey(posString)) {
            removePos = SboVec(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            lastInteractedPos = pos
        }
    }

    fun smokeRemove(packet: ParticleS2CPacket) {
        if (packet.parameters.type == MCParticleTypes.LARGE_SMOKE && packet.speed == 0.01f && packet.offsetX == 0.0f && packet.offsetY == 0.0f && packet.offsetZ == 0.0f) {
            val pos = SboVec(packet.x, packet.y, packet.z).roundLocationToBlock().down(1.0)
            WaypointManager.removeWaypointAt(pos, "burrow")
            WaypointManager.removeWaypointAt(pos, "inq")
        }
    }

    fun refreshBurrows() {
        WaypointManager.removeWaypointAt(removePos, "burrow")
        val playerPos = Player.getLastPosition()
        if (guessWp != null && guessWp!!.pos.distanceTo(playerPos) < 4) {
            guessWp?.hide()
        }

        if(!Diana.dianaMultiBurrowGuess) return
        val removedGuesses = mutableListOf<Waypoint>()
        getGuessWaypoints().forEach { waypoint ->
            if(waypoint.pos.distanceTo(playerPos) < 4) {
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