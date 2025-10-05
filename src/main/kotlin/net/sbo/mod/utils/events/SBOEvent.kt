package net.sbo.mod.utils.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.sbo.mod.utils.events.impl.entity.EntityLoadEvent
import net.sbo.mod.utils.events.impl.entity.EntityUnloadEvent
import net.sbo.mod.utils.events.impl.game.ChatMessageAllowEvent
import net.sbo.mod.utils.events.impl.game.ChatMessageEvent
import net.sbo.mod.utils.events.impl.game.DisconnectEvent
import net.sbo.mod.utils.events.impl.game.GameCloseEvent
import net.sbo.mod.utils.events.impl.game.WorldChangeEvent
import net.sbo.mod.utils.events.impl.guis.GuiCloseEvent
import kotlin.reflect.KClass

object SBOEvent {
    private val listeners = mutableMapOf<KClass<*>, MutableList<(Any) -> Unit>>()

    /**
     * Initialize the event system by registering Fabric events to emit custom events.
     * This should be called once during mod initialization.
     */
    fun init () {
        /**
         * World Change Event
         * Fired after the client world changes (e.g., when joining a new world or server).
         */
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register { mc, world ->
            emit(WorldChangeEvent(mc, world))
        }
        /**
         * Disconnect Event
         * Fired when the client disconnects from a server.
         */
        ClientPlayConnectionEvents.DISCONNECT.register { handler, client ->
            emit(DisconnectEvent(handler, client))
        }
        /**
         * Game Close Event
         * Fired when the Minecraft client is stopping.
         */
        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            emit(GameCloseEvent(client))
        }
        /**
         * Entity Load/Unload Events
         * Fired when an entity is loaded into or unloaded from the client world.
         */
        ClientEntityEvents.ENTITY_LOAD.register { entity, world ->
            emit(EntityLoadEvent(entity, world))
        }
        ClientEntityEvents.ENTITY_UNLOAD.register { entity, world ->
            emit(EntityUnloadEvent(entity, world))
        }
        /**
         * Chat Message Event
         * Fired when a chat message is received.
         */
        ClientReceiveMessageEvents.GAME.register { message, signed ->
            emit(ChatMessageEvent(message, signed))
        }
        /**
         * Chat Message Allow Event
         * Fired to determine if a chat message should be displayed.
         * Allows for filtering of spammy messages.
         */
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, signed ->
            val event = ChatMessageAllowEvent(message, signed, true)
            emit(event)
            event.isAllowed
        }

        /**
         * GUI Close Event
         * Fired when a GUI screen is closed.
         */
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
