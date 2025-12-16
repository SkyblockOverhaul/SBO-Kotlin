package net.sbo.mod.diana.guesses

import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.utils.waypoint.WaypointManager

class GuessEntry(val guesses: List<SboVec>) {
    private var currentIndex = 0

    fun getCurrent(): SboVec = guesses[currentIndex]

    fun contains(vec: SboVec): Boolean = guesses.contains(vec)

    fun removeGuesses() {
        guesses.forEach {
            WaypointManager.removeArrowGuess(it)
        }
    }

    fun moveToNext(): Boolean {
        WaypointManager.removeArrowGuess(getCurrent())
        val nextIndex = currentIndex + 1

        if (nextIndex in guesses.indices) {
            currentIndex = nextIndex
            WaypointManager.addArrowGuess(guesses[currentIndex])
            return true
        }
        return false
    }
}