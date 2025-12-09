package net.sbo.mod.diana.sphinx

import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.Helper.removeFormatting
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.chat.ChatUtils.formattedString
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.guis.GuiMouseClick
import java.net.URI
import java.net.URISyntaxException
import java.util.regex.Pattern

object SphinxSolver {
    val questions: MutableList<SphinxQuestion> = mutableListOf()
    var aText: Text = Text.of("")
    var bText: Text = Text.of("")
    var cText: Text = Text.of("")
    var r: Int = -1
    var timerOn: Boolean = false;

    var correctAnswers: MutableList<String?> = mutableListOf<String?>(
        "Slime",
        "Shark",
        "Mushroom Desert",
        "Roddy",
        "Ruby",
        "Divine",
        "Dark Auction",
        "Hoppity",
        "7",
        "Zombie",
        "Vacuum",
        "Marigold",
        "Prismite",
        "Junk",
        "Backwater Bayou"
    )


    fun init() {
        addQuestions()
        detectQuestion()
    }

    @SboEvent
    fun clickChat(event: GuiMouseClick) {
        if (event.button == 0 && r != -1 && event.screen is ChatScreen) {
            Chat.command("/sphinxanswer " + r)
            r = -1
        }
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

        Register.onChatMessageCancable(
            Pattern.compile("^§7 {3}([ABC])\\) §f(.*?)$")
        ) { msg, matcher ->
                var msg: Text = msg
                timer()
                val possibleAnswer: String? = matcher.group(2).trim()
                msg = stringToText(
                    msg.formattedString().replace(
                        "§f",
                        if (correctAnswers.contains(possibleAnswer)) "§a" else "§c"
                    ),
                    stringToClickEvent("", ""),
                    getFullHoverEvent(msg)
                )
                when (matcher.group(1)) {
                    "A" -> {
                        r = if (correctAnswers.contains(possibleAnswer)) 0 else r
                        aText = msg
                    }

                    "B" -> {
                        r = if (correctAnswers.contains(possibleAnswer)) 1 else r
                        bText = msg
                    }

                    "C" -> {
                        r = if (correctAnswers.contains(possibleAnswer)) 2 else r
                        cText = msg
                    }
                }
                false
            }
    }

    fun timer() {
        if (timerOn) return
        timerOn = true
        Helper.sleep(100, {
            for (msg in arrayOf<Text?>(
                aText,
                bText,
                cText
            )) {
                Chat.chat(
                    stringToText(
                        msg?.formattedString() ?: "",
                        stringToClickEvent("", "/sphinxanswer " + r),
                        getFullHoverEvent(msg)
                    )
                )
            }
            timerOn = false
        })
    }

    fun stringToText(s: String, click: ClickEvent?, hover: HoverEvent?): Text {
        return Text.literal(s).setStyle(
            Style.EMPTY
                .withClickEvent(click)
                .withHoverEvent(hover)
        )
    }

    /**
     * Convert string to ClickEvent
     * @param type
     * @param command
     * @return ClickEvent
     */
    fun stringToClickEvent(type: String, command: String): ClickEvent {
        return ClickEvent.RunCommand(command)
    }

    /**
     * Get full HoverEvent from Text
     * @param t
     * @return HoverEvent
     */
    fun getFullHoverEvent(t: Text?): HoverEvent? {
        if (getHoverEvent(t) == null) return null
        else return stringToHoverEvent(getHoverEvent(t)?.get("value"))
    }

    /**
     * Convert string to HoverEvent
     * @param hover
     * @return HoverEvent
     */
    fun stringToHoverEvent(hover: String?): HoverEvent {
        return HoverEvent.ShowText(Text.of(hover))
    }

    /**
     * Get HoverEvent details from Text
     * @param message
     * @return Map<String></String>, String>
     */
    fun getHoverEvent(message: Text?): MutableMap<String?, String?>? {
        val hover = message?.getStyle()?.getHoverEvent()
        if (hover == null) return null

        val result: MutableMap<String?, String?> = HashMap<String?, String?>()

        if (hover is HoverEvent.ShowText) {
            val text = hover.value()
            result.put("action", "show_text")
            result.put("value", text.formattedString())
        }

        return result
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
