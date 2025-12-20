package net.sbo.mod.diana.achievements

import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.chat.Chat.textComponent
import net.sbo.mod.utils.data.SboDataObject
import net.sbo.mod.utils.data.SboDataObject.achievementsData

class Achievement(
    val id: Int,
    val name: String,
    var description: String,
    val rarity: String,
    val previousId: Int? = null,
    val hidden: Boolean = false,
    val repeatable: Boolean = false,
) {
    val color = AchievementManager.rarityColorDict[rarity] ?: "§f"

    private fun checkYearReset() {
        val currentYear = SboDataObject.dianaTrackerMayor.year
        if (achievementsData.lastEventYear != currentYear) {
            achievementsData.currentEventAchievements.clear()
            achievementsData.lastEventYear = currentYear
            SboDataObject.save("AchievementsData")
        }
    }

    private fun showUnlockEffects() {
        var hiddenExtra = ""
        if (this.hidden) {
            this.description = this.description.substring(2)
            hiddenExtra = "§7[Secret Achievement] "
        }
        val player = mc.player
        if (this.rarity == "Divine" || this.rarity == "Impossible" || this.rarity == "Celestial") {
            Helper.showTitle("§kd§r $color$name §kd§r", "§aAchievement Unlocked!", 0, 50, 20)
            Chat.chat(textComponent("§6[SBO] §aAchievement Unlocked §7>> $color§kd§r $color$name §kd§r", "$hiddenExtra§a$description"))
            mc.world?.playSound(player, player?.blockPos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f)

        } else {
            Helper.showTitle("$color$name", "§aAchievement Unlocked!", 0, 50, 20)
            Chat.chat(textComponent("§6[SBO] §aAchievement Unlocked §7>> $color$name", "$hiddenExtra§a$description"))
            mc.world?.playSound(player, player?.blockPos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f)
        }
    }

    fun unlock() {
        checkYearReset()

        val currentTotal = achievementsData.totalAchievements.getOrDefault(id, 0)
        achievementsData.totalAchievements[id] = currentTotal + 1


        if (repeatable) {
            achievementsData.currentEventAchievements[id] = true
            AchievementManager.achievementsUnlockedEvent += 1
        }
        AchievementManager.achievementsUnlockedTotal += 1

        SboDataObject.save("AchievementsData")

        showUnlockEffects()
    }

    fun lock() {
        achievementsData.totalAchievements.remove(id)
        achievementsData.currentEventAchievements.remove(id)
        AchievementManager.achievementsUnlockedTotal -= 1
        if (this.repeatable) AchievementManager.achievementsUnlockedEvent -= 1
        if (this.hidden) {
            this.description = "§k" + this.description
        }
    }

    fun loadState() {
        if (isUnlocked(true)) {
            AchievementManager.achievementsUnlockedTotal += 1
        } else {
            if (this.hidden) this.description = "§k" + this.description
        }
        if (this.repeatable && isUnlocked()) {
            AchievementManager.achievementsUnlockedEvent += 1
        }
    }

    fun canBeUnlocked(): Boolean {
        checkYearReset()

        if (!repeatable) {
            return (achievementsData.totalAchievements[id] ?: 0) == 0
        }
        return achievementsData.currentEventAchievements[id] != true
    }

    fun isUnlocked(total: Boolean = false): Boolean {
        checkYearReset()

        if (repeatable && !total) {
            return achievementsData.currentEventAchievements[id] ?: false
        }
        return (achievementsData.totalAchievements[id] ?: 0) > 0
    }

    fun getDisplayName(): String {
        return "$color$name"
    }
}