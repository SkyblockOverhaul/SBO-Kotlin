package net.sbo.mod.overlays

import net.sbo.mod.utils.events.Register
import net.sbo.mod.settings.categories.General
import net.sbo.mod.utils.game.World
import net.minecraft.entity.projectile.FishingBobberEntity
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayTextLine


object Bobber {
    var bobberCount: Int = 0
    val overlay: Overlay = Overlay("bobberOverlay", 10.0f, 10.0f, 1.0f)
    val overlayText: OverlayTextLine = OverlayTextLine("")

    fun init() {
        overlay.init()
        overlay.setCondition { General.bobberOverlay }
        overlay.addLine(overlayText)
        Register.onTick(20) {
            if (!General.bobberOverlay || !World.isInSkyblock()) return@onTick
            val player = mc.player ?: return@onTick
            val world = mc.world ?: return@onTick
            val nearbyBobbers = world.entities.filter { entity ->
                entity is FishingBobberEntity && entity.distanceTo(player) < 31
            }
            bobberCount = nearbyBobbers.size
            overlayText.text = "§e§lBobber: §b§l$bobberCount"
        }
    }
}