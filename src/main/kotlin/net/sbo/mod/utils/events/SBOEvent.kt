package net.sbo.mod.utils.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.sbo.mod.utils.events.impl.entity.EntityLoadEvent
import net.sbo.mod.utils.events.impl.entity.EntityUnloadEvent
import net.sbo.mod.utils.events.impl.game.DisconnectEvent
import net.sbo.mod.utils.events.impl.game.GameCloseEvent
import net.sbo.mod.utils.events.impl.game.WorldChangeEvent
import net.sbo.mod.utils.events.impl.guis.GuiCloseEvent
import kotlin.reflect.KClass

object SBOEvent {
    private val listeners = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()

    fun init () {
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
            emit(WorldChangeEvent(mc, world))
        }
        ClientPlayConnectionEvents.DISCONNECT.register { handler, client ->
            emit(DisconnectEvent(handler, client))
        }
        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            emit(GameCloseEvent(client))
        }
        ClientEntityEvents.ENTITY_LOAD.register { entity, world ->
            emit(EntityLoadEvent(entity, world))
        }
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, world ->
            emit(EntityUnloadEvent(entity, world))
        }
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            ScreenEvents.remove(screen).register {
                emit(GuiCloseEvent(client, screen, scaledWidth, scaledHeight))
            }
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
