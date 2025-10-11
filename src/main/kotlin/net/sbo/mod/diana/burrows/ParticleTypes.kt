package net.sbo.mod.diana.burrows

import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleTypes as MCParticleTypes

internal object ParticleTypes {
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