package net.sbo.mod.utils

import net.sbo.mod.settings.categories.Debug
import net.sbo.mod.utils.ScoreBoard

object World {
    /**
     * Retrieves the current world name from the TabList.
     * If the world name is not found, it returns "None".
     */
    fun getWorld(): String {
        val worldName = TabList.findInfo("Area: ") ?: "None"
        return worldName
    }

    /**
     * Retrieves the current zone from the ScoreBoard.
     * If no zone is found, it returns "None".
     */
    fun getZone(): String {
        val lines = ScoreBoard.getLines()
        if (lines.isEmpty()) return "None"
        val zoneLine = lines.find { line -> line.contains("⏣") || line.contains("ф") }
        return zoneLine?.trim()?.removePrefix("⏣")?.removePrefix("ф")?.trim() ?: "None"
    }

    /**
     * Checks if the player is currently in Skyblock.
     * This is determined by checking if the scoreboard title contains "skyblock".
     * @return true if the player is in Skyblock, false otherwise.
     */
    fun isInSkyblock(): Boolean {
        val title = ScoreBoard.getTitle().lowercase()
        return title.contains("skyblock") || Debug.alwaysInSkyblock
    }
}