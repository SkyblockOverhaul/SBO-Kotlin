package net.sbo.mod.overlays
/* todo: Refactoring
* This Code definetly needs refactoring, but I don't have the time to do it right now.
* I will do it in the future, but for now it works and I don't want to break it.
* If you want to refactor it, feel free to do so, but please keep the functionality intact.
*/
import net.minecraft.util.Formatting.*
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.Helper.calcPercentOne
import net.sbo.mod.utils.Helper.removeFormatting
import net.sbo.mod.utils.data.SboDataObject.SBOConfigBundle
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.GuiOpenEvent
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayTextLine
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

object DianaMobs {
    val overlay = Overlay(
        "Diana Mobs",
        10f,
        10f,
        1f,
        listOf("Chat screen", "Crafting")
    ).setCondition { (Diana.mobTracker != Diana.Tracker.OFF && Helper.checkDiana()) || Helper.hasSpade }
    val changeView: OverlayTextLine = OverlayTextLine("${YELLOW}Change View")
        .onClick {
            Diana.mobTracker = Diana.mobTracker.next()
            updateLines()
        }
        .onMouseEnter {
            changeView.text = "$YELLOW${UNDERLINE}Change View"
        }
        .onMouseLeave {
            changeView.text = "${YELLOW}Change View"
        }

    fun init() {
        overlay.init()
        updateLines()
        Register.onGuiClose { screen ->
            if (screen.title.string == "Crafting") {
                overlay.removeLine(changeView)
            }
        }
    }

    @SboEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.screen.title.string == "Crafting") {
            updateLines("CraftingOpen")
        }
    }

    fun createLine(name: String, formattedText: String): OverlayTextLine {
        val line = OverlayTextLine(formattedText).onClick {
            if (mc.currentScreen?.title?.string != "Crafting") return@onClick
            if (SBOConfigBundle.sboData.hideTrackerLines.contains(name)) {
                SBOConfigBundle.sboData.hideTrackerLines.remove(name)
            } else {
                SBOConfigBundle.sboData.hideTrackerLines.add(name)
            }
            updateLines()
        }
            .setCondition {
                !(mc.currentScreen?.title?.string != "Crafting" && SBOConfigBundle.sboData.hideTrackerLines.contains(
                    name
                ))
            }
        if (SBOConfigBundle.sboData.hideTrackerLines.contains(name)) {
            line.text = "$GRAY$STRIKETHROUGH${formattedText.removeFormatting()}"
        }
        return line
    }

    fun updateLines(screen: String = "") {
        val lines = mutableListOf<OverlayTextLine>()
        val type = Diana.mobTracker
        val tracker = when (type) {
            Diana.Tracker.TOTAL -> SBOConfigBundle.dianaTrackerTotalData
            Diana.Tracker.EVENT -> SBOConfigBundle.dianaTrackerMayorData
            Diana.Tracker.SESSION -> SBOConfigBundle.dianaTrackerSessionData
            Diana.Tracker.OFF -> {
                overlay.setLines(emptyList())
                return
            }
        }
        val inqPercent = calcPercentOne(tracker.items, tracker.mobs, "MINOS_INQUISITOR")
        val champPercent = calcPercentOne(tracker.items, tracker.mobs, "MINOS_CHAMPION")
        val minotaurPercent = calcPercentOne(tracker.items, tracker.mobs, "MINOTAUR")
        val gaiaPercent = calcPercentOne(tracker.items, tracker.mobs, "GAIA_CONSTRUCT")
        val lynxPercent = calcPercentOne(tracker.items, tracker.mobs, "SIAMESE_LYNXES")
        val hunterPercent = calcPercentOne(tracker.items, tracker.mobs, "MINOS_HUNTER")
        val playTimeHrs = tracker.items.TIME.toDouble() / TimeUnit.HOURS.toMillis(1)
        val mobsPerHr = if (playTimeHrs > 0) {
            BigDecimal(tracker.mobs.TOTAL_MOBS.toDouble() / playTimeHrs).setScale(2, RoundingMode.HALF_UP).toDouble()
        } else 0.0

        if (screen == "CraftingOpen" || mc.currentScreen?.title?.string == "Crafting") {
            lines.add(changeView)
        }

        lines.add(OverlayTextLine("$YELLOW${BOLD}Diana Mobs $GRAY($YELLOW${Helper.toTitleCase(type.toString())}$GRAY)"))

        lines.addAll(
            listOf(
                createLine(
                    "INQUISITOR",
                    "$GRAY - ${LIGHT_PURPLE}Inquisitor: $AQUA${
                        Helper.formatNumber(
                            tracker.mobs.MINOS_INQUISITOR,
                            true
                        )
                    } $GRAY($AQUA${inqPercent}%$GRAY) [${AQUA}LS$GRAY:$AQUA${
                        Helper.formatNumber(
                            tracker.mobs.MINOS_INQUISITOR_LS,
                            true
                        )
                    }$GRAY]"
                ),
                createLine(
                    "CHAMPION",
                    "$GRAY - ${DARK_PURPLE}Champion: $AQUA${
                        Helper.formatNumber(
                            tracker.mobs.MINOS_CHAMPION,
                            true
                        )
                    } $GRAY($AQUA${champPercent}%$GRAY)"
                ),
                createLine(
                    "MINOTAUR",
                    "$GRAY - ${GOLD}Minotaur: $AQUA${
                        Helper.formatNumber(
                            tracker.mobs.MINOTAUR,
                            true
                        )
                    } $GRAY($AQUA${minotaurPercent}%$GRAY)"
                ),
                createLine(
                    "GAIA_CONSTRUCT",
                    "$GRAY - ${GREEN}Gaia Construct: $AQUA${
                        Helper.formatNumber(
                            tracker.mobs.GAIA_CONSTRUCT,
                            true
                        )
                    } $GRAY($AQUA${gaiaPercent}%$GRAY)"
                ),
                createLine(
                    "SIAMESE_LYNXES",
                    "$GRAY - ${GREEN}Siamese Lynxes: $AQUA${
                        Helper.formatNumber(
                            tracker.mobs.SIAMESE_LYNXES,
                            true
                        )
                    } $GRAY($AQUA${lynxPercent}%$GRAY)"
                ),
                createLine(
                    "MINOS_HUNTER",
                    "$GRAY - ${GREEN}Minos Hunter: $AQUA${
                        Helper.formatNumber(
                            tracker.mobs.MINOS_HUNTER,
                            true
                        )
                    } $GRAY($AQUA${hunterPercent}%$GRAY)"
                ),
                OverlayTextLine(
                    "$GRAY - ${GRAY}Total Mobs: $AQUA${
                        Helper.formatNumber(
                            tracker.mobs.TOTAL_MOBS,
                            true
                        )
                    } $GRAY[$AQUA$mobsPerHr$GRAY/${AQUA}hr$GRAY]"
                )
            )
        )
        overlay.setLines(lines)
    }
}