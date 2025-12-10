package net.sbo.mod.diana.guesses

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleTypes
import net.sbo.mod.SBOKotlin
import net.sbo.mod.utils.TimeLimitedList
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.packets.PacketReceiveEvent
import net.sbo.mod.utils.math.SboVec
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.minutes

object ArrowGuessBurrow {
    private val allowedOffsets = setOf(0.0f, 128.0f, 255.0f)

    private val recentArrowParticles = TimeLimitedList<SboVec>(1.minutes)
    private val locations: MutableSet<SboVec> = mutableSetOf()

    @SboEvent
    fun onReceiveParticle(event: PacketReceiveEvent) {
        val packet = event.packet
        if (packet !is ParticleS2CPacket) return
        if (packet.distanceToPlayer() > 6.0) return
        if (packet.parameters.type != ParticleTypes.DUST) return
        if (packet.count != 0) return
        if (packet.speed != 1.0f) return
        val parameters = packet.parameters
        if (parameters !is DustParticleEffect) return
        if (packet.offsetX !in allowedOffsets || packet.offsetY !in allowedOffsets || packet.offsetZ !in allowedOffsets) return

        val location = SboVec(packet.x, packet.y, packet.z)
        if(!recentArrowParticles.add(location)) return
        locations.add(location)

    }

    fun ParticleS2CPacket.distanceToPlayer(): Double {
        val player = SBOKotlin.mc.player ?: return Double.MAX_VALUE
        val dx = this.x - player.x
        val dy = this.y - player.y
        val dz = this.z - player.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}