package net.sbo.mod.utils.game

import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
import net.minecraft.util.Util
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.PacketReceiveEvent
import kotlin.math.max

object ServerStats {
    private var prevTime = 0L
    var averageTps = 20f
        private set

    fun getTps(): Float {
        return averageTps
    }

    @SboEvent
    fun onPacketReceive(event: PacketReceiveEvent) {
        when (event.packet) {
            is WorldTimeUpdateS2CPacket -> {
                val currentTime = Util.getMeasuringTimeMs()
                if (prevTime != 0L) {
                    val deltaTime = currentTime - prevTime
                    averageTps = (20000f / max(1, deltaTime)).coerceIn(0f, 20f)
                }
                prevTime = currentTime
            }

            is GameJoinS2CPacket -> {
                averageTps = 20f
                prevTime = 0L
            }
        }
    }
}