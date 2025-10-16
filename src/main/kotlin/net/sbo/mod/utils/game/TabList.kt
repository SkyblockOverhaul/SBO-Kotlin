package net.sbo.mod.utils.game

import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import net.sbo.mod.SBOKotlin.mc

object TabList {
    /**
     * Returns a list of all PlayerListEntry objects from the current tab list.
     * Each PlayerListEntry object contains detailed information about a player.
     */
    fun getTabEntries(): List<PlayerListEntry?> {
        val client = mc
        return try {
            client.player?.networkHandler?.playerList?.toList() ?: emptyList()
        } catch (_: Exception) {
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
        return getTabEntries()
            .filterNotNull()
            .mapNotNull { entry ->
                val text = entry.displayName ?: entry.profile?.name?.let { Text.literal(it) }
                text?.string?.trim()
            }
            .firstOrNull { it.startsWith(key) }
            ?.substring(key.length)
            ?.trim()
    }
}