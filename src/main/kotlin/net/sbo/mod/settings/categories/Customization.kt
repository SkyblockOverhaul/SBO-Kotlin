package net.sbo.mod.settings.categories

import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import gg.essential.universal.UDesktop
import net.fabricmc.loader.api.FabricLoader
import java.awt.Color
import java.io.File

object Customization : CategoryKt("Customization") {
    val ALL_SOUNDS_FILENAMES: List<String> = try {
        val path = "${FabricLoader.getInstance().configDir}/sbo/sounds"
        val directory = File(path)
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles { file -> file.extension == "ogg" }
                ?.map { it.nameWithoutExtension }
                ?.sorted()
                ?: emptyList()
        } else {
            println("Directory not found or is not a directory: $path")
            emptyList()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }

    init {
        separator {
            title = "Waypoint Customization"
        }
    }

    var guessColor by color(
        Color(0.0f, 0.964f, 1.0f).rgb
    ) {
        name = Translated("Guess Color")
        description = Translated("Pick a color for your guess")
        allowAlpha = true
    }

    var StartColor by color(
        Color(0.333f, 1.0f, 0.333f).rgb
    ) {
        name = Translated("Start Burrow Color")
        description = Translated("Pick a color for start burrows")
        allowAlpha = true
    }

    var focusedColor by color(
        Color(0.6f, 0.2f, 0.8f).rgb) {
        this.name = Translated("Focused Color")
        this.description = Translated("Pick a color for your focused guess")
        this.allowAlpha = true
    }

    var MobColor by color(
        Color(1.0f, 0.333f, 0.333f).rgb
    ) {
        name = Translated("Mob Burrow Color")
        description = Translated("Pick a color for mob burrows")
        allowAlpha = true
    }

    var TreasureColor by color(
        Color(1f, 0.666f, 0.0f).rgb
    ) {
        name = Translated("Treasure Burrow Color")
        description = Translated("Pick a color for treasure burrows")
        allowAlpha = true
    }

    init {
        separator {
            title = "Waypoint Text Customization"
        }
    }

    var waypointTextShadow by boolean(true) {
        name = Translated("Waypoint Text Shadow")
        description = Translated("Enables shadow for waypoint text")
    }

    var waypointTextScale by float(0.7f) {
        name = Translated("Waypoint Text Scale")
        description = Translated("Scale of the waypoint text")
        range = 0.5f..2.0f
        slider = true
    }

    init {
        separator {
            title = "Sounds"
        }

        button {
            title = "Open Sound Folder"
            text = "open"
            description = "Custom sounds go in here (sound must be a .ogg). You need to restart minecraft after adding a sound"
            onClick {
                val path = "${FabricLoader.getInstance().configDir}/sbo/sounds"
                val directory = File(path)
                if (directory.exists()) {
                    try {
                        UDesktop.open(directory)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    println("Directory not found: $path")
                }
            }
        }
    }

    var inqSound by strings("exporb") {
        name = Translated("Inquisitor Spawn Sound")
        description = Translated("Set the sound that plays when an inquisitor spawns. (enter filename)")
    }
    var inqVolume by float(1.0f) {
        name = Translated("Inquisitor Spawn Volume")
        description = Translated("Set the volume of the inquisitor spawn sound")
        range = 0.0f..1.0f
        slider = true
    }

    var burrowSound by strings("") {
        name = Translated("Burrow Found Sound")
        description = Translated("Set the sound that plays when you find a burrow. (enter filename)")
    }
    var burrowVolume by float(1.0f) {
        name = Translated("Burrow Found Volume")
        description = Translated("Set the volume of the burrow found sound")
        range = 0.0f..1.0f
        slider = true
    }

    var chimSound by strings("") {
        name = Translated("Chimera Drop Sound")
        description = Translated("Set the sound that plays when you drop a chimera book. (enter filename)")
    }
    var chimVolume by float(1.0f) {
        name = Translated("Chimera Drop Volume")
        description = Translated("Set the volume of the chimera drop sound")
        range = 0.0f..1.0f
        slider = true
    }

    var relicSound by strings("") {
        name = Translated("Relic Drop Sound")
        description = Translated("Set the sound that plays when you drop a minos relic. (enter filename)")
    }
    var relicVolume by float(1.0f) {
        name = Translated("Relic Drop Volume")
        description = Translated("Set the volume of the relic drop sound")
        range = 0.0f..1.0f
        slider = true
    }

    var stickSound by strings("") {
        name = Translated("Daedalus Stick Drop Sound")
        description = Translated("Set the sound that plays when you drop a daedalus stick.")
    }
    var stickVolume by float(1.0f) {
        name = Translated("Daedalus Stick Drop Volume")
        description = Translated("Set the volume of the daedalus stick drop sound")
        range = 0.0f..1.0f
        slider = true
    }

    var sprSound by strings("") {
        name = Translated("Shelmet/Plushie/Remedies Drop Sound")
        description = Translated("Set the sound that plays when you drop a Shelmet/Plushie/Remedies. (enter filename)")
    }
    var sprVolume by float(1.0f) {
        name = Translated("Shelmet/Plushie/Remedies Drop Volume")
        description = Translated("Set the volume of the Shelmet/Plushie/Remedies drop sound")
        range = 0.0f..1.0f
        slider = true
    }
}