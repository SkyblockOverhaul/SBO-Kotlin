package net.sbo.mod.utils.waypoint

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.sbo.mod.utils.render.RenderUtil
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.settings.categories.Diana
import java.awt.Color
import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.utils.Player
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.roundToInt

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
    val r: Float,
    val g: Float,
    val b: Float,
    val ttl: Int = 0,
    val type: String = "normal",
    var line: Boolean = false,
    var beam: Boolean = true,
    var distance: Boolean = true
) {
    var pos: SboVec = SboVec(this.x, this.y, this.z)
    var color: Color = Color(this.r, this.g, this.b)
    var hexCode: Int = this.color.rgb
    val alpha: Double = 0.5
    var hidden: Boolean = false
    val creation: Long = System.currentTimeMillis()
    var formatted: Boolean = false
    var distanceRaw: Double = 0.0
    var distanceText: String = ""
    var formattedText: String = ""
    var warp: String? = null

    fun distanceToPlayer(): Double { // works
        val playerPos = Player.getLastPosition()
        return sqrt((playerPos.x - this.pos.x).pow(2) + (playerPos.y - this.pos.y).pow(2) + (playerPos.z - this.pos.z).pow(2))
    }

    private fun setWarpText() {
        this.warp = WaypointManager.getClosestWarp(this.pos)
        this.formattedText = this.warp?.let { "$text§7 (warp $it)${this.distanceText}" } ?: "${this.text}${this.distanceText}"
    }

    fun format(
        inqWaypoints: List<Waypoint>,
        closestBurrowDistance: Double
    ) {
        this.distanceRaw = this.distanceToPlayer()
        this.distanceText = if (this.distance) " §b[${this.distanceRaw.roundToInt()}m]" else ""

        if (this.type == "guess") {
            this.line =  Diana.guessLine && (closestBurrowDistance > 60) && inqWaypoints.isEmpty()
            this.color =  Color(Customization.guessColor)
            this.hexCode = color.rgb

            val (exists, wp) = WaypointManager.waypointExists("burrow", this.pos)
            if (exists && wp != null) {
                this.hidden = wp.distanceToPlayer() < 60
            }

            this.setWarpText()
        } else if (this.type == "inq" && inqWaypoints.lastOrNull() == this) {
            this.setWarpText()
            this.line = Diana.inqLine
        } else {
            this.formattedText = "${this.text}${this.distanceText}"
        }

        this.formatted = true
    }

    fun hide(): Waypoint { // works
        this.hidden = true
        return this
    }

    fun show(): Waypoint { // works
        this.hidden = false
        return this
    }

    fun render(context: WorldRenderContext) {
        if (!this.formatted || this.hidden) return

        if (this.type == "guess" && this.distanceRaw <= Diana.removeGuessDistance) return

        RenderUtil.renderWaypoint(
            context,
            this.formattedText,
            this.pos,
            floatArrayOf(this.r, this.g, this.b),
            this.hexCode,
            this.alpha.toFloat(),
            true,
            this.line,
            Diana.dianaLineWidth.toFloat(),
            this.beam
        )
    }
}