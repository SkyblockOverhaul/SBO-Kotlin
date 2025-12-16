package net.sbo.mod.diana.guesses

import net.minecraft.block.Blocks
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.util.math.Box
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleTypes
import net.sbo.mod.SBOKotlin
import net.sbo.mod.diana.burrows.BurrowDetector
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.math.RaycastUtils
import net.sbo.mod.utils.collection.TimeLimitedSet
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.game.PlayerInteractEvent
import net.sbo.mod.utils.events.impl.packets.PacketReceiveEvent
import net.sbo.mod.utils.events.impl.packets.PacketSendEvent
import net.sbo.mod.utils.game.InventoryUtils
import net.sbo.mod.utils.game.World
import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.utils.math.SboVec.Companion.toSboVec
import net.sbo.mod.utils.waypoint.WaypointManager
import java.util.regex.Pattern
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

//todo: when not precise and normal guess was used remove latest arrow guess waypoint

/**
 * A utility object to guess the location of Griffin Burrows based on arrow particle effects.
 *
 * The credits to this object go fully to "SidOfThe7Cs" who made this incredible logic in the Skyhanni Mod:
 *
 * https://github.com/hannibal002/SkyHanni/pull/4916
 */
object ArrowGuessBurrow {
    private const val SHAFT_LENGTH = 20
    private const val PARTICLE_DETECTION_TOLERANCE = 0.12
    private const val COUNT_NEAR_TIP = 4
    private const val COUNT_NEAR_BASE = 2
    private const val EPSILON = 1e-6
    private val HUB_BOUNDS_MIN = SboVec(-296.0, 0.0, -272.0)
    private val HUB_BOUNDS_MAX = SboVec(207.0, 256.0, 223.0)
    private val HUB_BOUNDS: Box = Box(HUB_BOUNDS_MIN.x, HUB_BOUNDS_MIN.y, HUB_BOUNDS_MIN.z, HUB_BOUNDS_MAX.x, HUB_BOUNDS_MAX.y, HUB_BOUNDS_MAX.z)
    private var spadeTitleShown = false

    private val allowedBlocksAboveGround = buildList {
        add(Blocks.AIR)
        add(Blocks.SPRUCE_FENCE)
        add(Blocks.WILDFLOWERS)
        add(Blocks.TALL_GRASS)
        add(Blocks.OAK_LEAVES)
        add(Blocks.BIRCH_LEAVES)
        add(Blocks.JUNGLE_LEAVES)
        add(Blocks.ACACIA_LEAVES)
        add(Blocks.DARK_OAK_LEAVES)
        add(Blocks.SPRUCE_LEAVES)
    }

    private val recentArrowParticles = TimeLimitedSet<SboVec>(1.minutes)
    private val locations: MutableSet<SboVec> = Collections.newSetFromMap(ConcurrentHashMap())

    private var lastBlockClicked: SboVec? = null

    private val allGuesses = mutableListOf<GuessEntry>()

    private var newArrow = true

    fun init() {
        registerBurrowDug()
        registerTick()
    }

    @SboEvent
    fun onReceiveParticle(event: PacketReceiveEvent) {
        if (!Diana.arrowGuess) return
        val packet = event.packet
        if (packet !is ParticleS2CPacket) return
        if (packet.distanceToPlayer() > 6.0) return
        if (packet.parameters.type != ParticleTypes.DUST) return
        if (packet.count != 0) return
        if (packet.speed != 1.0f) return
        val parameters = packet.parameters
        if (parameters !is DustParticleEffect) return
        val range = getArrowRange(packet.offsetX, packet.offsetY) ?: return
        val location = SboVec(packet.x, packet.y, packet.z)

        if(!recentArrowParticles.add(location)) return
        locations.add(location)

        val arrow = detectArrow() ?: return
        locations.clear()

        val guess = findClosestValidBlockToRayNew(arrow, range) ?: return
        WaypointManager.addArrowGuess(guess)
        newArrow = false
    }

    private fun registerBurrowDug() {
        Register.onChatMessageCancable(Pattern.compile("^§eYou (.*?) Griffin [Bb]urrow(.*?) §7\\((.*?)/(.*?)\\)$", Pattern.DOTALL)) { message, matchResult ->
            if (!Diana.arrowGuess) return@onChatMessageCancable true
            val currentBurrow = matchResult.group(3).toIntOrNull() ?: return@onChatMessageCancable true
            val maxBurrow = matchResult.group(4).toIntOrNull() ?: return@onChatMessageCancable true
            lastBlockClicked?.let { onBurrowDug(it, currentBurrow, maxBurrow) }
            true
        }
    }

    private fun registerTick() {
        Register.onTick(4) {
            if (!Diana.arrowGuess) return@onTick
            if (World.getWorld() != "Hub") return@onTick
            if (allGuesses.isEmpty()) return@onTick
            checkMoveGuess()
        }
    }

    @SboEvent
    fun onPlayerActionSend(event: PacketSendEvent) {
        if (!Diana.arrowGuess) return
        val packet = event.packet
        if (packet !is PlayerActionC2SPacket) return
        if (World.getWorld() != "Hub") return
        if (packet.action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return

        lastBlockClicked = SboVec(
            packet.pos.x.toDouble(),
            packet.pos.y.toDouble(),
            packet.pos.z.toDouble()
        )
    }

    @SboEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!Diana.arrowGuess) return
        val action = event.action
        if (action != "useBlock") return
        val player = SBOKotlin.mc.player
        val item = player?.mainHandStack
        if (item?.isEmpty == true) return
        if (item == null || !item.name.string.contains("Spade")) return
        if (event.pos ==  null) return

        lastBlockClicked = SboVec(
            event.pos.x.toDouble(),
            event.pos.y.toDouble(),
            event.pos.z.toDouble()
        )
    }

    private fun onBurrowDug(location: SboVec, currentChain: Int, maxChain: Int) {
        if (currentChain != maxChain) {
            locations.clear()
            newArrow = true
        }
        if (currentChain == 1) return

        val containList = allGuesses.filter { guessEntry ->
            guessEntry.guesses.any { guess -> guess.distanceTo(location) <= 3 }
        }

        containList.forEach { entry ->
            entry.removeGuesses()
        }

        allGuesses.removeAll(containList)
    }

    private fun detectArrow(): RaycastUtils.Ray? {
        val line = findline()
        if (line.isEmpty()) return null
        val candidate1 = line[1]
        val candidate2 = line[line.size - 2]
        val count1 = getPointsWithinDistance(candidate1)
        val count2 = getPointsWithinDistance(candidate2)

        if (!((count1 == COUNT_NEAR_BASE && count2 == COUNT_NEAR_TIP) ||
            (count1 == COUNT_NEAR_TIP && count2 == COUNT_NEAR_BASE))
        ) return null

        val base: SboVec
        val tip: SboVec
        if (count1 == COUNT_NEAR_TIP) {
            base = line.last()
            tip = line.first()
        } else {
            base = line.first()
            tip = line.last()
        }

        val adjustedBase = base.down(1.5)
        val adjustedTip = tip.down(1.5)

        return RaycastUtils.Ray(adjustedBase, adjustedTip.minus(adjustedBase).normalize())
    }

    private fun findline(): List<SboVec> {
        val locationsSnapshot = locations.toSet()

        for (location in locationsSnapshot) {
            val line = mutableListOf<SboVec>()
            val visited = mutableSetOf<SboVec>()
            line.add(location)
            visited.add(location)

            if (extendLine(line, visited, locationsSnapshot, SHAFT_LENGTH, PARTICLE_DETECTION_TOLERANCE)) {
                return line.toList()
            }
        }
        return emptyList()
    }
    private fun extendLine(
        line: MutableList<SboVec>,
        visited: MutableSet<SboVec>,
        locations: Set<SboVec>,
        numPoints: Int,
        maxDist: Double
    ): Boolean {
        if (line.size == numPoints) return true

        var nextLoc: SboVec? = null
        var minDist = Double.MAX_VALUE

        for (location in locations) {
            if (visited.contains(location)) continue
            val dist = line.last().distanceTo(location)
            if (dist > maxDist) continue

            val second = if (line.size > 1) line[1] else line[0]
            if (!isCollinear(line.first(), second, location)) continue
            if (dist < minDist) {
                minDist = dist
                nextLoc = location
            }
        }

        if (nextLoc != null) {
            line.add(nextLoc)
            visited.add(nextLoc)
            if (extendLine(line, visited, locations, numPoints, maxDist)) {
                return true
            }
            line.removeLast()
            visited.remove(nextLoc)
        }
        return false
    }

    private fun getPointsWithinDistance(origin: SboVec): Int {
        val maxDistSq = PARTICLE_DETECTION_TOLERANCE * PARTICLE_DETECTION_TOLERANCE
        return locations.count { it != origin && it.distanceSq(origin) <= maxDistSq }
    }

    private fun findClosestValidBlockToRayNew(ray: RaycastUtils.Ray, range: IntRange): SboVec? {
        val bounds = HUB_BOUNDS
        if (!bounds.isInside(ray.origin)) return null

        val endPoint = RaycastUtils.intersectAABBWithRay(bounds, ray)?.second ?: return null
        val diff = endPoint.minus(ray.origin).toDoubleArray()
        val axisIndex = diff.withIndex()
            .filter { (_, value) -> abs(value) > 0.9 }
            .minByOrNull { (_, value) -> abs(value) }
            ?.index
            ?: return null


        val candidates = mutableMapOf<SboVec, Pair<Double, Double>>()

        val endPointArray = endPoint.toDoubleArray()
        val originArray = ray.origin.toDoubleArray()
        val directionArray = ray.direction.toDoubleArray()

        val iterations = abs(endPointArray[axisIndex] - originArray[axisIndex]).toInt()
        for (i in 1..iterations) {
            val axisValue = originArray[axisIndex] + i * sign(directionArray[axisIndex])
            val candidatePoint = RaycastUtils.findPointOnRay(ray, axisIndex, axisValue) ?: continue
            val candidateBlock = candidatePoint.roundToBlock()
            if (!isBlockValid(candidateBlock)) continue
            val blockCenter = candidateBlock.add(0.5, 0.5, 0.5)
            val distanceToRay = RaycastUtils.findDistanceToRay(ray, blockCenter)

            val distanceFromOrigin = candidatePoint.distance(ray.origin)

            val scaledDistance = (distanceToRay * 500000 / distanceFromOrigin).roundTo(5)

            candidates[candidateBlock] = Pair(scaledDistance.roundTo(5), distanceFromOrigin)
        }
        if (candidates.isEmpty()) return null

        val minValue = candidates.values.minOf { it.first }
        val possibilities = candidates.filterValues { it.first == minValue }
        val withinRange = possibilities.filterValues { it.second.toInt() in range }.map { it.key }

        if (withinRange.isNotEmpty()) {
            allGuesses.add(GuessEntry(withinRange))
        }

        if (Diana.showTitleWhenInaccurate) {
            if (withinRange.size > 1) {
                if (!spadeTitleShown) Helper.showTitle("§c Use Spade!", "", 0, 30, 0)
                spadeTitleShown = true
            } else {
                spadeTitleShown = false
            }
        }

        return withinRange.getOrNull(0)
    }

    private fun checkMoveGuess() {
        val player = SBOKotlin.mc.player ?: return
        val hasSpade = InventoryUtils.isItemHeld("SPADE", 1.seconds)
        val burrowLocations = BurrowDetector.burrows.values.map { it.waypoint?.pos ?: SboVec(0.0, 0.0, 0.0) }.toSet()

        for (guess in allGuesses.toList()) {
            val current = guess.getCurrent()
            if (!isBlockValid(current)) {
                guess.moveToNext()
                continue
            }
            if (hasSpade) {
                //#if MC >= 1.21.9
                //$$ val playerPos = player.entityPos.toSboVec()
                //#else
                val playerPos = player.pos.toSboVec()
                //#endif
                val isKnownBurrow = burrowLocations.contains(current)
                if (!isKnownBurrow && current.distanceSq(playerPos) < 900) {
                    if (guess.moveToNext()) {
                        return
                    }
                }
            }
        }
    }

    private fun getArrowRange(offsetX: Float, offsetY: Float): IntRange? {
        if (offsetY == 128.0f) return 0..117 //Green Close
        if (offsetY == 255.0f && offsetX == 255.0f) return 112..282 //Red Medium
        if (offsetX == 255.0f) return 281..600 //Black Far
        return null
    }

    private fun isCollinear(a: SboVec, b: SboVec, c: SboVec): Boolean {
        val ab = b.minus(a)
        val ac = c.minus(a)
        val cross = ab.crossProduct(ac)
        return cross.lengthSquared() < EPSILON
    }

    internal fun isBlockValid(pos: SboVec): Boolean {
        if (!pos.isInLoadedChunk()) return true
        val isGround = pos.getBlockAt() == Blocks.GRASS_BLOCK
        val isValidBlockAbove = pos.up().getBlockAt() in allowedBlocksAboveGround
        return isGround && isValidBlockAbove
    }

    private fun ParticleS2CPacket.distanceToPlayer(): Double {
        val player = SBOKotlin.mc.player ?: return Double.MAX_VALUE
        val dx = this.x - player.x
        val dy = this.y - player.y
        val dz = this.z - player.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(this * factor) / factor
    }

    private fun Box.isInside(vec: SboVec): Boolean {
        return vec.x > this.minX && vec.x <= this.maxX &&
                vec.y > this.minY && vec.y <= this.maxY &&
                vec.z > this.minZ && vec.z <= this.maxZ;
    }

}
