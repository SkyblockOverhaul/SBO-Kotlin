package net.sbo.mod.guis

import gg.essential.universal.UScreen
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.guis.partyfinder.PartyFinderGUI
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.events.EventBus
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.game.World

object Main {
    private var partyFinderGui: PartyFinderGUI? = null

    fun register() {
        Register.command("sbopf") {
            if (!World.isInSkyblock()) {
                Chat.chat("§6[SBO] §cYou can only use this command in Skyblock.")
                return@command
            }
            mc.send {
                if (partyFinderGui == null) {
                    partyFinderGui = PartyFinderGUI()
                }
                UScreen.displayScreen(partyFinderGui!!)
                EventBus.emit("gui_opened")
            }
        }
    }
}