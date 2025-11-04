package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.overlay.OverlayEditScreen

object General : CategoryKt("General") {
    enum class HideOwnWaypoints {
        NORMAL, INQ, MANTICORE, KING, SPHINX
    }

    init {
        separator {
            this.title = "Overlays"
        }
    }

    var bobberOverlay by boolean(false) {
        this.name = Translated("Bobber Overlay")
        this.description = Translated("Tracks the number of bobbers near you /sboguis to move the overlay")
    }

    var legionOverlay by boolean(false) {
        this.name = Translated("Legion Overlay")
        this.description = Translated("Tracks the players near you for legion buff /sboguis to move the overlay")
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
            this.title = "Waypoints"
        }
    }

    var hideOwnWaypoints by select<HideOwnWaypoints> {
        this.name = Translated("Hide Own Waypoints")
        this.description = Translated("Hides waypoints you created")
    }

    var patcherWaypoints by boolean(true) {
        this.name = Translated("Waypoints From Chat")
        this.description = Translated("Creates waypoints from chat messages (format: x: 20, y: 60, z: 80)")
    }
}