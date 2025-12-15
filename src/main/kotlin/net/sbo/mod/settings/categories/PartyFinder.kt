package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object PartyFinder : CategoryKt("PartyFinder") {
    var autoInvite by boolean(true) {
        this.name = Literal("Auto Invite")
        this.description = Literal("Auto invites players that send you a join request and meet the party requirements")
    }

    var autoRequeue by boolean(false) {
        this.name = Literal("Auto Requeue")
        this.description = Literal("Automatically requeues the party after a member leaves")
    }

    var scaleText by float(0f) {
        this.name = Literal("Text Scale")
        this.description = Literal("Change the size of the text")
        this.range = -2f..2f
        this.slider = true
    }

    var scaleIcon by float(0f) {
        this.name = Literal("Icon Scale")
        this.description = Literal("Change the size of the icons")
        this.range = -20f..20f
        this.slider = true
    }
}