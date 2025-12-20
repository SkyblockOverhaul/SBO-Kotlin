package net.sbo.mod.diana.mobs.indicator

import net.sbo.mod.utils.math.SboVec

data class DamageIndicator(
    val mobId: Int,
    val pos: SboVec,
    val damage: Float,
    val spawnTime: Long = System.currentTimeMillis()
)