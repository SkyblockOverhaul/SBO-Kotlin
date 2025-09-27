package net.sbo.mod.qol

import net.sbo.mod.settings.categories.QOL
import net.sbo.mod.utils.events.Register
import java.util.regex.Pattern

object MessageHider {
    fun init() {
        general()
        diana()
    }

    fun general() {
        Register.onChatMessageCancelable(
            Pattern.compile(
                "^§cAutopet §eequipped your (.*?) §a§lVIEW RULE$",
                Pattern.DOTALL
            )
        ) { message, matchResult ->
            !QOL.hideAutoPetMSG
        }
        Register.onChatMessageCancelable(
            Pattern.compile(
                "^§7Your Implosion hit (.*?) §7damage\\.$",
                Pattern.DOTALL
            )
        ) { message, matchResult ->
            !QOL.hideImplosionMSG
        }
    }

    fun diana() {
        Register.onChatMessageCancelable(
            Pattern.compile(
                "^§eFollow the arrows to find the §6treasure§e!$",
                Pattern.DOTALL
            )
        ) { message, matchResult ->
            !QOL.dianaMessageHider
        }
        Register.onChatMessageCancelable(
            Pattern.compile(
                "^§cThis ability is on cooldown for (.*?)$",
                Pattern.DOTALL
            )
        ) { message, matchResult ->
            !QOL.dianaMessageHider
        }
        Register.onChatMessageCancelable(
            Pattern.compile(
                "^§7Warping\\.\\.\\.$",
                Pattern.DOTALL
            )
        ) { message, matchResult ->
            !QOL.dianaMessageHider
        }
        Register.onChatMessageCancelable(
            Pattern.compile(
                "^§cThere are blocks in the way!$",
                Pattern.DOTALL
            )
        ) { message, matchResult ->
            !QOL.dianaMessageHider
        }
    }
}