package net.sbo.mod.diana.mobs.indicator

import net.minecraft.entity.LivingEntity
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.diana.mobs.DpsDetect
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.render.RenderUtils3D
import net.sbo.mod.utils.math.SboVec

//#if MC >= 1.21.9
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
//#else
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
//#endif

object DamageIndicatorRenderer {

    private fun LivingEntity.drawDamageScore(context: WorldRenderContext, damage: Float) {
        val text = "§d${Helper.formatNumber(damage)}"
        val pos = SboVec(this.x, this.y, this.z)
        val verticalOffset = this.height.toDouble() + 0.4

        RenderUtils3D.drawScore(
            context,
            pos,
            verticalOffset,
            text,
            -1,
            true,
            0.03,
            true
        )
    }

    fun onWorldRender(context: WorldRenderContext) {
        if (!Diana.dpsTracker) return
        val world = mc.world ?: return

        DpsDetect.getActiveMobIds().forEach { mobId ->
            val mob = world.getEntityById(mobId) as? LivingEntity ?: return@forEach

            val myDmg = DpsDetect.getClientTotal(mobId)
            if (myDmg > 0f) {
                mob.drawDamageScore(context, myDmg)
            }
        }
    }
}