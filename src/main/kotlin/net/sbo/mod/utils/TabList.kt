package net.sbo.mod.utils

import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import net.sbo.mod.SBOKotlin.mc

object TabList {
    /**
     * Returns a list of all PlayerListEntry objects from the current tab list.
     * Each PlayerListEntry object contains detailed information about a player.
     */
    fun getTabEntries(): List<PlayerListEntry> {
        val client = mc
        return try {
            client.player?.networkHandler?.playerList?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Finds the value associated with a specific key in the tab list entries.
     * The key should be a prefix that appears at the start of the line in the tab list.
     * @param key The key to search for in the tab list entries.
     * @return The value associated with the key, or null if not found.
     */
    fun findInfo(key: String): String? {
        val tabEntries = getTabEntries()
        if (tabEntries.isEmpty()) return null
        for (entry in tabEntries) {
            val lineText: Text = entry.displayName ?: Text.literal(entry.profile.name)
            val lineString = lineText.string.trim()
            if (lineString.startsWith(key)) {
                return lineString.substring(key.length).trim() // Return the value after the key
            }
        }
        return null // Return null if the key is not found
    }
}