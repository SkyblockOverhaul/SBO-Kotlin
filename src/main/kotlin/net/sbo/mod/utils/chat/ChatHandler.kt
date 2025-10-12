package net.sbo.mod.utils.chat

import net.minecraft.text.Text
import net.sbo.mod.settings.categories.Debug
import net.sbo.mod.utils.chat.ChatUtils.formattedString
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.game.ChatMessageAllowEvent
import java.util.regex.Matcher
import java.util.regex.Pattern

object ChatHandler {

    private val messageHandlers = mutableListOf<ChatRule>()

    @SboEvent
    fun onAllowMessage(event: ChatMessageAllowEvent) {
        event.isAllowed = processMessage(event.message)
    }

    fun registerHandler(pattern: Pattern, action: (Text, Matcher) -> Boolean) {
        messageHandlers.add(ChatRule(pattern, action))
    }

    fun processMessage(message: Text): Boolean {
        val messageString = message.formattedString().replace("§r", "")
        if (Debug.debugMessages && !messageString.contains("❈ Defense")) {
            println("Processing chat message: $messageString")
        }

        var allowMessage = true

        messageHandlers.forEach { rule ->
            val matcher = rule.pattern.matcher(messageString)
            if (matcher.find()) {
                val result = rule.action(message, matcher)
                if (!result) allowMessage = false
            }
        }

        return allowMessage
    }

    private data class ChatRule(
        val pattern: Pattern,
        val action: (Text, Matcher) -> Boolean
    )
}