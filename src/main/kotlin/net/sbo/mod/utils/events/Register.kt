package net.sbo.mod.utils.events

import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.Helper.removeFormatting
import net.sbo.mod.utils.chat.ChatHandler
import net.sbo.mod.utils.chat.ChatUtils.formattedString
import net.sbo.mod.utils.events.TickScheduler.ScheduledTask
import net.sbo.mod.utils.events.TickScheduler.tasks
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Utility object for registering events
 */
object Register {
    /**
     * Registers a command with the specified name and aliases.
     * The action is executed when the command is invoked, with the provided arguments.
     *
     * @param name The name of the command.
     * @param aliases Optional aliases for the command.
     * @param action The action to execute when the command is invoked.
     */
    fun command(
        name: String,
        vararg aliases: String,
        action: (Array<String>) -> Unit
    ) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->

            fun createLiteral(commandName: String) =
                ClientCommandManager.literal(commandName)
                    .executes {
                        action(emptyArray())
                        1
                    }
                    .then(
                        ClientCommandManager.argument("args", StringArgumentType.greedyString())
                            .executes {
                                val argsString = StringArgumentType.getString(it, "args")
                                val args = argsString.split(' ').filter { s -> s.isNotEmpty() }.toTypedArray()
                                action(args)
                                1
                            }
                    )

            dispatcher.register(createLiteral(name))

            aliases.forEach { alias ->
                dispatcher.register(createLiteral(alias))
            }
        }
    }

    /**
     * Registers a tick event that executes an action every specified number of ticks.
     * @param tick The number of ticks after which the action should be executed.
     * @param action The action to execute. It receives a lambda to unregister itself.
     */
    fun onTick(tick: Int, action: (unregister: () -> Unit) -> Unit) {
        tasks += ScheduledTask(tick, action)
    }

    /**
     * Registers an event that listens for chat messages.
     * The action receives the message as a `Text` object.
     */
    fun onChatMessage(
        action: (message: Text) -> Unit
    ) {
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            action(message)
        }
    }

    /**
     * Registers an event that listens for chat messages that match a regex.
     * The action receives both the message and the regex match result for easy value extraction.
     *
     * @param regex The regular expression to filter messages with.
     * @param action The action to execute. It receives the message and the `MatchResult`.
     */
    fun onChatMessage(
        regex: Regex,
        noFormatting: Boolean = false,
        action: (message: Text, matchResult: MatchResult) -> Unit
    ) {
        ClientReceiveMessageEvents.GAME.register { message, _ ->
            var text = message.formattedString()

            if (noFormatting) text = text.removeFormatting()

            regex.find(text)?.let { result ->
                action(message, result)
            }
        }
    }

    /**
     * Registers an event that listens for chat messages that match a regex.
     * The action receives both the message and the regex matcher for easy value extraction.
     * If the action returns true, the message will be cancelled (not displayed in chat).
     *
     * @param regex The regular expression to filter messages with.
     * @param action The action to execute. It receives the message and the `Matcher`.
     */
    fun onChatMessageCancable(
        regex: Pattern,
        action: (message: Text, matchResult: Matcher) -> Boolean
    ) {
        ChatHandler.registerHandler(regex, action)
    }

    /**
     * Registers an event that listens for GUI close events and executes an action.
     * @param action The action to execute when a GUI is closed.
     */
    fun onGuiClose(action: (screen: Screen) -> Unit) {
        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            ScreenEvents.remove(screen).register {
                action(screen)
            }
        }
    }

    fun onEntityLoad(action: (entity: Entity, clientWorld: ClientWorld) -> Unit) {
        ClientEntityEvents.ENTITY_LOAD.register(action)
    }

    fun onEntityUnload(action: (entity: Entity, clientWorld: ClientWorld) -> Unit) {
        ClientEntityEvents.ENTITY_UNLOAD.register(action)
    }
}