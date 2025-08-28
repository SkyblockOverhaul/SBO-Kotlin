package net.sbo.mod.general

import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.events.Register

object HelpCommand {
    val commands = arrayOf(
        mapOf("cmd" to "sbo", "desc" to "Open the Settings GUI"),
        mapOf("cmd" to "sbohelp", "desc" to "Shows this message"),
        mapOf("cmd" to "sboguis", "desc" to "Open the GUIs and move them around (or: /sbomoveguis)"),
        mapOf("cmd" to "sboclearburrows", "desc" to "Clear all burrow waypoints (or: /sbocb)"),
        mapOf("cmd" to "sbocheck <player>", "desc" to "Check a player (or: /sboc <player>)"),
        mapOf("cmd" to "sbocheckp", "desc" to "Check your party (alias /sbocp)"),
        mapOf("cmd" to "sboimporttracker <profilename>", "desc" to "Import skyhanni tracker"),
        mapOf("cmd" to "sboimporttrackerundo", "desc" to "Undo the tracker import"),
        mapOf("cmd" to "sbodc", "desc" to "Diana dropchances"),
        mapOf("cmd" to "sbopartyblacklist", "desc" to "Party commands blacklisting"),
        mapOf("cmd" to "sbobacktrackachievements", "desc" to "Backtrack achievements"),
        mapOf("cmd" to "sboachievements", "desc" to "Opens the achievements GUI"),
        mapOf("cmd" to "sbolockachievements", "desc" to "Locks all Achievements (needs confirmation)"),
        mapOf("cmd" to "sbopde", "desc" to "Opens the Past Diana Events GUI"),
        mapOf("cmd" to "sboactiveuser", "desc" to "Shows the active user of the mod"),
        mapOf("cmd" to "sbopf", "desc" to "Opens the PartyFinder GUI"),
        mapOf("cmd" to "sbopartycommands", "desc" to "Displays all diana partycommands"),
        mapOf("cmd" to "sboresetavgmftracker", "desc" to "Resets the avg mf tracker"),
        mapOf("cmd" to "sboresetstatstracker", "desc" to "Resets the stats tracker"),
        mapOf("cmd" to "sboKey", "desc" to "Set your sbokey"),
        mapOf("cmd" to "sboClearKey", "desc" to "Reset your sbokey")
    )

    fun init() {
        Register.command("sbohelp") {
            val headerText = Text.literal("[SBO] ")
                .formatted(Formatting.GOLD)
                .append(Text.literal("Commands:").formatted(Formatting.YELLOW))

            Chat.chat(headerText)

            commands.forEach { command ->
                val cmd = command["cmd"]!!
                val description = command["desc"]!!

                val commandToRun = if (cmd.contains(" ")) cmd.substringBefore(" ") else cmd

                val fullLineText = Text.literal("> ").formatted(Formatting.GRAY)
                    .append(Text.literal("/$cmd").formatted(Formatting.GREEN))
                    .append(Text.literal(" - ").formatted(Formatting.GRAY))
                    .append(Text.literal(description).formatted(Formatting.YELLOW))

                val styledText = fullLineText.setStyle(
                    Style.EMPTY
                        .withClickEvent(RunCommand("/$commandToRun"))
                        .withHoverEvent(
                            ShowText(
                                Text.literal("Click to run /$commandToRun").formatted(Formatting.GRAY)
                            )
                        )
                )

                Chat.chat(styledText)
            }
        }
    }
}