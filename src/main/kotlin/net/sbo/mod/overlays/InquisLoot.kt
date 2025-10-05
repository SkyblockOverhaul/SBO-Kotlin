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
import net.sbo.mod.utils.Helper.removeFormatting
import net.sbo.mod.utils.data.SboDataObject.SBOConfigBundle
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.guis.GuiCloseEvent
import net.sbo.mod.utils.events.impl.guis.GuiOpenEvent
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayTextLine

object InquisLoot {
    val overlay = Overlay("Inquis", 10f, 10f, 1f, listOf("Chat screen", "Crafting")).setCondition { Diana.inquisTracker != Diana.Tracker.OFF && (Helper.checkDiana() || Helper.hasSpade)}
    val changeView: OverlayTextLine = OverlayTextLine("${YELLOW}Change View")
        .onClick {
            Diana.inquisTracker = Diana.inquisTracker.next()
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
    }

    @SboEvent
    fun onGuiClose(event: GuiCloseEvent) {
        if (event.screen.title.string == "Crafting") {
            overlay.removeLine(changeView)
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
        val type = Diana.inquisTracker
        val tracker = when (type) {
            Diana.Tracker.TOTAL -> SBOConfigBundle.dianaTrackerTotalData
            Diana.Tracker.EVENT -> SBOConfigBundle.dianaTrackerMayorData
            Diana.Tracker.SESSION -> SBOConfigBundle.dianaTrackerSessionData
            Diana.Tracker.OFF -> {
                overlay.setLines(emptyList())
                return
            }
        }

        if (screen == "CraftingOpen" || mc.currentScreen?.title?.string == "Crafting") {
            lines.add(changeView)
        }
        lines.add(OverlayTextLine("$YELLOW${BOLD}Inquis Loot $GRAY($YELLOW${Helper.toTitleCase(type.toString())}$GRAY)"))

        lines.addAll(
            listOf(
                createLine(
                    "INQ_TURTLE_SHELMET",
                    "$GRAY - ${LIGHT_PURPLE}Turtle Shelmet: $AQUA${tracker.inquis.DWARF_TURTLE_SHELMET}"
                ),
                createLine(
                    "INQ_CROCHET_TIGER_PLUSHIE",
                    "$GRAY - ${LIGHT_PURPLE}Tiger Plushie: $AQUA${tracker.inquis.CROCHET_TIGER_PLUSHIE}"
                ),
                createLine(
                    "INQ_ANTIQUE_REMEDIES",
                    "$GRAY - ${LIGHT_PURPLE}Antique Remedie: $AQUA${tracker.inquis.ANTIQUE_REMEDIES}"
                ),
                createLine(
                    "INQ_TURTLE_SHELMET_LS",
                    "$GRAY - ${LIGHT_PURPLE}Turtle Shelmets $GRAY[${AQUA}LS$GRAY]: $AQUA${tracker.inquis.DWARF_TURTLE_SHELMET_LS}"
                ),
                createLine(
                    "INQ_CROCHET_TIGER_PLUSHIE_LS",
                    "$GRAY - ${LIGHT_PURPLE}Tiger Plushie $GRAY[${AQUA}LS$GRAY]: $AQUA${tracker.inquis.CROCHET_TIGER_PLUSHIE_LS}"
                ),
                createLine(
                    "INQ_ANTIQUE_REMEDIES_LS",
                    "$GRAY - ${LIGHT_PURPLE}Antique Remedie $GRAY[${AQUA}LS$GRAY]: $AQUA${tracker.inquis.ANTIQUE_REMEDIES_LS}"
                ),
            )
        )
        overlay.setLines(lines)
    }
}