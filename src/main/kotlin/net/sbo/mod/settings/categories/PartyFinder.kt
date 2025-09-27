package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object PartyFinder : CategoryKt("PartyFinder") {
    var autoInvite by boolean(true) {
        name = Translated("Auto Invite")
        description = Translated("Auto invites players that send you a join request and meet the party requirements")
    }

    var autoRequeue by boolean(false) {
        name = Translated("Auto Requeue")
        description = Translated("Automatically requeues the party after a member leaves")
    }

    var scaleText by float(0f) {
        name = Translated("Text Scale")
        description = Translated("Change the size of the text")
        range = -2f..2f
        slider = true
    }

    var scaleIcon by float(0f) {
        name = Translated("Icon Scale")
        description = Translated("Change the size of the icons")
        range = -20f..20f
        slider = true
    }
}