package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt

object Debug : CategoryKt("Debug") {
    var itsAlwaysDiana by boolean(false) {
        name = Translated("Always Diana Mayor")
        description = Translated("Its always Diana, no need to check for mayor, perks, spade or world")
    }

    var alwaysInSkyblock by boolean(false) {
        name = Translated("Always on Skyblock")
        description = Translated("Always assume you are on hypixel skyblock")
    }

    var debugMessages by boolean(false) {
        name = Translated("Debug Messages")
        description = Translated("Enable debug messages for development purposes")
    }
}