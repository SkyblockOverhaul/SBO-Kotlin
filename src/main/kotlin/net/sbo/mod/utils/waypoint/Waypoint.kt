package net.sbo.mod.utils.waypoint

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.utils.render.RenderUtil
import net.sbo.mod.utils.waypoint.WaypointManager.closestGuess
import net.sbo.mod.utils.waypoint.WaypointManager.focusedGuess
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import java.awt.Color

/**
 * @class Waypoint
 * @description A class to create waypoints in the game.
 * @param text The text to display on the waypoint.
 * @param x The x-coordinate of the waypoint.
 * @param y The y-coordinate of the waypoint.
 * @param z The z-coordinate of the waypoint.
 * @param r The red color component of the waypoint.
 * @param g The green color component of the waypoint.
 * @param b The blue color component of the waypoint.
 * @param ttl The time to live for the waypoint in seconds (0 for infinite).
 * @param type The type of the waypoint for customization.
 * @param line Whether to draw a line to the waypoint.
 * @param beam Whether to draw a beam at the waypoint.
 * @param distance Whether to display the distance in meters (blocks) to the waypoint.
 */
class Waypoint(
    var text: String,
    val x: Double,
    val y: Double,
    val z: Double,
    var r: Float,
    var g: Float,
    var b: Float,
    val ttl: Int = 0,
    val type: String = "normal",
    var line: Boolean = false,
    var beam: Boolean = true,
    var distance: Boolean = true
) {
    var pos: SboVec = SboVec(x, y, z)
    var color: Color = Color(r, g, b)
    var hexCode: Int = color.rgb
    val alpha: Double = 0.5
    var hidden: Boolean = false
    val creation: Long = System.currentTimeMillis()
    var formatted: Boolean = false
    var distanceRaw: Double = 0.0
    var distanceText: String = ""
    var formattedText: String = ""
    var warp: String? = null

    fun distanceToPlayer(): Double {
        val playerPos = Player.getLastPosition()
        return sqrt((playerPos.x - pos.x).pow(2) + (playerPos.y - pos.y).pow(2) + (playerPos.z - pos.z).pow(2))
    }

    private fun setWarpText() {
        warp = WaypointManager.getClosestWarp(pos)
        formattedText = warp?.let {
            "$text§7 (warp $it)$distanceText"
        } ?: "$text$distanceText"
    }

    fun format(
        inqWaypoints: List<Waypoint>,
        closestBurrowDistance: Double
    ) {
        distanceRaw = distanceToPlayer()
        distanceText = if (distance) " §b[${distanceRaw.roundToInt()}m]" else ""

        if (type == "guess") {
            line = Diana.guessLine && (closestBurrowDistance > 60) && inqWaypoints.isEmpty() && (closestGuess.first == this)
            color = Color(Customization.guessColor)

            if (focusedGuess == this) {
                color = Color(Customization.focusedColor)
                line = Diana.guessLine && (closestBurrowDistance > 60) && inqWaypoints.isEmpty()
            }

            r = color.red.toFloat() / 255.0f
            g = color.green.toFloat() / 255.0f
            b = color.blue.toFloat() / 255.0f
            hexCode = color.rgb

            val (exists, wp) = WaypointManager.waypointExists("burrow", pos)
            if (exists && wp != null) {
                hidden = wp.distanceToPlayer() < 60
            }

            setWarpText()
        } else if (type == "inq" && inqWaypoints.lastOrNull() == this) {
            setWarpText()
            line = Diana.inqLine
        } else {
            formattedText = "$text$distanceText}"
        }

        formatted = true
    }

    fun hide(): Waypoint {
        hidden = true
        return this
    }

    fun show(): Waypoint {
        hidden = false
        return this
    }

    fun render(context: WorldRenderContext) {
        if (!formatted || hidden) return
        if (type == "guess" && distanceRaw <= Diana.removeGuessDistance) return

        RenderUtil.renderWaypoint(
            context,
            formattedText,
            pos,
            floatArrayOf(r, g, b),
            hexCode,
            alpha.toFloat(),
            true,
            line,
            Diana.dianaLineWidth.toFloat(),
            beam
        )
    }
}