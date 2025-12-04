package net.sbo.mod.diana

import net.minecraft.entity.player.PlayerEntity
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.diana.DianaMobDetect.RareDianaMob
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.accessors.isSboGlowing
import net.sbo.mod.utils.accessors.setSboGlowColor
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.entity.EntityLoadEvent
import net.sbo.mod.utils.events.impl.entity.EntityUnloadEvent
import java.awt.Color

/**
 * Highlights rare Diana mobs by making them glow.
 *
 * This object listens for entity load and unload events to track rare mobs.
 * On every 4th tick, it checks if the rare mobs should be glowing based on visibility
 * and settings, and updates their glow state accordingly.
 */
object RareMobHighlight {
    private val rareMobs = mutableSetOf<PlayerEntity>()

    fun init() {
        Register.onTick(4) {
            val world = mc.world ?: return@onTick
            checkMobGlow(world)
        }
    }

    @SboEvent
    fun onEntityLoad(event: EntityLoadEvent) {
        if (event.entity is PlayerEntity) {
            if (!Diana.HighlightRareMobs) return
            if (RareDianaMob.entries.any { event.entity.name.string.contains(it.display, ignoreCase = true) } && event.entity.uuid.version() != 4) {
                rareMobs.add(event.entity)
            }
        }
    }

    @SboEvent
    fun onEntityUnload(event: EntityUnloadEvent) {
        if (event.entity is PlayerEntity) {
            if (!Diana.HighlightRareMobs) return
            if (rareMobs.contains(event.entity)) {
                event.entity.isSboGlowing = false
                rareMobs.remove(event.entity)
            }
        }
    }

    private fun checkMobGlow(world: net.minecraft.world.World) {
        val iterator = rareMobs.iterator()
        while (iterator.hasNext()) {
            val mob = iterator.next()

            if (!mob.isAlive || mob.world != world) {
                mob.isSboGlowing = false
                iterator.remove()
                continue
            }

            if (Diana.HighlightRareMobs && mc.player?.canSee(mob) == true && !mob.isInvisible) {
                mob.isSboGlowing = true
                mob.setSboGlowColor(Color(Diana.HighlightColor))
            } else {
                mob.isSboGlowing = false
            }
        }
    }
}