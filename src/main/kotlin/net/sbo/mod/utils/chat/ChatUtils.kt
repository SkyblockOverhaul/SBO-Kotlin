package net.sbo.mod.utils.chat

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

object ChatUtils {
    private val colorToFormatChar: Map<TextColor, Formatting> = Formatting.entries.mapNotNull { format ->
        TextColor.fromFormatting(format)?.let { it to format }
    }.toMap()

    private fun getColorFormatChar(color: TextColor): Char? {
        val formatting = colorToFormatChar[color]
        return formatting?.code
    }

    private fun Style.getFormatCodes() = buildString {
        this@getFormatCodes.color?.let(ChatUtils::getColorFormatChar)?.run { append("§").append(this) }

        if (this@getFormatCodes.isBold) append("§l")
        if (this@getFormatCodes.isItalic) append("§o")
        if (this@getFormatCodes.isUnderlined) append("§n")
        if (this@getFormatCodes.isStrikethrough) append("§m")
        if (this@getFormatCodes.isObfuscated) append("§k")
    }

    fun Text.formattedString(): String {
        val builder = StringBuilder()

        this.visit(
            { style, content ->
                builder.append(style.getFormatCodes())
                builder.append(content)
                Optional.empty<Any>()
            },
            Style.EMPTY
        )
        return builder.toString()
    }

    internal fun Text.getShowTextHoverEvent(): HoverEvent? {
        val hover = this.style?.hoverEvent ?: return null
        if (hover is HoverEvent.ShowText) {
            return HoverEvent.ShowText(hover.value())
        }
        return null
    }

    internal fun String.toStyledText(click: ClickEvent?, hover: HoverEvent?): Text {
        return Text.literal(this).setStyle(
            Style.EMPTY
                .withClickEvent(click)
                .withHoverEvent(hover)
        )
    }

    internal fun Text.toClickableText(command: String): Text {
        val content = this.formattedString()
        val hover = this.getShowTextHoverEvent()
        val click = ClickEvent.RunCommand(command)
        return content.toStyledText(click, hover)
    }
}
