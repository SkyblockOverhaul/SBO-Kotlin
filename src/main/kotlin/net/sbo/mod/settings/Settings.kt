package net.sbo.mod.settings

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt
import net.minecraft.util.Util
import net.sbo.mod.SBOKotlin
import net.sbo.mod.settings.categories.General
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.settings.categories.PartyCommands
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.settings.categories.QOL
import net.sbo.mod.settings.categories.Credits
import net.sbo.mod.settings.categories.Debug
import net.sbo.mod.settings.categories.PartyFinder

object Settings : ConfigKt("sbo/config") {
    override val name: TranslatableValue
        get() = Literal("SBO v${SBOKotlin.version}")
    override val description = Literal("Skyblock Overhaul — Mod for the Mythological Ritual event in hypixel skyblock and custom partyfinder")

    init {
        separator {
            title = "Welcome to Skyblock Overhaul!"
            description = "Made by D4rkSwift/RolexDE and contributors."
        }

        button {
            title = "Check for Updates"
            description = "Opens the GitHub releases page"
            text = "Open"
            onClick {
                Util.getOperatingSystem().open("https://github.com/SkyblockOverhaul/SBO-Kotlin/releases")
            }
        }

        button {
            title = "Join Discord"
            description = "Get support and updates on Discord"
            text = "Join"
            onClick {
                Util.getOperatingSystem().open("https://discord.gg/QvM6b9jsJD")
            }
        }

        category(General)
        category(Diana)
        category(PartyCommands)
        category(Customization)
        category(PartyFinder)
        category(QOL)
        category(Credits)
        category(Debug)
    }

    fun save() = SBOKotlin.settings.save()
}