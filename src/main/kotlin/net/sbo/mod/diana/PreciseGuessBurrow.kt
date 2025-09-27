package net.sbo.mod.diana

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes
import net.sbo.mod.SBOKotlin
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.PacketReceiveEvent
import net.sbo.mod.utils.events.impl.PlayerInteractEvent
import net.sbo.mod.utils.game.World
import net.sbo.mod.utils.math.PolynomialFitter
import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.utils.waypoint.WaypointManager
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object PreciseGuessBurrow {
    private var particleLocations = mutableListOf<SboVec>()
    private var guessPoint: SboVec? = null
    private var lastLavaParticle: Long = 0

    var finalLocation: SboVec? = null
    var lastGuessTime: Long = 0

    fun init() {
        Register.onWorldChange {
            if (!Diana.dianaBurrowGuess) return@onWorldChange
            this.guessPoint = null
            this.particleLocations.clear()
            finalLocation = null
        }
    }

    @SboEvent
    fun onReceiveParticle(event: PacketReceiveEvent) {
        val packet = event.packet
        if (packet !is ParticleS2CPacket) return
        if (!Diana.dianaBurrowGuess || World.getWorld() != "Hub") return
        if (packet.parameters.type != ParticleTypes.DRIPPING_LAVA || packet.count != 2 || packet.speed != -0.5f) return
        val currLoc = SboVec(packet.x, packet.y, packet.z)
        this.lastLavaParticle = System.currentTimeMillis()
        if (System.currentTimeMillis() - lastGuessTime > 3000) return

        if (this.particleLocations.isEmpty()) {
            this.particleLocations.add(currLoc)
            return
        }

        val distToLast = this.particleLocations.last().distanceTo(currLoc)
        if (distToLast > 3 || distToLast == 0.0) return
        this.particleLocations.add(currLoc)

        val guessPosition = this.guessBurrowLocation()
        if (guessPosition == null) return
        finalLocation = guessPosition.down(0.5).roundLocationToBlock()
        finalLocation = guessPosition.down(0.5).roundLocationToBlock()
        WaypointManager.updateGuess(finalLocation)
    }

    @SboEvent
    fun onUseSpade(event: PlayerInteractEvent) {
        if (!Diana.dianaBurrowGuess) return
        val action = event.action
        if (action != "useItem" && action != "useBlock") return
        val player = SBOKotlin.mc.player
        val item = player?.mainHandStack
        if (item?.isEmpty == true) return
        if (item == null || !item.name.string.contains("Spade")) return
        if (System.currentTimeMillis() - this.lastLavaParticle < 200) {
            event.isCanceled = true
            return
        }
        this.particleLocations.clear()
        lastGuessTime = System.currentTimeMillis()
    }

    fun guessBurrowLocation(): SboVec? {
        if (this.particleLocations.size < 4) return null
        val fitters = List(3) { PolynomialFitter(3) }

        this.particleLocations.forEachIndexed { index, location ->
            val x = index.toDouble()
            location.toDoubleArray().forEachIndexed { i, value ->
                fitters[i].addPoint(x, value)
            }
        }

        val coefficients = fitters.map { it.fit() }
        val startPointDerivative = SboVec.Companion.fromArray(coefficients.map { it[1] })

        val pitch = this.getPitchFromDerivative(startPointDerivative)
        val controlPointDistance = sqrt(24 * sin(pitch - PI) + 25)
        val t = (3 * controlPointDistance) / startPointDerivative.length()
        val result = coefficients.map { coeff ->
            coeff[0] + coeff[1] * t + coeff[2] * t.pow(2) + coeff[3] * t.pow(3)
        }
        return SboVec.Companion.fromArray(result)
    }

    private fun getPitchFromDerivative(derivative: SboVec): Double {
        val xzLength = sqrt(derivative.x.pow(2) + derivative.z.pow(2))
        val pitchRadians = -atan2(derivative.y, xzLength)

        var guessPitch = pitchRadians
        var windowMin = -PI / 2
        var windowMax = PI / 2

        repeat(100) {
            val resultPitch = atan2(sin(guessPitch) - 0.75, cos(guessPitch))

            if (resultPitch == pitchRadians) {
                return guessPitch
            }

            if (resultPitch < pitchRadians) {
                windowMin = guessPitch
            } else {
                windowMax = guessPitch
            }
            guessPitch = (windowMin + windowMax) / 2
        }
        return guessPitch
    }
}