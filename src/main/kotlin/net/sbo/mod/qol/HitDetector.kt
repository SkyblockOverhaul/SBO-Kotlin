package net.sbo.mod.qol

import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.sbo.mod.diana.achievements.AchievementManager.unlockAchievement
import net.minecraft.entity.player.PlayerEntity
import net.sbo.mod.utils.ItemUtils.getDisplayName
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.entity.EntitiyHitEvent

object HitDetector {
    fun init() {
        // Loads file
    }

    @SboEvent
    fun onHit(event: EntitiyHitEvent) {
        if (getDisplayName(event.player.mainHandStack).contains("Shears", true) && event.entity.name.contains(Text.of("Zombie"))) unlockAchievement(92)
        if (getDisplayName(event.player.mainHandStack).contains("Core", true) && event.entity.name.contains(Text.of("Zombie"))) unlockAchievement(93)
        ActionResult.PASS
    }
}