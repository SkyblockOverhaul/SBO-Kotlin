package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.overlay.OverlayEditScreen

object General : CategoryKt("General") {
    enum class HideOwnWaypoints {
        NORMAL, INQ
    }

    init {
        separator {
            title = "Overlays"
        }
    }

    var bobberOverlay by boolean(false) {
        name = Translated("Bobber Overlay")
        description = Translated("Tracks the number of bobbers near you /sboguis to move the overlay")
    }

    var legionOverlay by boolean(false) {
        name = Translated("Legion Overlay")
        description = Translated("Tracks the players near you for legion buff /sboguis to move the overlay")
    }

    init {
        button {
            title = "Move GUI's"
            text = "Move GUI's"
            description = "Opens Gui Move Menu you can use /sboguis too"
            onClick {
                mc.send {
                    mc.setScreen(OverlayEditScreen())
                }
            }
        }

        separator {
            title = "Waypoints"
        }
    }

    var hideOwnWaypoints by select<HideOwnWaypoints> {
        name = Translated("Hide Own Waypoints")
        description = Translated("Hides waypoints you created")
    }

    var patcherWaypoints by boolean(true) {
        name = Translated("Waypoints From Chat")
        description = Translated("Creates waypoints from chat messages (format: x: 20, y: 60, z: 80)")
    }
}