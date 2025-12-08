package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ObservableEntry
import net.sbo.mod.overlays.DianaLoot
import net.sbo.mod.overlays.DianaMobs
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.waypoint.AdditionalHubWarps
import java.awt.Color


object Diana : CategoryKt("Diana") {
    enum class ShareList {
        INQ, MANTICORE, KING, SPHINX
    }

    enum class ReceiveList {
        INQ, MANTICORE, KING, SPHINX, OTHER
    }

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
        this.name = Literal("Diana Burrow Guess")
        this.description = Literal("Guess the burrow location. Needs Driping Lava Partciles and set /particlequality to Extreme for more accuracy")
    }

    var dianaMultiBurrowGuess by boolean(false) {
        this.name = Literal("Diana Multi Burrow Guess")
        this.description = Literal("Multi guess the burrow location. Needs Driping Lava Partciles and set /particlequality to Extreme for more accuracy")
    }

    var dianaBurrowDetect by boolean(true) {
        this.name = Literal("Diana Burrow Detection")
        this.description = Literal("Detects Diana burrows | to reset waypoints /sboclearburrows")
    }

    init {
        separator {
            this.title = "Diana Warp"
        }
    }

    var allowedWarps by select(AdditionalHubWarps.WIZARD, AdditionalHubWarps.DA, AdditionalHubWarps.CASTLE) {
        this.name = Literal("Add Warps")
        this.description = Literal("Select the warps you want to be able to warp to with the guess and inquisitor warp keys.")
    }

    var dontWarpIfBurrowClose by boolean(true) {
        this.name = Literal("Don't Warp If a Burrow is nearby")
        this.description = Literal("If enabled, the warp key will not warp you if you are within 60 blocks of a burrow")
    }

    var warpDiff by int(10) {
        this.range = 0..60
        this.slider = true
        this.name = Literal("Warp Block Difference")
        this.description = Literal("The additional block difference to consider when warping to a waypoint. (0 to disable)")
    }

    var warpDelay by int(0) {
        this.range = 0..1000
        this.slider = true
        this.name = Literal("Warp Delay (<X>ms)")
        this.description = Literal("The delay bevor you can warp after guessing with spade. (0 to disable)")
    }

    var focusedWarp by boolean(false) {
        this.name = Literal("Focused Warp")
        this.description = Literal("If enabled, you warp to the guess you look at instead of the closest one")
    }

    init {
        separator {
            this.title = "Diana Tracker"
        }
    }

    var mobTracker by ObservableEntry(
        enum(Tracker.OFF) {
            this.name = Literal("Mob Tracker")
            this.description = Literal(
                "Shows your Diana mob kills, /sboguis to move the overlay\n" +
                "§bNOTE!: You can interact with the tracker in the inventory!!!§r\n" +
                "By clicking on a mob line you can hide/unhide it\n" +
                "Hovering over some lines may display additional information"
            )
        }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                DianaMobs.updateLines()
            }
        }
    }

    var lootTracker by ObservableEntry(
        enum(Tracker.OFF) {
            this.name = Literal("Loot Tracker")
            this.description = Literal(
                "Shows your Diana loot, /sboguis to move the overlay\n" +
                    "§bNOTE!: You can interact with the tracker in the inventory!!!§r\n" +
                    "By clicking on a loot line you can hide/unhide it\n" +
                    "Hovering over some lines may display additional information"
            )
        }
    ) { old, new ->
        if (old != new) {
            if (new != Tracker.OFF) {
                DianaLoot.updateLines()
            }
        }
    }

    var hideUnobtainedItems by ObservableEntry(
        boolean(true) {
            this.name = Literal("Hide Unobtained Items")
            this.description = Literal("Hides any loot or mob lines that have not been tracked yet (value is 0) to reduce clutter in the overlays.")
        }
    ) { old, new ->
        if (old != new) {
            if (lootTracker != Tracker.OFF) {
                DianaLoot.updateLines()
            }
            if (mobTracker != Tracker.OFF) {
                DianaMobs.updateLines()
            }
        }
    }

    var combineLootLines by ObservableEntry(
        boolean(false) {
            this.name = Literal("Combine LS Loot")
            this.description = Literal("Combines the base item and the Loot Share (LS) variant into a single line. The individual LS count is shown on hover.")
        }
    ) { old, new ->
        if (old != new) {
            if (lootTracker != Tracker.OFF) {
                DianaLoot.updateLines()
            }
        }
    }

    var statsTracker by boolean(false) {
        this.name = Literal("Diana Stats Tracker")
        this.description = Literal("Shows stats like Mobs since Inquisitor, Inquisitors since Chimera, /sboguis to move the overlay")
    }

    var magicFindTracker by boolean(false) {
        this.name = Literal("Magic Find Tracker")
        this.description = Literal("Shows your highest magic find for sticks and chimeras (only after you dropped it once), /sboguis to move the overlay")
    }

    var fourEyedFish by boolean(false) {
        this.name = Literal("Four-Eyed Fish")
        this.description = Literal("Set if you have a Four-Eyed Fish on your griffin pet")
    }

    var sendSinceMessage by boolean(true) {
        this.name = Literal("Stats Message")
        this.description = Literal("Sends the chat Message with stat: [SBO] Took 120 Mobs to get a Inquis!")
    }

    var bazaarSettingDiana by enum(SettingDiana.SELLOFFER) {
        this.name = Literal("Bazaar Setting")
        this.description = Literal("Bazaar setting to set the price for loot")
    }

    init {
        separator {
            this.title = "Diana Announcer"
        }
    }

    var lootAnnouncerChat by boolean(true) {
        this.name = Literal("Rare Drop Announcer")
        this.description = Literal("Announces relic/shelmet/plushie/remedies in chat")
    }

    var lootAnnouncerScreen by boolean(false) {
        this.name = Literal("Loot Screen Announcer")
        this.description = Literal("Announces chimera/stick/relic on screen")
    }

    var lootAnnouncerPrice by ObservableEntry(boolean(true) {
            this.name = Literal("Show Price Title")
            this.description = Literal("Shows chimera/stick/relic price as a subtitle on screen")
        }
    ) { old, new ->
        if (old != new) {
            if (new) {
                lootAnnouncerScreen = true
            }
        }
    }

    var lootAnnouncerParty by boolean(false) {
        this.name = Literal("Loot Party Announcer")
        this.description = Literal("Announces chimera/stick/relic and Shelmet/Plushie/Remedies (only when dropped from Inquisitor) in party chat")
    }

    var chimMessageBool by boolean(false) {
        this.name = Literal("Chim Message")
        this.description = Literal("Enables custom chim message")
    }

    var customChimMessage by strings("&6[SBO] &6&lRARE DROP! &d&lChimera! &b{mf} &b#{amount}") {
        this.name = Literal("Custom Chim Message Text")
        this.description = Literal("use: {mf} for MagicFind, {amount} for drop Amount this event and {percentage} for chimera/inquis ratio.")
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
        this.name = Literal("Guess Line")
        this.description = Literal("Draws line for guess, Disable View Bobbing in controls if its buggy")
    }

    var inqLine by boolean(true) {
        this.name = Literal("Rare Mob Line")
        this.description = Literal("Draws line to rare mob waypoints, Disable View Bobbing in controls if its buggy")
    }

    var burrowLine by boolean(true) {
        this.name = Literal("Burrow Line")
        this.description = Literal("Draws line for burrow, Disable View Bobbing in controls if its buggy")
    }

    var dianaLineWidth by int(5) {
        this.range = 1..20
        this.slider = true
        this.name = Literal("Diana Line Width")
        this.description = Literal("The width of the lines drawn for Diana waypoints")
    }

    var removeGuessDistance by int(0) {
        this.range = 0..20
        this.slider = true
        this.name = Literal("Remove Guess When Close")
        this.description = Literal("Removes the guess waypoint when you are within this distance of it (0 to disable)")
    }

    var removeRareMobwaypoint by boolean(true) {
        this.name = Literal("Remove Rare Mob Waypoint when near")
        this.description = Literal("Removes the rare mob waypoint when you are within 3 blocks of it")
    }

    var removeBeam by int(8) {
        this.range = 0..20
        this.slider = true
        this.name = Translated("Remove Rare Mob Beam Distance")
        this.description = Translated("Removes the rare mob waypoint beam when you are within this distance of it (0 to disable)")
    }

    init {
        separator {
            this.title = "Rare Mobs"
        }
    }

    var shareRareMob by boolean(true) {
        this.name = Literal("Share Rare-Mob")
        this.description = Literal("Sends the coordinates of rare mobs(King, Manti, Sphinx, Inq)to your party")
    }

    var ShareMobs by select(ShareList.INQ, ShareList.MANTICORE, ShareList.KING, ShareList.SPHINX) {
        this.name = Literal("Select which Mobs to Share")
        this.description = Literal("Select which mobs to share")
    }

    var receiveRareMob by boolean(true) {
        this.name = Literal("Receive Rare-Mob")
        this.description = Literal("Create a waypoint when someone in your party shares a rare mob(King, Manti, Sphinx, Inq)")
    }

    var ReceiveMobs by select(ReceiveList.INQ, ReceiveList.MANTICORE, ReceiveList.KING, ReceiveList.SPHINX, ReceiveList.OTHER) {
        this.name = Literal("Which Mobs to Receive")
        this.description = Literal(
        "Select which mobs to receive\n" +
            "§bOTHER = Rare mobs from players that dont ping with sbo (mainly skyhanni)"
        )
    }

    var HighlightRareMobs by boolean(true) {
        this.name = Literal("Highlight Rare Mobs")
        this.description = Literal("Highlights rare mobs(King, Manti, Sphinx, Inq) with a glowing effect")
    }

    var HighlightColor by color(
        Color(0.0f, 0.964f, 1.0f).rgb) {
        this.name = Literal("Highlight Color")
        this.description = Literal("Color for the rare mob highlight effect")
        this.allowAlpha = true
    }

    var allWaypointsAreInqs by boolean(false) {
        this.name = Literal("All Waypoints are Rare Mobs")
        this.description = Literal("All coordinates from chat are considered rare mobs(King, Manti, Sphinx, Inq) only works in hub during diana")
    }

    var announceKilltext by strings("") {
        this.name = Literal("Send Text On Rare Mob Spawn")
        this.description = Literal("Sends a text on Rare Mob spawn 5 seconds after spawn, use {since} for mobs since mob, {chance} for mob chance")
    }

    var announceCocoon by boolean(false) {
        this.name = Literal("Send Text On Cocoon")
        this.description = Literal("Sends a text on cocoon")
    }

    var cocoonTitle by boolean(false) {
        this.name = Literal("Show Title On Cocoon")
        this.description = Literal("Shows a title on cocoon")
    }

    var hpAlert by double(0.0) {
        this.name = Literal("HP Alert")
        this.description = Literal("Sends a title alert when a Rare Mob is below the set HP value in Million (0 to disable)")
    }

    var noShurikenOverlay by boolean(false) {
        this.name = Translated("No Shuriken Overlay")
        this.description = Translated("Shows an overlay when the RareMob has no shuriken applied /sboguis to move it")
    }

    init {
        button {
            title = "Send Test Message"
            text = "Send Test"
            description = "Sends a test message for the Rare mob spawn message"
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
        this.name = Literal("Mythos Mob HP")
        this.description = Literal("Displays HP of mythological mobs near you. /sboguis to move it")
    }
}