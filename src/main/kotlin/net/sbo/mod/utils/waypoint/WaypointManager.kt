package net.sbo.mod.utils.waypoint

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.sbo.mod.diana.PreciseGuessBurrow
import net.minecraft.client.MinecraftClient
import net.sbo.mod.diana.burrows.BurrowDetector.refreshBurrows
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.utils.render.WaypointRenderer
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.settings.categories.General.HideOwnWaypoints
import net.sbo.mod.settings.categories.General.hideOwnWaypoints
import net.sbo.mod.settings.categories.General.patcherWaypoints
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.Helper.checkDiana
import net.sbo.mod.utils.Helper.sleep
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.SoundHandler.playCustomSound
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.WorldChangeEvent
import net.sbo.mod.utils.math.SboVec
import java.awt.Color
import kotlin.collections.iterator
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.text.get

object WaypointManager {
    var guessWp: Waypoint? = null
    val waypoints = mutableMapOf<String, MutableList<Waypoint>>()
    var closestBurrow: Pair<Waypoint?, Double> = null to 1000.0
    var closestGuess: Pair<Waypoint?, Double> = null to 1000.0
    var focusedGuess: Waypoint? = null
    private val angleThreshold = Math.toRadians(4.0)
    private val cosThreshold = cos(angleThreshold)

    fun init() {
        if (guessWp == null) {
            guessWp = Waypoint("Guess", 100.0, 100.0, 100.0, 0.0f, 0.964f, 1.0f, 0, "guess")
        }

        Register.command("sboraitestguess") {
            val pos = Player.getLastPosition()
            guessWp = createGuessWaypoint(pos.plus(SboVec(10.0, 2.0, 10.0)))
            if (guessWp == null) return@command

            addWaypoint(guessWp!!)
        }

        Register.command("sboraitestresetguess") {
            getGuessWaypoints().clear()
        }

        Register.command("raitestrefreshguess") {
            refreshBurrows()
        }

        Register.command("sbosendinq") {
            val playerPos = Player.getLastPosition()
            Chat.command("pc x: ${playerPos.x.roundToInt()}, y: ${playerPos.y.roundToInt() - 1}, z: ${playerPos.z.roundToInt()}")
        }

        Register.onChatMessage(
            Regex("^(?<channel>.*> )?(?<playerName>.+?)[§&]f: (?:[§&]r)?x: (?<x>[^ ,]+),? y: (?<y>[^ ,]+),? z: (?<z>[^ ,]+)(?<trailing>.*)$")
        ) { message, match ->
            val channel = match.groups["channel"]?.value ?: "Unknown"
            val player = match.groups["playerName"]?.value ?: "Unknown"

            val x = match.groups["x"]?.value?.toIntOrNull() ?: 0.0
            val y = match.groups["y"]?.value?.toIntOrNull() ?: 0.0
            val z = match.groups["z"]?.value?.toIntOrNull() ?: 0.0

            val trailing = match.groups["trailing"]?.value ?: ""
            val playername = Player.getName() ?: ""
            if (!channel.contains("Guild")) {
                if ((!trailing.startsWith(" ") || trailing.lowercase().contains("inquisitor") || Diana.allWaypointsAreInqs) && Diana.receiveInq && checkDiana()) {
                    if (hideOwnWaypoints.contains(HideOwnWaypoints.INQ) && player.contains(playername)) return@onChatMessage
                    Helper.showTitle("§r§6§l<§b§l§kO§6§l> §b§lINQUISITOR! §6§l<§b§l§kO§6§l>", player, 0, 90, 20)
                    playCustomSound(Customization.inqSound[0], Customization.inqVolume)
                    addWaypoint(
                        Waypoint(
                            player,
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            1.0f,
                            0.84f,
                            0.0f,
                            45,
                            type = "inq"
                        )
                    )
                } else if (patcherWaypoints) {
                    if (hideOwnWaypoints.contains(HideOwnWaypoints.NORMAL) && player.contains(playername)) return@onChatMessage
                    addWaypoint(
                        Waypoint(
                            player,
                            x.toDouble(),
                            y.toDouble(),
                            z.toDouble(),
                            0.0f,
                            0.2f,
                            1.0f,
                            30,
                            type = "world"
                        )
                    )
                }
            }
        }

        Register.onTick(5) { _ ->
            closestBurrow = getClosestWaypoint(Player.getLastPosition(), "burrow") ?: (null to 1000.0)
            closestGuess = getClosestWaypoint(Player.getLastPosition(), "guess") ?: (null to 1000.0)

            val inqWps = getWaypointsOfType("inq")

            forEachWaypoint { waypoint ->
                if (waypoint.ttl > 0 && waypoint.creation + waypoint.ttl * 1000 < System.currentTimeMillis()) {
                    removeWaypoint(waypoint)
                }

                waypoint.line =
                    closestBurrow.first == waypoint && Diana.burrowLine && closestBurrow.second <= 60 && inqWps.isEmpty()

                waypoint.format(inqWps, closestBurrow.second)
            }

            guessWp?.format(inqWps, closestBurrow.second)
            refreshBurrows()
        }

        Register.onTick(1) { _ ->
            if (!Diana.dianaMultiBurrowGuess) return@onTick
            if (!Diana.focusedWarp) {
                focusedGuess = null
                return@onTick
            }

            val player = MinecraftClient.getInstance().player ?: return@onTick
            val eyeLocation = player.eyePos
            val lookVec = player.rotationVector.normalize()
            var maxDot = -1.0

            val bestGuess = getGuessWaypoints().minByOrNull { waypoint ->
                val dirToWaypoint = waypoint.pos.center().toVec3d().subtract(eyeLocation).normalize()
                lookVec.dotProduct(dirToWaypoint).also { dot ->
                    if (maxDot < dot) maxDot = dot
                }
            }

            focusedGuess = bestGuess.takeIf { maxDot >= cosThreshold }
        }

        WorldRenderEvents.AFTER_TRANSLUCENT.register(WaypointRenderer)
    }

    @SboEvent
    fun onWorldChange(event: WorldChangeEvent) {
        guessWp?.hide()
        removeAllOfType("world")
    }

    fun onLootshare() {
        removeWithinDistance("inq", 30)
    }

    /**
     * Renders all waypoints in the management system.
     * @param context The world render context.
     */
    fun renderAllWaypoints(context: WorldRenderContext) {
        forEachWaypoint { waypoint ->
            waypoint.render(context)
        }

        //this.guessWp?.render(context)

        // Create a copy of the list to avoid ConcurrentModificationException
        //val guessWaypointsCopy = this.guessWaypoints.toList()
        //guessWaypointsCopy.forEach { it.render(context) }
    }

    fun createGuessWaypoint(pos: SboVec?): Waypoint? {
        if (pos == null) return null
        val color = Color(Customization.guessColor)
        val r = color.red.toFloat() / 255.0f
        val g = color.green.toFloat() / 255.0f
        val b = color.blue.toFloat() / 255.0f

        val guess = Waypoint(
            "Guess",
            pos.x,
            pos.y,
            pos.z,
            r,
            g,
            b,
            0,
            "guess"
        )
        guess.show()
        return guess
    }

    /**
     * Adds a waypoint to the management system.
     * @param waypoint The waypoint to add.
     */
    fun addWaypoint(waypoint: Waypoint) {
        waypoints.getOrPut(waypoint.type.lowercase()) { mutableListOf() }.add(waypoint)
        if (waypoint.type.lowercase() == "burrow") playCustomSound(
            Customization.burrowSound[0],
            Customization.burrowVolume
        )
    }

    /**
     * Removes a specific waypoint from the management system.
     * @param waypoint The waypoint to remove.
     */
    fun removeWaypoint(waypoint: Waypoint) {
        if (closestBurrow.first == waypoint) {
            closestBurrow = null to 1000.0
        }
        waypoints[waypoint.type.lowercase()]?.remove(waypoint)
    }

    /**
     * Removes a waypoint at a specific position and type.
     * @param pos The position of the waypoint to remove.
     * @param type The type of the waypoint to remove.
     */
    fun removeWaypointAt(pos: SboVec, type: String) {
        val waypoint = waypoints[type.lowercase()]?.find { it.pos == pos }
        if (waypoint != null) {
            removeWaypoint(waypoint)
        }
    }

    /**
     * Removes all waypoints of a specific type.
     * @param type The type of waypoints to remove.
     */
    fun removeAllOfType(type: String) {
        waypoints[type.lowercase()]?.clear()
    }

    /**
     * Removes all waypoints of a specific type that are within a certain distance from the player's last position.
     * @param type The type of waypoints to remove.
     */
    fun removeWithinDistance(type: String, distance: Int) {
        val playerPos = Player.getLastPosition()
        waypoints[type] = getWaypointsOfType(type).filterNot { it.pos.distanceTo(playerPos) < distance }.toMutableList()
    }

    /**
     * Updates the guess waypoint position.
     * @param pos The new position for the guess waypoint.
     */
    fun updateGuess(pos: SboVec?) {
        if (!Diana.dianaMultiBurrowGuess) {
            guessWp?.apply {
                val (exists, wp) = waypointExists("burrow", this.pos)
                hidden = if (exists && wp != null) {
                    wp.distanceToPlayer() < 60
                } else {
                    false
                }
                if (pos != null) {
                    this.pos = pos
                }
            }
            return
        }
        if (pos == null) return
        val waypoint = createGuessWaypoint(pos) ?: return
        addWaypoint(waypoint)
    }

    /**
     * Checks if a waypoint of a specific type exists at a given position.
     * @param type The type of the waypoint to check.
     * @param pos The position to check for the waypoint.
     * @return A pair containing a boolean indicating existence and the waypoint if found.
     */
    fun waypointExists(type: String, pos: SboVec): Pair<Boolean, Waypoint?> {
        val waypoint = getWaypointsOfType(type).find { it.pos == pos }
        return (waypoint != null) to waypoint
    }

    /**
     * Iterates over all waypoints and applies the given action to each.
     * @param action The action to apply to each waypoint.
     */
    fun forEachWaypoint(action: (Waypoint) -> Unit) {
        waypoints.values.flatten().forEach(action)
    }

    fun getGuessWaypoints(): MutableList<Waypoint> {
        return waypoints.getOrPut("guess") { mutableListOf() }
    }

    /**
     * Retrieves all waypoints of a specific type.
     * @param type The type of waypoints to retrieve.
     * @return A list of waypoints of the specified type.
     */
    fun getWaypointsOfType(type: String): List<Waypoint> {
        return waypoints[type.lowercase()] ?: emptyList()
    }

    /**
     * Gets the closest waypoint of a specific type to a given position.
     * @param pos The position to find the closest waypoint to.
     * @param type The type of waypoint to search for.
     * @return The closest waypoint of the specified type, or null if none are found.
     */
    fun getClosestWaypoint(pos: SboVec, type: String): Pair<Waypoint, Double>? {
        val waypointsOfType = getWaypointsOfType(type)
        if (waypointsOfType.isEmpty()) return null

        var closestWaypoint: Waypoint? = null
        var closestDistance = Double.MAX_VALUE

        for (waypoint in waypointsOfType) {
            val distance = pos.distanceTo(waypoint.pos)
            if (distance < closestDistance) {
                closestDistance = distance
                closestWaypoint = waypoint
            }
        }

        return closestWaypoint?.let { it to closestDistance }
    }

    /**
     * Gets the closest warp point to a given position.
     * @param pos The position to find the closest warp to.
     * @return The name of the closest warp, or null if no warps are available.
     */
    fun getClosestWarp(pos: SboVec): String? {
        var warps = hubWarps.filter { it.value.unlocked }.mapValues { it.value }
        for (warp in Diana.allowedWarps) {
            if (additionalHubWarps.containsKey(warp.name.lowercase())) {
                val additionalWarp = additionalHubWarps[warp.name.lowercase()]
                if (additionalWarp != null && additionalWarp.unlocked) {
                    warps = warps + (warp.name.lowercase() to additionalWarp)
                }
            }
        }

        var closestWarp: String? = null
        var closestDistance = Double.MAX_VALUE
        val playerDistance = pos.distanceTo(Player.getLastPosition())
        for ((name, warp) in warps) {
            val distance = pos.distanceTo(warp.pos)
            if (distance < closestDistance) {
                closestDistance = distance
                closestWarp = name
            }
        }

        val condition1 = playerDistance > (closestDistance + Diana.warpDiff)
        val condition2 = condition1 && (closestBurrow.second > 60 || getWaypointsOfType("inq").isNotEmpty())

        val condition = if (Diana.dontWarpIfBurrowClose) condition2 else condition1

        return if (condition) closestWarp else null
    }

    fun warpToGuess() {
        if (!Diana.dianaMultiBurrowGuess) {
            val wp = guessWp ?: return
            if (wp.hidden) return
            getClosestWarp(wp.pos)?.let { executeWarpCommand(it) }
            return
        }
        val targetWp = if (Diana.focusedWarp) {
            focusedGuess
        } else {
            getGuessWaypoints().minByOrNull { it.pos.distanceTo(Player.getLastPosition()) }
        } ?: return
        getClosestWarp(targetWp.pos)?.let { executeWarpCommand(it) }
    }

    fun warpToInq() {
        val newestInq = getWaypointsOfType("inq").maxByOrNull { it.creation }
        if (newestInq == null) return

        val warp = getClosestWarp(newestInq.pos)
        if (warp == null) return

        executeWarpCommand(warp)
    }

    fun warpBoth() {
        if (getWaypointsOfType("inq").isEmpty()) {
            warpToGuess()
        } else {
            warpToInq()
        }
    }

    var tryWarp: Boolean = false
    fun executeWarpCommand(warp: String) {
        if (!checkDiana()) return
        if (Diana.warpDelay > 0 && System.currentTimeMillis() - PreciseGuessBurrow.lastGuessTime < Diana.warpDelay) return
        if (warp.isNotEmpty() && !tryWarp) {
            tryWarp = true
            Chat.command("warp $warp")
            sleep(500) {
                tryWarp = false
            }
        }
    }
}