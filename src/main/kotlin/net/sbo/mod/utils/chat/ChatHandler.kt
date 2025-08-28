package net.sbo.mod.utils.chat

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.text.Text
import net.sbo.mod.settings.categories.Debug
import net.sbo.mod.utils.chat.ChatUtils.formattedString
import java.util.regex.Matcher
import java.util.regex.Pattern

object ChatHandler {

    private val messageHandlers = mutableListOf<ChatRule>()
    private val spammyPattern = Regex("§[0-9a-fk-or].+[0-9,]+\\/[0-9,]+❤.*")

    fun init () {
        ClientReceiveMessageEvents.ALLOW_GAME.register { message, overlay ->
            if (spammyPattern.matches(message.string)) return@register true
            processMessage(message)
        }
    }

    fun registerHandler(pattern: Pattern, action: (Text, Matcher) -> Boolean) {
        messageHandlers.add(ChatRule(pattern, action))
    }

    fun processMessage(message: Text): Boolean {
        val messageString = message.formattedString().replace("§r", "")
        if (messageString.contains("[SBO]")) return true
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