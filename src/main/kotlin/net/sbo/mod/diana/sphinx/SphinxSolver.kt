package net.sbo.mod.diana.sphinx

import net.minecraft.client.gui.screen.ChatScreen
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.chat.ChatUtils.createStyledAnswerText
import net.sbo.mod.utils.chat.ChatUtils.toClickableText
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.guis.GuiMouseClickBefore
import java.util.regex.Pattern

object SphinxSolver {
    private var currentSession: SphinxSession? = null

    fun init() {
        detectQuestion()
    }

    @SboEvent
    fun onGuiMouseClick(event: GuiMouseClickBefore) {
        if (!Diana.sphinxSolver) return

        val index = currentSession?.correctAnswersIndex ?: return
        if (event.button != 0) return
        if (index == -1) return
        if (event.screen !is ChatScreen) return
        Chat.command("/sphinxanswer $index")
        currentSession = null
    }


    fun detectQuestion() {
        Register.onChatMessageCancable(
            Pattern.compile("^§7 {3}([ABC])\\) §f(.*?)$")
        ) { msg, matcher ->
            if (!Diana.sphinxSolver) return@onChatMessageCancable true

            val letter = matcher.group(1)
            val possibleAnswer = matcher.group(2).trim()
            val index = letterToIndex(letter)

            val isCorrect = possibleAnswer in SphinxQuestions.CORRECT_ANSWERS
            val session = currentSession ?: SphinxSession().also { currentSession = it }

            if (isCorrect) {
                session.correctAnswersIndex = index
            }

            val formattedMsg = msg.createStyledAnswerText(possibleAnswer, isCorrect)
            session.answerTexts[letter] = formattedMsg

            if (session.isComplete()) {
                handleSessionComplete(session)
            }
            false
        }
    }

    private fun letterToIndex(letter: String): Int = when (letter) {
        "A" -> 0
        "B" -> 1
        "C" -> 2
        else -> -1
    }

    private fun handleSessionComplete(session: SphinxSession) {
        if (session.correctAnswersIndex == -1) return

        for (msg in session.answerTexts.values) {
            val clickableText = msg.toClickableText("/sphinxanswer ${session.correctAnswersIndex}")
            Chat.chat(clickableText)
        }
    }

}
