package net.sbo.mod.general

import net.minecraft.text.ClickEvent.RunCommand
import net.minecraft.text.HoverEvent.ShowText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.sbo.mod.utils.Helper
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
        mapOf("cmd" to "sboimporttracker <profilename>", "desc" to "Import skyhanni tracker"), //todo: add sboimporttracker command
        mapOf("cmd" to "sboimporttrackerundo", "desc" to "Undo the tracker import"), // todo: add sboimporttrackerundo command
        mapOf("cmd" to "sbodc", "desc" to "Diana dropchances"),
        mapOf("cmd" to "sbopartyblacklist", "desc" to "Party commands blacklisting"), // todo: add sbopartyblacklist command
        mapOf("cmd" to "sbobacktrackachievements", "desc" to "Backtrack achievements"),
        mapOf("cmd" to "sboachievements", "desc" to "Opens the achievements GUI"),
        mapOf("cmd" to "sbolockachievements", "desc" to "Locks all Achievements (needs confirmation)"),
        mapOf("cmd" to "sbopde", "desc" to "Opens the Past Diana Events GUI"),
        mapOf("cmd" to "sboactiveuser", "desc" to "Shows the active user of the mod"), // todo: add sboactiveuser command
        mapOf("cmd" to "sbopf", "desc" to "Opens the PartyFinder GUI"),
        mapOf("cmd" to "sbopartycommands", "desc" to "Displays all diana partycommands"), // todo: add sbopartycommands command
        mapOf("cmd" to "sboresetavgmftracker", "desc" to "Resets the avg mf tracker"), // todo: add sboresetavgmftracker command
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
        dropChances()
    }

    fun dropChances() {
        Register.command("sbodc", "sbodropchances") { args ->
            if (args.size < 2) {
                Chat.chat("§6[SBO] §ePlease provide mf/looting values. /sbodc <mf> <looting>")
                return@command
            }

            val mf = args[0].toIntOrNull()
            val looting = args[1].toIntOrNull()
            if (mf == null || looting == null) {
                Chat.chat("§6[SBO] §ePlease provide valid numbers. /sbodc 500 5")
                return@command
            }

            val items = listOf("Chimera" to "chim", "Stick" to "stick", "Relic" to "relic")
            val normalChances = Helper.getChance(mf, looting)
            val lsChances = Helper.getChance(mf, looting, true)

            (listOf(false, true)).forEach { isLs ->
                val chances = if (isLs) lsChances else normalChances
                val labelFunc: (String) -> String = if (isLs) { _ -> "§7[MF:$mf]" } else { _ -> Helper.getMagicFindAndLooting(mf, looting) }

                items.forEach { (name, key) ->
                    val chance = chances[key] ?: 0.0
                    val lsPrefix = if (isLs) "§7[§bLS§7] " else ""
                    Chat.chat("§6[SBO] $lsPrefix§e$name ${Helper.formatChances(chance, labelFunc(name))}")
                }
            }
        }
    }

}