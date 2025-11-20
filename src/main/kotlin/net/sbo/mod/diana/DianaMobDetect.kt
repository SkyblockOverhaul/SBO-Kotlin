package net.sbo.mod.diana

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.item.ItemStack
import net.sbo.mod.utils.events.Register
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper.getSecondsPassed
import net.sbo.mod.utils.Helper.lastCocoon
import net.sbo.mod.utils.Helper.lastInqDeath
import net.sbo.mod.utils.Helper.lastKingDeath
import net.sbo.mod.utils.Helper.lastMantiDeath
import net.sbo.mod.utils.Helper.lastSphinxDeath
import net.sbo.mod.utils.Helper.showTitle
import net.sbo.mod.utils.Helper.sleep
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.SoundHandler.playCustomSound
import net.sbo.mod.utils.accessors.isSboGlowing
import net.sbo.mod.utils.accessors.setSboGlowColor
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.chat.ChatUtils.formattedString
import net.sbo.mod.utils.events.SBOEvent
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.entity.DianaMobDeathEvent
import net.sbo.mod.utils.events.impl.entity.EntityLoadEvent
import net.sbo.mod.utils.events.impl.entity.EntityUnloadEvent
import net.sbo.mod.utils.game.World
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayExamples
import net.sbo.mod.utils.overlay.OverlayTextLine
import java.awt.Color
import kotlin.math.roundToInt

object DianaMobDetect {
    private const val DEATH_WINDOW_SECONDS = 5
    private const val COCOON_COOLDOWN_MS = 10_000L
    private const val CHAT_DELAY_MS = 200L
    private const val ANNOUNCE_DELAY_MS = 5_000L

    private val COCOON_TEXTURE = "eyJ0aW1lc3RhbXAiOjE1ODMxMjMyODkwNTMsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlYjBlZDhmYzIyNzJiM2QzZDgyMDY3NmQ1MmEzOGU3YjJlOGRhOGM2ODdhMjMzZTBkYWJhYTE2YzBlOTZkZiJ9fX0="
    private val healthRegex = """([0-9]+(?:\.[0-9]+)?[MK]?)§f/""".toRegex()

    private val tracked = mutableMapOf<Int, ArmorStandEntity>()
    private val defeated = mutableSetOf<Int>()
    private val warned = mutableSetOf<Int>()

    private val mobHpOverlay: Overlay = Overlay("mythosMobHp", 10f, 10f, 1f, listOf("Chat screen"), OverlayExamples.mythosMobHpExample).setCondition { Diana.mythosMobHp }

    private enum class RareDianaMob(val display: String) {
        INQ("Minos Inquisitor"),
        KING("King Minos"),
        SPHINX("Sphinx"),
        MANTI("Manticore");

        companion object {
            fun fromName(name: String): RareDianaMob? = entries.firstOrNull { name.contains(it.display, ignoreCase = true) }
        }
    }

    private fun ArmorStandEntity.actualMob(): Entity? = mc.world?.getEntityById(this.id - 1)

    private fun setGlow(entity: ArmorStandEntity, color: Color?) {
        entity.actualMob()?.let { mob ->
            val player = mc.player ?: return
            if (!player.canSee(mob)) {
                mob.isSboGlowing = false
                return
            }
            mob.isSboGlowing = true
            if (color != null) mob.setSboGlowColor(color)
        }
    }

    private fun clearGlow(entity: ArmorStandEntity) {
        entity.actualMob()?.isSboGlowing = false
    }

    private fun parseHealthFromName(name: String): Double? =
        healthRegex.find(name)?.groupValues?.get(1)?.let { raw ->
            when {
                raw.endsWith("M") -> raw.dropLast(1).toDoubleOrNull()?.times(1_000_000)
                raw.endsWith("K") -> raw.dropLast(1).toDoubleOrNull()?.times(1_000)
                else -> raw.toDoubleOrNull()
            }
        }

    private fun shouldAlertForMob(name: String) = RareDianaMob.fromName(name) != null && Diana.hpAlert > 0.0

    fun init() {
        mobHpOverlay.init()
        Register.onTick(1) {
            val world = mc.world ?: return@onTick
            val overlayLines = mutableListOf<OverlayTextLine>()

            val iterator = tracked.iterator()
            while (iterator.hasNext()) {
                val (id, armorStand) = iterator.next()

                if (!armorStand.isAlive || armorStand.world != world) {
                    clearGlow(armorStand)
                    iterator.remove()
                    defeated.remove(id)
                    continue
                }

                checkCocoon(armorStand)
                checkDianaMob(armorStand, id)?.let { overlayLines.add(it) }
            }
            mobHpOverlay.setLines(overlayLines)
        }
    }

    @SboEvent
    fun onEntityLoad(event: EntityLoadEvent) {
        if (event.entity is ArmorStandEntity) tracked[event.entity.id] = event.entity
    }

    @SboEvent
    fun onEntityUnload(event: EntityUnloadEvent) {
        if (event.entity is ArmorStandEntity) {
            clearGlow(event.entity)
            tracked.remove(event.entity.id)
            defeated.remove(event.entity.id)
        }
    }

    private fun checkDianaMob(entity: ArmorStandEntity, id: Int) : OverlayTextLine? {
        val name = entity.customName?.formattedString() ?: entity.name.formattedString()
        if (name.isEmpty() || name == "Armor Stand") return null
        if (!name.contains("༕", ignoreCase = true)) return null

        val health = parseHealthFromName(name)
        maybeTriggerHealthAlert(name, id, health)

        if (Diana.HighightRareMobs) {
            val color = Color(Diana.HighightColor)
            setGlow(entity, color)
        }

        if (health != null && health <= 0.0 && id !in defeated) {
            defeated.add(id)
            warned.remove(id)
            clearGlow(entity)
            SBOEvent.emit(DianaMobDeathEvent(name, entity))
        }
        return OverlayTextLine(name)
    }

    private fun checkCocoon(entity: ArmorStandEntity) {
        if (World.getWorld() != "Hub") return
        val recentDeath = listOf(lastInqDeath, lastKingDeath, lastSphinxDeath, lastMantiDeath)
            .any { getSecondsPassed(it) < DEATH_WINDOW_SECONDS }

        if (!recentDeath) return
        val head: ItemStack = entity.getEquippedStack(EquipmentSlot.HEAD)

        if (head.isEmpty) return
        if (head.item.toString() != "minecraft:player_head") return
        val profile: ProfileComponent? = head.get(DataComponentTypes.PROFILE)
        val texture: String? = profile?.properties?.get("textures")?.firstOrNull()?.value

        if (texture == null) return
        val now = System.currentTimeMillis()
        if (texture == COCOON_TEXTURE && lastCocoon + COCOON_COOLDOWN_MS < now) {
            lastCocoon = now
            if (Diana.announceCocoon) {
                sleep(CHAT_DELAY_MS) { Chat.command("pc Cocoon!") }
            }
            if (Diana.cocoonTitle) {
                showTitle("§r§6§l<§b§l§kO§6§l> §b§lCOCOON! §6§l<§b§l§kO§6§l>", null, 10, 40, 10)
                playCustomSound(Customization.inqSound[0], Customization.inqVolume)
            }
        }
    }

    private fun maybeTriggerHealthAlert(name: String, id: Int, health: Double?) {
        if (health == null) return
        if (id in defeated || id in warned) return
        if (!shouldAlertForMob(name)) return
        val hpThreshold = if (Diana.hpAlert > 0.0) Diana.hpAlert * 1_000_000 else 0.0
        if (hpThreshold > 0.0 && health <= hpThreshold) {
            showTitle("§cHP LOW!", null, 10, 40, 10)
            warned.add(id)
        }
    }

    fun onRareSpawn(mob: String) {
        if (Diana.shareRareMob) {
            val mobType = when (mob) {
                RareDianaMob.INQ.display -> Diana.ShareList.INQ
                RareDianaMob.KING.display -> Diana.ShareList.KING
                RareDianaMob.SPHINX.display -> Diana.ShareList.SPHINX
                RareDianaMob.MANTI.display -> Diana.ShareList.MANTICORE
                else -> null
            } ?: return

            if (mobType !in Diana.ShareMobs) return
            val playerPos = Player.getLastPosition()
            Chat.command("pc x: ${playerPos.x.roundToInt()}, y: ${playerPos.y.roundToInt() - 1}, z: ${playerPos.z.roundToInt()} | $mob")
        }

        Diana.announceKilltext.firstOrNull()?.let { killText ->
            if (killText.isNotBlank()) {
                sleep(ANNOUNCE_DELAY_MS) { Chat.command("pc " + Diana.announceKilltext[0]) }
            }
        }
    }
}