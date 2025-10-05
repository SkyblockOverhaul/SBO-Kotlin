package net.sbo.mod.overlays

import net.minecraft.util.Formatting.*
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.data.SboDataObject.sboData
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayTextLine

object DianaStats {
    val overlay = Overlay("Diana Stats", 10f, 10f, 1f, listOf("Chat screen")).setCondition { Diana.statsTracker && (Helper.checkDiana() || Helper.hasSpade)}

    fun init() {
        overlay.init()
        updateLines()
    }

    fun updateLines() {
        val lines = mutableListOf<OverlayTextLine>()
        lines.addAll(
            listOf(
                OverlayTextLine("$YELLOW${BOLD}Diana Stats"),
                OverlayTextLine("$GRAY - ${LIGHT_PURPLE}Mobs since Inq: $AQUA${sboData.mobsSinceInq}"),
                OverlayTextLine("$GRAY - ${LIGHT_PURPLE}Inqs since Chimera: $AQUA${sboData.inqsSinceChim}"),
                OverlayTextLine("$GRAY - ${LIGHT_PURPLE}Inqs since Chimera §7[§bLS§7]: $AQUA${sboData.inqsSinceLsChim}"),
                OverlayTextLine(
                    "$GRAY - ${GOLD}Minos since Stick: $AQUA${
                        Helper.formatNumber(
                            sboData.minotaursSinceStick,
                            true
                        )
                    }"
                ),
                OverlayTextLine(
                    "$GRAY - ${DARK_PURPLE}Champs since Relic: $AQUA${
                        Helper.formatNumber(
                            sboData.champsSinceRelic,
                            true
                        )
                    }"
                )
            )
        )
        overlay.setLines(lines)
    }
}