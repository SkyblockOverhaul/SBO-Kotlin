package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import net.sbo.mod.overlays.DianaLoot
import net.sbo.mod.overlays.DianaMobs
import net.sbo.mod.overlays.InquisLoot
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.chat.Chat
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
            title = "Diana Burrows"
        }
    }

    var dianaBurrowGuess by boolean(true) {
        name = Translated("Diana Burrow Guess")
        description =
            Translated("Guess the burrow location. Needs Driping Lava Partciles and set /particlequality to Extreme for more accuracy")
    }

    var dianaMultiBurrowGuess by boolean(false) {
        name = Translated("Diana Multi Burrow Guess")
        description =
            Translated("Multi guess the burrow location. Needs Driping Lava Partciles and set /particlequality to Extreme for more accuracy")
    }

    var dianaBurrowDetect by boolean(true) {
        name = Translated("Diana Burrow Detection")
        description = Translated("Detects Diana burrows | to reset waypoints /sboclearburrows")
    }

    init {
        separator {
            title = "Diana Warp"
        }
    }

    var focusedWarp by boolean(false) {
        name = Translated("Focused Warp")
        description = Translated("If enabled, you warp to the guess you look at instead of the closest one")
    }

    var allowedWarps by select(AdditionalHubWarps.WIZARD, AdditionalHubWarps.DA, AdditionalHubWarps.CASTLE) {
        name = Translated("Add Warps")
        description =
            Translated("Select the warps you want to be able to warp to with the guess and inquisitor warp keys.")
    }

    var dontWarpIfBurrowClose by boolean(true) {
        name = Translated("Don't Warp If a Burrow is nearby")
        description =
            Translated("If enabled, the warp key will not warp you if you are within 60 blocks of a burrow")
    }

    var warpDiff by int(10) {
        range = 0..60
        slider = true
        name = Translated("Warp Block Difference")
        description =
            Translated("The additional block difference to consider when warping to a waypoint. (0 to disable)")
    }

    var warpDelay by int(0) {
        range = 0..1000
        slider = true
        name = Translated("Warp Delay (<X>ms)")
        description = Translated("The delay bevor you can warp after guessing with spade. (0 to disable)")
    }

    init {
        separator {
            title = "Diana Tracker"
        }
    }

    var mobTracker by ObservableEntry(
        enum(Tracker.OFF) {
            name = Translated("Mob Tracker")
            description = Translated("Shows your Diana mob kills, /sboguis to move the overlay")
        }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                DianaMobs.updateLines()
            }
        }
    }

    var lootTracker by ObservableEntry(enum(Tracker.OFF) {
        name = Translated("Loot Tracker")
        description = Translated("Shows your Diana loot, /sboguis to move the overlay")
    }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                DianaLoot.updateLines()
            }
        }
    }

    var inquisTracker by ObservableEntry(enum(Tracker.OFF) {
        name = Translated("Inquis Loot Tracker")
        description = Translated("Shows your Inquisitor Loot so you see how lucky/unlucky you are (Shelmet/Plushie/Remedies), /sboguis to move the overlay")
    }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                InquisLoot.updateLines()
            }
        }
    }

    var statsTracker by boolean(false) {
        name = Translated("Diana Stats Tracker")
        description = Translated("Shows stats like Mobs since Inquisitor, Inquisitors since Chimera, /sboguis to move the overlay")
    }

    var magicFindTracker by boolean(false) {
        name = Translated("Magic Find Tracker")
        description = Translated("Shows your highest magic find for sticks and chimeras (only after you dropped it once), /sboguis to move the overlay")
    }

    var fourEyedFish by boolean(false) {
        name = Translated("Four-Eyed Fish")
        description = Translated("Set if you have a Four-Eyed Fish on your griffin pet")
    }

    var sendSinceMessage by boolean(true) {
        name = Translated("Stats Message")
        description = Translated("Sends the chat Message with stat: [SBO] Took 120 Mobs to get a Inquis!")
    }

    var bazaarSettingDiana by enum(SettingDiana.SELLOFFER) {
        name = Translated("Bazaar Setting")
        description = Translated("Bazaar setting to set the price for loot")
    }

    init {
        separator {
            title = "Diana Announcer"
        }
    }

    var lootAnnouncerChat by boolean(true) {
        name = Translated("Rare Drop Announcer")
        description = Translated("Announces relic/shelmet/plushie/remedies in chat")
    }

    var lootAnnouncerScreen by boolean(false) {
        name = Translated("Loot Screen Announcer")
        description = Translated("Announces chimera/stick/relic on screen")
    }

    var lootAnnouncerPrice by ObservableEntry(boolean(true) {
        name = Translated("Show Price Title")
        description = Translated("Shows chimera/stick/relic price as a subtitle on screen")
    }
    ) { old, new ->
        if (old != new) {
            if (new) {
                lootAnnouncerScreen = true
            }
        }
    }

    var lootAnnouncerParty by boolean(false) {
        name = Translated("Loot Party Announcer")
        description = Translated("Announces chimera/stick/relic and Shelmet/Plushie/Remedies (only when dropped from Inquisitor) in party chat")
    }

    var chimMessageBool by boolean(false) {
        name = Translated("Chim Message")
        description = Translated("Enables custom chim message")
    }

    var customChimMessage by strings("&6[SBO] &6&lRARE DROP! &d&lChimera! &b{mf} &b#{amount}") {
        name = Translated("Custom Chim Message Text")
        description = Translated("use: {mf} for MagicFind, {amount} for drop Amount this event and {percentage} for chimera/inquis ratio.")
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
            title = "Diana Waypoints"
        }
    }

    var guessLine by boolean(true) {
        name = Translated("Guess Line")
        description = Translated("Draws line for guess, Disable View Bobbing in controls if its buggy")
    }

    var inqLine by boolean(true) {
        name = Translated("Inquisitor Line")
        description = Translated("Draws line for inquisitor, Disable View Bobbing in controls if its buggy")
    }

    var burrowLine by boolean(true) {
        name = Translated("Burrow Line")
        description = Translated("Draws line for burrow, Disable View Bobbing in controls if its buggy")
    }

    var dianaLineWidth by int(5) {
        range = 1..20
        slider = true
        name = Translated("Diana Line Width")
        description = Translated("The width of the lines drawn for Diana waypoints")
    }

    var removeGuessDistance by int(0) {
        range = 0..20
        slider = true
        name = Translated("Remove Guess When Close")
        description = Translated("Removes the guess waypoint when you are within this distance of it (0 to disable)")
    }

    init {
        separator {
            title = "Inquistor"
        }
    }

    var shareInq by boolean(true) {
        name = Translated("Share Inquisitor")
        description = Translated("Sends the coordinates of the inquisitor to party chat when it spawns")
    }

    var receiveInq by boolean(true) {
        name = Translated("Receive Inquisitor")
        description = Translated("Create a waypoint when someone in your party shares an inquisitor")
    }

    var allWaypointsAreInqs by boolean(false) {
        name = Translated("All Waypoints From Chat Are Inqs")
        description = Translated("All coordinates from chat are considered Inquisitor waypoints (only works in Hub and during Diana event)")
    }

    var announceKilltext by strings("") {
        name = Translated("Send Text On Inq Spawn")
        description = Translated("Sends a text on inq spawn 5 seconds after spawn, use {since} for mobs since inq, {chance} for inq chance")
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
            title = "Other"
        }
    }

    var mythosMobHp by boolean(true) {
        name = Translated("Mythos Mob HP")
        description = Translated("Displays HP of mythological mobs near you. /sboguis to move it")
    }
}