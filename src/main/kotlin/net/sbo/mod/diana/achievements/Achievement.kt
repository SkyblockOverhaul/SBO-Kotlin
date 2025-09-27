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
    val timeout: Int = 1,
    val hidden: Boolean = false,
) {
    val color = AchievementManager.rarityColorDict[rarity] ?: "§f"
    var unlocked: Boolean = false

    fun unlock() {
        achievementsData.achievements[id] = true
        SboDataObject.save("AchievementsData")

        var hiddenExtra = ""
        if (hidden) {
            description = description.substring(2)
            hiddenExtra = "§7[Secret Achievement] "
        }
        val player = mc.player
        if (rarity == "Divine" || rarity == "Impossible") {
            Helper.showTitle("§kd§r $color$name §kd§r", "§aAchievement Unlocked!", 0, 50, 20)
            Chat.chat(
                textComponent(
                    "§6[SBO] §aAchievement Unlocked §7>> $color§kd§r $color$name §kd§r",
                    "$hiddenExtra§a$description"
                )
            )
            mc.world?.playSound(
                player,
                player?.blockPos,
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
            )

        } else {
            Helper.showTitle("$color$name", "§aAchievement Unlocked!", 0, 50, 20)
            Chat.chat(textComponent("§6[SBO] §aAchievement Unlocked §7>> $color$name", "$hiddenExtra§a$description"))
            mc.world?.playSound(
                player,
                player?.blockPos,
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
            )
        }
        unlocked = true
        AchievementManager.achievementsUnlocked += 1
    }

    fun lock() {
        achievementsData.achievements.remove(id)
        unlocked = false
        AchievementManager.achievementsUnlocked -= 1
        if (hidden) {
            description = "§k$description"
        }
    }

    fun loadState() {
        unlocked = achievementsData.achievements[id] ?: false
        if (unlocked) {
            AchievementManager.achievementsUnlocked += 1
        } else {
            if (hidden) description = "§k$description"
        }
    }

    fun isUnlocked(): Boolean {
        return achievementsData.achievements[id] ?: false
    }

    fun getDisplayName(): String {
        return "$color$name"
    }
}