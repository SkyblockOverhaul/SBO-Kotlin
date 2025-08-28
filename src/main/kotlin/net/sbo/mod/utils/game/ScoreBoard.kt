package net.sbo.mod.utils.game

import net.sbo.mod.SBOKotlin
import net.minecraft.scoreboard.*
import java.lang.String.CASE_INSENSITIVE_ORDER

object ScoreBoard {
    private val COMPARATOR: Comparator<ScoreboardEntry> = Comparator.comparing { obj: ScoreboardEntry -> obj.value() }
        .reversed()
        .thenComparing({ obj: ScoreboardEntry -> obj.owner() }, CASE_INSENSITIVE_ORDER);

    /**
     * Retrieves the lines from the scoreboard sidebar.
     * It returns a list of formatted strings representing the scoreboard entries.
     * Each entry is stripped of any formatting codes.
     * @return A list of formatted strings representing the scoreboard entries.
     */
    fun getLines(): List<String> {
        val scoreboard = SBOKotlin.mc.world?.scoreboard ?: return emptyList()
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()

        return scoreboard.getScoreboardEntries(objective)
            .filter { entry -> entry?.owner != null && !entry.hidden() }
            .sortedWith(COMPARATOR)
            .take(15)
            .map { entry ->
                Team.decorateName(
                    scoreboard.getScoreHolderTeam(entry.owner()),
                    entry.name()
                ).string
            }
            .map { decoratedText ->
                decoratedText.replace("§[^a-f0-9]".toRegex(), "")
            }
            .asReversed()
    }

    fun getTitle(): String {
        val scoreboard = SBOKotlin.mc.world?.scoreboard ?: return "Unknown Scoreboard"
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return "No Objective"
        return objective.displayName.string
    }
}