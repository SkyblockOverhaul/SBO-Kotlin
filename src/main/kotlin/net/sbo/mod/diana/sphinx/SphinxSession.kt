package net.sbo.mod.diana.sphinx

import net.minecraft.text.Text

data class SphinxSession(
    val expectedAnswers: Int = 3, // A, B, C
    val answerTexts: MutableMap<String, Text> = mutableMapOf(),
    var correctAnswersIndex: Int = -1 // 0 for A, 1 for B, 2 for C
) {
    /**
     * Check if all expected answers have been provided for this session.
     */
    fun isComplete(): Boolean {
        return answerTexts.size == expectedAnswers
    }
}
