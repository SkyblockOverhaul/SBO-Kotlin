package net.sbo.mod.qol

import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.sbo.mod.diana.achievements.AchievementManager.unlockAchievement
import net.sbo.mod.utils.ItemUtils.getDisplayName

object HitDetector {
    fun init() {
        AttackEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            if (getDisplayName(player.mainHandStack).contains("Shears", true) && entity.name.contains(Text.of("King Minos"))) unlockAchievement(92)
            if (getDisplayName(player.mainHandStack).contains("Core", true) && entity.name.contains(Text.of("Manticore"))) unlockAchievement(93)
            ActionResult.PASS
        }
    }
}