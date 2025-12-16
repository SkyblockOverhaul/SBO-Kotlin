package net.sbo.mod.utils.collection

import com.google.common.cache.RemovalCause
import kotlin.time.Duration

/**
 * A [MutableSet] implementation backed by a time-limited cache.
 *
 * Elements in this set will expire and be removed after a specified duration
 * since their last write (addition).
 *
 * @param T The type of elements in the set.
 * @param expireAfterWrite The duration after which an element should expire.
 * @param removalListener An optional listener that gets notified when an element is removed,
 *                        along with the cause of removal.
 *
 * Credits to this go fully to SkyHanni
 */
class TimeLimitedSet<T : Any>(
    expireAfterWrite: Duration,
    removalListener: ((T?, RemovalCause) -> Unit)? = null,
) : CacheSet<T>() {

    override val cache = TimeLimitedCache<T, Unit>(
        expireAfterWrite,
        removalListener.toMapListener(),
    )
}