package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.diana.DianaTracker.announceLootToParty
import net.sbo.mod.overlays.DianaLoot
import net.sbo.mod.overlays.DianaMobs
import net.sbo.mod.overlays.InquisLoot
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.overlay.OverlayEditScreen
import net.sbo.mod.utils.waypoint.AdditionalHubWarps



object Diana : CategoryKt("Diana") {
    enum class SettingDiana {
        INSTASELL, SELLOFFER;

        fun next(): SettingDiana {
            return if (this == INSTASELL) {
                SELLOFFER
            } else {
                INSTASELL
            }
        }
    }

    enum class Tracker {
        OFF, TOTAL, EVENT, SESSION;

        fun next(): Tracker {
            val values = entries
            val currentIndex = ordinal

            if (currentIndex == 0) return values[0 + 1]
            if (this == values.last()) return TOTAL
            return values[currentIndex + 1]
        }
    }

    init {
        separator {
            this.title = "Diana Burrows"
        }
    }

    var dianaBurrowGuess by boolean(true) {
        this.name = Translated("Diana Burrow Guess")
        this.description = Translated("Guess the burrow location. Needs Driping Lava Partciles and set /particlequality to Extreme for more accuracy")
    }

    var dianaBurrowDetect by boolean(true) {
        this.name = Translated("Diana Burrow Detection")
        this.description = Translated("Detects Diana burrows | to reset waypoints /sboclearburrows")
    }

    init {
        separator {
            this.title = "Diana Warp"
        }
    }

    var allowedWarps by select(AdditionalHubWarps.WIZARD, AdditionalHubWarps.DA, AdditionalHubWarps.CASTLE) {
        this.name = Translated("Add Warps")
        this.description = Translated("Select the warps you want to be able to warp to with the guess and inquisitor warp keys.")
    }

    var dontWarpIfBurrowClose by boolean(true) {
        this.name = Translated("Don't Warp If a Burrow is nearby")
        this.description = Translated("If enabled, the warp key will not warp you if you are within 60 blocks of a burrow")
    }

    var warpDiff by int(10) {
        this.range = 0..60
        this.slider = true
        this.name = Translated("Warp Block Difference")
        this.description = Translated("The additional block difference to consider when warping to a waypoint. (0 to disable)")
    }

    var warpDelay by int(0) {
        this.range = 0..1000
        this.slider = true
        this.name = Translated("Warp Delay (<X>ms)")
        this.description = Translated("The delay bevor you can warp after guessing with spade. (0 to disable)")
    }

    init {
        separator {
            this.title = "Diana Tracker"
        }
    }

    var mobTracker by ObservableEntry(
        enum(Tracker.OFF) {
            this.name = Translated("Mob Tracker")
            this.description = Translated("Shows your Diana mob kills, /sboguis to move the overlay")
        }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                DianaMobs.updateLines()
            }
        }
    }

    var lootTracker by ObservableEntry( enum(Tracker.OFF) {
            this.name = Translated("Loot Tracker")
            this.description = Translated("Shows your Diana loot, /sboguis to move the overlay")
        }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                DianaLoot.updateLines()
            }
        }
    }

    var inquisTracker by ObservableEntry( enum(Tracker.OFF) {
            this.name = Translated("Inquis Loot Tracker")
            this.description = Translated("Shows your Inquisitor Loot so you see how lucky/unlucky you are (Shelmet/Plushie/Remedies), /sboguis to move the overlay")
        }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                InquisLoot.updateLines()
            }
        }
    }

    var statsTracker by boolean(false) {
        this.name = Translated("Diana Stats Tracker")
        this.description = Translated("Shows stats like Mobs since Inquisitor, Inquisitors since Chimera, /sboguis to move the overlay")
    }

    var magicFindTracker by boolean(false) {
        this.name = Translated("Magic Find Tracker")
        this.description = Translated("Shows your highest magic find for sticks and chimeras (only after you dropped it once), /sboguis to move the overlay")
    }

    var fourEyedFish by boolean(false) {
        this.name = Translated("Four-Eyed Fish")
        this.description = Translated("Set if you have a Four-Eyed Fish on your griffin pet")
    }

    var sendSinceMessage by boolean(true) {
        this.name = Translated("Stats Message")
        this.description = Translated("Sends the chat Message with stat: [SBO] Took 120 Mobs to get a Inquis!")
    }

    var bazaarSettingDiana by enum(SettingDiana.SELLOFFER) {
        this.name = Translated("Bazaar Setting")
        this.description = Translated("Bazaar setting to set the price for loot")
    }

    init {
        separator {
            this.title = "Diana Announcer"
        }
    }

    var lootAnnouncerChat by boolean(true) {
        this.name = Translated("Rare Drop Announcer")
        this.description = Translated("Announces relic/shelmet/plushie/remedies in chat")
    }

    var lootAnnouncerScreen by boolean(false) {
        this.name = Translated("Loot Screen Announcer")
        this.description = Translated("Announces chimera/stick/relic on screen")
    }

    var lootAnnouncerPrice by ObservableEntry(boolean(true) {
            this.name = Translated("Show Price Title")
            this.description = Translated("Shows chimera/stick/relic price as a subtitle on screen")
        }
    ) { old, new ->
        if (old != new) {
            if (new) {
                lootAnnouncerScreen = true
            }
        }
    }

    var lootAnnouncerParty by boolean(false) {
        this.name = Translated("Loot Party Announcer")
        this.description = Translated("Announces chimera/stick/relic and Shelmet/Plushie/Remedies (only when dropped from Inquisitor) in party chat")
    }

    var chimMessageBool by boolean(false) {
        this.name = Translated("Chim Message")
        this.description = Translated("Enables custom chim message")
    }

    var customChimMessage by strings("&6[SBO] &6&lRARE DROP! &d&lChimera! &b{mf} &b#{amount}") {
        this.name = Translated("Custom Chim Message Text")
        this.description = Translated("use: {mf} for MagicFind, {amount} for drop Amount this event and {percentage} for chimera/inquis ratio.")
    }

    init {
        button {
            title = "Send Test Chim Message"
            text = "Send Test"
            description = "Sends a test message for the chimera message"
            onClick {
                val customChimMsg = Helper.checkCustomChimMessage(400)
                if (customChimMsg.first) {
                    Chat.chat(customChimMsg.second)
                }
            }
        }
    }

    init {
        separator {
            this.title = "Diana Waypoints"
        }
    }

    var guessLine by boolean(true) {
        this.name = Translated("Guess Line")
        this.description = Translated("Draws line for guess, Disable View Bobbing in controls if its buggy")
    }

    var inqLine by boolean(true) {
        this.name = Translated("Inquisitor Line")
        this.description = Translated("Draws line for inquisitor, Disable View Bobbing in controls if its buggy")
    }

    var burrowLine by boolean(true) {
        this.name = Translated("Burrow Line")
        this.description = Translated("Draws line for burrow, Disable View Bobbing in controls if its buggy")
    }

    var dianaLineWidth by int(5) {
        this.range = 1..20
        this.slider = true
        this.name = Translated("Diana Line Width")
        this.description = Translated("The width of the lines drawn for Diana waypoints")
    }

    var removeGuessDistance by int(0) {
        this.range = 0..20
        this.slider = true
        this.name = Translated("Remove Guess When Close")
        this.description = Translated("Removes the guess waypoint when you are within this distance of it (0 to disable)")
    }

    init {
        separator {
            this.title = "Inquistor"
        }
    }

    var shareInq by boolean(true) {
        this.name = Translated("Share Inquisitor")
        this.description = Translated("Sends the coordinates of the inquisitor to party chat when it spawns")
    }

    var receiveInq by boolean(true) {
        this.name = Translated("Receive Inquisitor")
        this.description = Translated("Create a waypoint when someone in your party shares an inquisitor")
    }

    var allWaypointsAreInqs by boolean(false) {
        this.name = Translated("All Waypoints From Chat Are Inqs")
        this.description = Translated("All coordinates from chat are considered Inquisitor waypoints (only works in Hub and during Diana event)")
    }

    var announceKilltext by strings("") {
        this.name = Translated("Send Text On Inq Spawn")
        this.description = Translated("Sends a text on inq spawn 5 seconds after spawn, use {since} for mobs since inq, {chance} for inq chance")
    }

    init {
        button {
            title = "Send Test Inq Message"
            text = "Send Test"
            description = "Sends a test message for the inquisitor spawn message"
            onClick {
                Chat.chat(announceKilltext[0])
            }
        }
    }

    init {
        separator {
            this.title = "Other"
        }
    }

    var mythosMobHp by boolean(true) {
        this.name = Translated("Mythos Mob HP")
        this.description = Translated("Displays HP of mythological mobs near you. /sboguis to move it")
    }
}