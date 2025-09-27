package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object PartyCommands : CategoryKt("Party Commands") {
    var warpCommand by boolean(false) {
        name = Translated("Warp Party")
        description = Translated("!w, !warp")
    }

    var allinviteCommand by boolean(false) {
        name = Translated("Allinvite")
        description = Translated("!allinv, !allinvite")
    }

    var transferCommand by boolean(false) {
        name = Translated("Party Transfer")
        description = Translated("!transfer [Player] (if no player is defined it transfers the party to the command writer)")
    }

    var moteCommand by boolean(false) {
        name = Translated("Promote/Demote")
        description = Translated("!promote/demote [Player] (if no player is defined it pro/demotes the command writer)")
    }

    var carrotCommand by boolean(false) {
        name = Translated("Ask Carrot")
        description = Translated("Enable !carrot Command")
    }

    var timeCommand by boolean(false) {
        name = Translated("Time Check")
        description = Translated("Sends your time in party chat (!time)")
    }

    var tpsCommand by boolean(false) {
        name = Translated("Check Tps")
        description = Translated("Sends the server tps in party chat (!tps)")
    }

    var dianaPartyCommands by boolean(true) {
        name = Translated("Diana Party Commands")
        description = Translated("Enable Diana party commands (!chim, !inq, !relic, !stick, !since, !burrow, !mob) (note: you need to have Diana tracker enabled)")
    }
}