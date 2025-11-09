package net.sbo.mod.diana.sphinx

import net.sbo.mod.utils.Helper.removeFormatting
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import java.util.regex.Pattern

object SphinxSolver {
    val questions: MutableList<SphinxQuestion> = mutableListOf()

    fun init() {
        addQuestions()
        detectQuestion()
    }

    fun detectQuestion() {
        Register.onChatMessageCancable(
            Pattern.compile("^(.*?)$", Pattern.DOTALL)
        ) { message, matchResult ->
            val questionText = matchResult.group(1).trim()
            for (sphinxQuestion in questions) {
                if (sphinxQuestion.question.equals(questionText.removeFormatting(), ignoreCase = true)) {
                    Chat.chat("§6[SBO] §aCorrect answer: ${sphinxQuestion.answer}")
                }
            }
            true
        }
    }

    fun addQuestions() {
        questions.add(SphinxQuestion(
            "Which of these is NOT a pet?",
            "Slime"
        ))
        questions.add(SphinxQuestion(
            "What type of mob is exclusive to the Fishing Festival?",
            "Shark"
        ))
        questions.add(SphinxQuestion(
            "Where is Trevor the Trapper found?",
            "Mushroom Desert"
        ))
        questions.add(SphinxQuestion(
            "Who helps you apply Rod Parts?",
            "Roddy"
        ))
        questions.add(SphinxQuestion(
            "Which type of Gemstone has the lowest Breaking Power?",
            "Ruby"
        ))
        questions.add(SphinxQuestion(
            "Which item rarity comes after Mythic?",
            "Divine"
        ))
        questions.add(SphinxQuestion(
            "How do you obtain the Dark Purple Dye?",
            "Dark Auction"
        ))
        questions.add(SphinxQuestion(
            "Who runs the Chocolate Factory?",
            "Hoppity"
        ))
        questions.add(SphinxQuestion(
            "How many floors are there in The Catacombs?",
            "7"
        ))
        questions.add(SphinxQuestion(
            "What is the first type of slayer Maddox offers?",
            "Zombie"
        ))
        questions.add(SphinxQuestion(
            "What item do you use to kill Pests?",
            "Vacuum"
        ))
        questions.add(SphinxQuestion(
            "Who owns the Gold Essence Shop?",
            "Marigold"
        ))
        questions.add(SphinxQuestion(
            "Which of these is NOT a type of Gemstone?",
            "Prismite"
        ))
        questions.add(SphinxQuestion(
            "What does Junker Joel collect?",
            "Junk"
        ))
        questions.add(SphinxQuestion(
            "Where is the Titanoboa found?",
            "Backwater Bayou"
        ))
    }
}
