package net.sbo.mod.utils.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.sbo.mod.utils.events.impl.WorldChangeEvent
import kotlin.reflect.KClass

object SBOEvent {
    private val listeners = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()

    fun init () {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
            emit(WorldChangeEvent(mc, world))
        }
    }

    /** Register a listener for a specific event type. */
    fun <T : Any> on(eventType: KClass<T>, callback: (T) -> Unit) {
        val callbacks = listeners.getOrPut(eventType) { mutableListOf() }
        @Suppress("UNCHECKED_CAST")
        callbacks.add(callback as (Any) -> Unit)
    }

    /** Emit an event to all registered listeners. */
    fun emit(event: Any) {
        listeners[event::class]?.forEach { callback ->
            callback(event)
        }
    }

    /** Convenience inline version for type inference. */
    inline fun <reified T : Any> on(noinline callback: (T) -> Unit) {
        on(T::class, callback)
    }
}
