package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.overlay.OverlayEditScreen

object General : CategoryKt("General") {
    enum class TestColor {
        RED, GREEN, BLUE, YELLOW, PURPLE
    }

    enum class HideOwnWaypoints {
        NORMAL, INQ
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

//    var test1 by boolean(false) {
//        this.name = Translated("test1")
//        this.description = Translated("test1 description")
//    }
//
//    var test2 by float(1f) {
//        this.range = 0f..5f
//        this.slider = true
//        this.name = Translated("test2")
//        this.description = Translated("test2 description")
//    }
//
//
//    var backgroundColor by color(0xC0000000u.toInt()) {
//        this.name = Translated("backgroundColor")
//        this.description = Translated("backgroundColor description")
//        this.allowAlpha = true
//    }
//
//    var testIput by strings("test") {
//        this.name = Translated("testInput")
//        this.description = Translated("testInput description")
//    }
//
//    var singleColorSelector  by enum(TestColor.PURPLE) {
//        this.name = Translated("testSelector")
//        this.description = Translated("testSelector description")
//    }
//
//    var multiColorSelectore by select(TestColor.PURPLE) {
//        this.name = Translated("testMultiSelector")
//        this.description = Translated("testMultiSelector description")
//    }
}