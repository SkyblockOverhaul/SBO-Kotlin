package net.sbo.mod.utils.game

import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.events.Register
import java.util.Collections

object TabList {
    /**
     * Holds cached tab lines updated each tick.
     */
    private var cachedTabLines = emptyList<String>()

    /**
     * Ensures the tab lines are fetched on the first call.
     */
    private val initOnce: Unit by lazy {
        // To avoid accessing uninitalized cache on first call, update cache explicitly once.
        updateCache()
    }

    /**
     * Registers a task to update the cache each tick.
     */
    fun init() {
        Register.onTick(1) {
            // Periodic updates each tick
            updateCache()
        }
    }

    /**
     * Updates tab list cache by fetching, filtering and mapping the tab list.
     */
    private fun updateCache() {
        val tabLines = mutableListOf<String>()

        for (entry in getTabEntries()) {
            if (entry == null) continue

            val displayName = entry.displayName
            val profile = entry.profile
            val profileName = profile?.name?.let { Text.literal(it) }

            val text = displayName ?: profileName ?: continue
            tabLines.add(text.string.trim())
        }

        cachedTabLines = tabLines
    }

    /**
     * Returns a list of all PlayerListEntry objects from the current tab list.
     * Each PlayerListEntry object contains detailed information about a player.
     */
    fun getTabEntries(): List<PlayerListEntry?> {
        val client = mc
        val playerListCollection = client.player?.networkHandler?.playerList ?: return emptyList()
        return Collections.synchronizedCollection(playerListCollection).toList()
    }

    /**
     * Finds the value associated with a specific key in the tab list entries.
     * The key should be a prefix that appears at the start of the line in the tab list.
     * @param key The key to search for in the tab list entries.
     * @return The value associated with the key, or null if not found.
     */
    fun findInfo(key: String): String? {
        initOnce

        for (line in cachedTabLines) {
            if (line.startsWith(key)) {
                return line.substring(key.length).trim()
            }
        }

        return null
    }
}
