package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object QOL : CategoryKt("QOL") {

    var pickuplogOverlay by boolean(false) {
        name = Translated("Pickup Log Overlay")
        description = Translated("Displays a pickup log in an overlay like sba. /sboguis to move the overlay")
    }

    var phoenixAnnouncer by boolean(true) {
        name = Translated("Phoenix Announcer")
        description = Translated("Announces on screen when you drop a phoenix pet")
    }

    var dianaMessageHider by boolean(false) {
        name = Translated("Diana Message Hider")
        description = Translated("Hides all spammy Diana messages")
    }

    var hideAutoPetMSG by boolean(false) {
        name = Translated("Hide AutoPet Messages")
        description = Translated("Hides all autopet messages")
    }

    var hideImplosionMSG by boolean(false) {
        name = Translated("Hide Implosion Messages")
        description = Translated("Hides all implosion messages")
    }

    var hideSacksMSG by boolean(false) {
        name = Translated("Hide Sack Messages")
        description = Translated("Hides all sack messages")
    }
}