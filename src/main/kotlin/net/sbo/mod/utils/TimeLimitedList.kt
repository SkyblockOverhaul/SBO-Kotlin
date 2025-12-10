package net.sbo.mod.utils

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import java.time.Duration as JavaDuration

/**
 * A concurrent, time-limited list (or cache) that automatically expires elements
 * older than the specified time limit.
 *
 * @param timeLimit The maximum duration an element should remain in the list.
 */
class TimeLimitedList<T>(private val timeLimit: Duration) {
    private val data: ConcurrentHashMap<T, Instant> = ConcurrentHashMap()
    private val javaTimeLimit: JavaDuration = timeLimit.toJavaDuration()
    /**
     * Adds an element to the list with the current timestamp.
     *
     * @param element The element to add.
     */
    fun add(element: T): Boolean {
        cleanup()
        data[element] = Instant.now()
        return true
    }

    /**
     * Returns a current view of all active (non-expired) elements in the list.
     * Note: This operation implicitly triggers a cleanup.
     *
     * @return A List of all active elements.
     */
    fun getActiveItems(): List<T> {
        cleanup()
        return data.keys().toList()
    }

    /**
     * Checks if the list contains the specified element.
     * Note: This operation implicitly triggers a cleanup.
     *
     * @param item The element to check for.
     * @return True if the element is present and has not expired, false otherwise.
     */
    operator fun contains(item: T): Boolean {
        cleanup()
        val timestamp = data[item] ?: return false
        // Re-check for expiration, though cleanup should have handled most cases.
        return timestamp.isAfter(Instant.now().minus(javaTimeLimit))
    }

    /**
     * Removes all expired elements from the internal map.
     */
    private fun cleanup() {
        val cutoff = Instant.now().minus(javaTimeLimit)

        // Use an iterator to safely remove items during iteration
        val iterator = data.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.isBefore(cutoff)) {
                iterator.remove()
            }
        }
    }
}