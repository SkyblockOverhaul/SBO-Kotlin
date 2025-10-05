package net.sbo.mod.guis
//todo: remake this with Vexel https://github.com/meowing-xyz/vexel

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import net.sbo.mod.diana.achievements.Achievement
import net.sbo.mod.diana.achievements.AchievementManager
import net.sbo.mod.utils.data.SboDataObject
import java.awt.Color
import kotlin.math.floor

class AchievementsGUI : WindowScreen(ElementaVersion.V10) {
    enum class AchievementFilter {
        DEFAULT, RARITY, LOCKED, UNLOCKED
    }

    private val rarityOrder = listOf("Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic", "Divine", "Impossible")

    private var filterType = AchievementFilter.DEFAULT
    private var achievementList: List<Achievement> = emptyList()
    private var sboData = SboDataObject.sboData

    private lateinit var contentPanel: UIComponent
    private lateinit var scrollComponent: ScrollComponent
    private lateinit var titleText : UIText
    private lateinit var unlockedCountText: UIText
    private lateinit var filterText: UIText
    private lateinit var filterButtonOutline: UIRoundedRectangle
    private lateinit var achievementsContainer: UIBlock

    init {
        renderGui()
    }

    override fun initScreen(width: Int, height: Int) {
        super.initScreen(width, height)
        loadFilterFromSboData()
        updateAchievementList()
        renderAchievements()
    }

    private fun updateAchievementList() {
        achievementList = AchievementManager.achievements.values.sortedBy { it.id }.let { achievements ->
            when (filterType) {
                AchievementFilter.RARITY -> achievements.sortedBy { rarityOrder.indexOf(it.rarity) }
                AchievementFilter.LOCKED -> achievements.filter { !it.isUnlocked() }
                AchievementFilter.UNLOCKED -> achievements.filter { it.isUnlocked() }
                else -> achievements
            }
        }

        if (this::unlockedCountText.isInitialized) {
            val unlockedAchievements = AchievementManager.achievements.values.count { it.isUnlocked() }
            val totalAchievements = AchievementManager.achievements.values.count()
            val unlockedPercentage = (unlockedAchievements.toFloat() / totalAchievements * 100).toFixed(2)
            unlockedCountText.setText("Unlocked: $unlockedAchievements/$totalAchievements ($unlockedPercentage%)")
        }

        if (this::filterText.isInitialized) {
            filterText.setText("Filter: ${filterType.name.lowercase().replaceFirstChar { it.uppercase() }}")
        }
    }

    private fun renderGui() {
        UIBlock().constrain {
            width = 100.percent
            height = 100.percent
        }.setColor(Color(0, 0, 0, 200)) childOf window

        achievementsContainer = UIBlock().constrain {
            x = CenterConstraint()
            y = 20.percent
            width = 80.percent
            height = 70.percent
        } childOf window
        achievementsContainer.setColor(Color(0, 0, 0, 0))

        titleText = UIText("SBO Achievements").constrain {
            x = CenterConstraint()
            y = (achievementsContainer.getTop() - 40).pixels
            textScale = 1.5.pixels
        } childOf window
        titleText.setColor(Color.WHITE)

        val unlockedAchievements = AchievementManager.achievements.values.count { it.isUnlocked() }
        val totalAchievements = AchievementManager.achievements.values.count()
        val unlockedPercentage = (unlockedAchievements.toFloat() / totalAchievements * 100).toFixed(2)
        unlockedCountText = UIText("Unlocked: ${AchievementManager.achievementsUnlocked}/${totalAchievements} ($unlockedPercentage%)").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            textScale = 1.2.pixels
        } childOf window
        unlockedCountText.setColor(Color(0, 255, 35, 224))

        scrollComponent = ScrollComponent().constrain {
            x = 0.pixels
            y = 0.pixels
            width = FillConstraint()
            height = FillConstraint()
        } childOf achievementsContainer
        scrollComponent.setColor(Color(0, 0, 0, 0))

        contentPanel = UIBlock().constrain {
            x = 0.pixels
            y = 0.pixels
            width = FillConstraint()
            height = ChildBasedSizeConstraint()
        }.setColor(Color(0, 0, 0, 0)) childOf scrollComponent

        filterButtonOutline = UIRoundedRectangle(5f).constrain {
            x = achievementsContainer.getLeft().pixels
            y = (achievementsContainer.getTop() - 40).pixels
            width = 122.pixels
            height = 32.pixels
        } childOf window
        filterButtonOutline.setColor(Color(255, 255, 255, 255)) // White outline color

        val filterButton = UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 120.pixels
            height = 30.pixels
        } childOf filterButtonOutline
        filterButton.setColor(Color.BLACK)

        filterText = UIText("Filter: ${filterType.name.lowercase().replaceFirstChar { it.uppercase() }}").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.0.pixels
        } childOf filterButton
        filterText.setColor(Color.WHITE)

        filterButton.onMouseClick { event ->
            val filterOptions = AchievementFilter.entries.toTypedArray()
            val currentIndex = filterOptions.indexOf(filterType)
            filterType = if (event.mouseButton == 0) {
                filterOptions[(currentIndex + 1) % filterOptions.size]
            } else {
                filterOptions[(currentIndex + filterOptions.size - 1) % filterOptions.size]
            }
            sboData.achievementFilter = filterType.name
            SboDataObject.save("SboData")
            updateAchievementList()
            renderAchievements()
        }
    }

    private fun renderAchievements() {
        contentPanel.clearChildren()

        val achievementBoxWidth = 200f
        val achievementBoxHeight = 45f
        val spacingX = 20f
        val spacingY = 20f
        val columns = floor((scrollComponent.getWidth() - spacingX) / (achievementBoxWidth + spacingX)).toInt()
        if (columns <= 0) return
        val totalGridWidth = (columns * achievementBoxWidth) + ((columns - 1) * spacingX)
        val centeringOffset = ((scrollComponent.getWidth() - totalGridWidth) / 2f).coerceAtLeast(10f)
        var lastY = 0f

        filterButtonOutline.constrain {
            x = (achievementsContainer.getLeft() + centeringOffset).pixels
            y = if (columns == 1) {
                (achievementsContainer.getTop() - 15).pixels
            } else {
                (achievementsContainer.getTop() - 40).pixels
            }
        }

        titleText.constrain {
            y = (achievementsContainer.getTop() - 40).pixels
            textScale = if (columns == 1) 1.2.pixels else 1.5.pixels
        }

        unlockedCountText.constrain {
            y = SiblingConstraint(5f)
            textScale = if (columns == 1) 1.0.pixels else 1.2.pixels
        }

        achievementList.forEachIndexed { index, achievement ->
            val column = index % columns
            val row = floor(index.toFloat() / columns).toInt()
            val posX = centeringOffset + (column * (achievementBoxWidth + spacingX))
            val posY = spacingY + (row * (achievementBoxHeight + spacingY))
            lastY = posY
            val borderColor = if (achievement.isUnlocked()) Color(0, 255, 0) else Color(255, 0, 0)

            val roundedOutline = UIRoundedRectangle(5f).constrain {
                x = posX.pixels
                y = posY.pixels
                width = (achievementBoxWidth + (1f * 2)).pixels
                height = (achievementBoxHeight + (1f * 2)).pixels
            }.setColor(borderColor)

            UIRoundedRectangle(5f).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = achievementBoxWidth.pixels
                height = achievementBoxHeight.pixels
            }.setColor(Color(0, 0, 0, 255))
                .addChild(UIText(achievement.getDisplayName()).constrain {
                    x = 5.pixels
                    y = 5.pixels
                    textScale = 1.0.pixels
                })
                .addChild(UIText("§7${achievement.description}").constrain {
                    x = 5.pixels
                    y = SiblingConstraint(5f)
                    textScale = 1.0.pixels
                })
                .addChild(UIText("${achievement.color}${achievement.rarity}").constrain {
                    x = 5.pixels
                    y = SiblingConstraint(5f)
                    textScale = 0.8.pixels
                }) childOf roundedOutline

            roundedOutline childOf contentPanel
        }

        val requiredHeight = if (achievementList.isNotEmpty()) {
            lastY + achievementBoxHeight + spacingY
        } else {
            0f
        }

        contentPanel.constrain {
            height = requiredHeight.pixels
        }
    }

    private fun loadFilterFromSboData() {
        filterType = try {
            AchievementFilter.valueOf(sboData.achievementFilter.uppercase())
        } catch (_: Exception) {
            AchievementFilter.DEFAULT
        }
    }

    private fun Float.toFixed(digits: Int): String {
        return String.format("%.${digits}f", this)
    }
}