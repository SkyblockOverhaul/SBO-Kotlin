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
            this.title = "Waypoint Customization"
        }
    }

    var guessColor by color(
        Color(0.0f, 0.964f, 1.0f).rgb) {
        this.name = Literal("Guess Color")
        this.description = Literal("Pick a color for your guess")
        this.allowAlpha = true
    }

    var StartColor by color(
        Color(0.333f, 1.0f, 0.333f).rgb) {
        this.name = Literal("Start Burrow Color")
        this.description = Literal("Pick a color for start burrows")
        this.allowAlpha = true
    }
    
    var focusedColor by color(
        Color(0.6f, 0.2f, 0.8f).rgb) {
        this.name = Literal("Focused Color")
        this.description = Literal("Pick a color for your focused guess")
        this.allowAlpha = true
    }

    var MobColor by color(
        Color(1.0f, 0.333f, 0.333f).rgb) {
        this.name = Literal("Mob Burrow Color")
        this.description = Literal("Pick a color for mob burrows")
        this.allowAlpha = true
    }

    var TreasureColor by color(
        Color(1f, 0.666f, 0.0f).rgb) {
        this.name = Literal("Treasure Burrow Color")
        this.description = Literal("Pick a color for treasure burrows")
        this.allowAlpha = true
    }

    init {
        separator {
            this.title = "Waypoint Text Customization"
        }
    }

    var waypointTextShadow by boolean(true) {
        this.name = Literal("Waypoint Text Shadow")
        this.description = Literal("Enables shadow for waypoint text")
    }

    var waypointTextScale by float(0.7f) {
        this.name = Literal("Waypoint Text Scale")
        this.description = Literal("Scale of the waypoint text")
        this.range = 0.3f..2.0f
        this.slider = true
    }

    init {
        separator {
            this.title = "Sounds"
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
        this.name = Literal("Rare Spawn Sound")
        this.description = Literal("Set the sound that plays when an rare mob spawns. (enter filename)")
    }
    var inqVolume by float(1.0f) {
        this.name = Literal("Rare Spawn Volume")
        this.description = Literal("Set the volume of the rare mob spawn sound")
        this.range = 0.0f..1.0f
        this.slider = true
    }

    var burrowSound by strings("") {
        this.name = Literal("Burrow Found Sound")
        this.description = Literal("Set the sound that plays when you find a burrow. (enter filename)")
    }
    var burrowVolume by float(1.0f) {
        this.name = Literal("Burrow Found Volume")
        this.description = Literal("Set the volume of the burrow found sound")
        this.range = 0.0f..1.0f
        this.slider = true
    }

    var chimSound by strings("") {
        this.name = Literal("Chimera Drop Sound")
        this.description = Literal("Set the sound that plays when you drop a chimera book. (enter filename)")
    }
    var chimVolume by float(1.0f) {
        this.name = Literal("Chimera Drop Volume")
        this.description = Literal("Set the volume of the chimera drop sound")
        this.range = 0.0f..1.0f
        this.slider = true
    }

    var relicSound by strings("") {
        this.name = Literal("Relic Drop Sound")
        this.description = Literal("Set the sound that plays when you drop a minos relic. (enter filename)")
    }
    var relicVolume by float(1.0f) {
        this.name = Literal("Relic Drop Volume")
        this.description = Literal("Set the volume of the relic drop sound")
        this.range = 0.0f..1.0f
        this.slider = true
    }

    var stickSound by strings("") {
        this.name = Literal("Daedalus Stick Drop Sound")
        this.description = Literal("Set the sound that plays when you drop a daedalus stick.")
    }
    var stickVolume by float(1.0f) {
        this.name = Literal("Daedalus Stick Drop Volume")
        this.description = Literal("Set the volume of the daedalus stick drop sound")
        this.range = 0.0f..1.0f
        this.slider = true
    }

    var sprSound by strings("") {
        this.name = Literal("Shelmet/Plushie/Remedies Drop Sound")
        this.description = Literal("Set the sound that plays when you drop a Shelmet/Plushie/Remedies. (enter filename)")
    }
    var sprVolume by float(1.0f) {
        this.name = Literal("Shelmet/Plushie/Remedies Drop Volume")
        this.description = Literal("Set the volume of the Shelmet/Plushie/Remedies drop sound")
        this.range = 0.0f..1.0f
        this.slider = true
    }
}