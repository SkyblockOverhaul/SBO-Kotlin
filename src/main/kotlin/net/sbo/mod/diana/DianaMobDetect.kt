package net.sbo.mod.diana

import com.mojang.authlib.properties.Property
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
import kotlin.collections.first
import kotlin.math.roundToInt

object DianaMobDetect {
    private val tracked = mutableMapOf<Int, ArmorStandEntity>()
    private val defeated = mutableSetOf<Int>()
    private val warnedMobs = mutableSetOf<Int>()
    private val mobHpOverlay: Overlay = Overlay("mythosMobHp", 10f, 10f, 1f, listOf("Chat screen"), OverlayExamples.mythosMobHpExample).setCondition { Diana.mythosMobHp }

    private const val DEATH_WINDOW_SECONDS = 5
    private const val COCOON_COOLDOWN_MS = 10_000L
    private const val CHAT_DELAY_MS = 200L
    private const val ANNOUNCE_DELAY_MS = 5000L

    fun init() {
        mobHpOverlay.init()
        Register.onTick(1) {
            val world = mc.world ?: return@onTick
            val overlayLines = mutableListOf<OverlayTextLine>()

            val iterator = tracked.iterator()
            while (iterator.hasNext()) {
                val (id, armorStand) = iterator.next()

                if (!armorStand.isAlive || armorStand.world != world) {
                    val mob = getActualMob(armorStand)
                    mob?.isSboGlowing = false
                    iterator.remove()
                    defeated.remove(id)
                    continue
                }

                checkCocoon(armorStand)

                val line = checkDianaMob(armorStand, id)
                if (line != null) {
                    overlayLines.add(line)
                }
            }
            mobHpOverlay.setLines(overlayLines)
        }
    }

    @SboEvent
    fun onEntityLoad(event: EntityLoadEvent) {
        if (event.entity is ArmorStandEntity) {
            tracked[event.entity.id] = event.entity
        }
    }

    @SboEvent
    fun onEntityUnload(event: EntityUnloadEvent) {
        if (event.entity is ArmorStandEntity) {
            val mob = getActualMob(event.entity)
            mob?.isSboGlowing = false
            tracked.remove(event.entity.id)
            defeated.remove(event.entity.id)
        }
    }

    private fun extractHealth(name: String): Double? {
        val regex = """([0-9]+(?:\.[0-9]+)?[MK]?)§f/""".toRegex()
        val match = regex.find(name) ?: return null
        val value = match.groupValues[1]
        return when {
            value.endsWith("M") -> value.dropLast(1).toDoubleOrNull()?.times(1_000_000)
            value.endsWith("K") -> value.dropLast(1).toDoubleOrNull()?.times(1_000)
            else -> value.toDoubleOrNull()
        }
    }

    private fun checkDianaMob(entity: ArmorStandEntity, id: Int) : OverlayTextLine? {
        val name = entity.customName?.formattedString() ?: entity.name.formattedString()
        if (name.isEmpty() || name == "Armor Stand") return null
        if (name.contains("༕", ignoreCase = true)) {
            val health = extractHealth(name)
            checkForHealthAlert(name, id, health)

            if (Diana.HighightRareMobs) {
                val color = Color(Diana.HighightColor)
                glowMob(entity, color)
            }

            if (health != null && health <= 0 && id !in defeated) {
                defeated.add(id)
                warnedMobs.remove(id)
                unglowMob(entity)
                SBOEvent.emit(DianaMobDeathEvent(name, entity))
            }
            return OverlayTextLine(name)
        }
        return null
    }

    private fun checkCocoon(entity: ArmorStandEntity): Boolean {
        if(World.getWorld() != "Hub") return false
        val cocoonTexture = "eyJ0aW1lc3RhbXAiOjE1ODMxMjMyODkwNTMsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlYjBlZDhmYzIyNzJiM2QzZDgyMDY3NmQ1MmEzOGU3YjJlOGRhOGM2ODdhMjMzZTBkYWJhYTE2YzBlOTZkZiJ9fX0="
        val anyRecentDeath = listOf(lastInqDeath, lastKingDeath, lastSphinxDeath, lastMantiDeath)
            .any { getSecondsPassed(it) < DEATH_WINDOW_SECONDS }

        if (!anyRecentDeath) return false
        val head: ItemStack = entity.getEquippedStack(EquipmentSlot.HEAD)
        if (!head.isEmpty && head.item.toString() == "minecraft:player_head"){
            val profile: ProfileComponent? = head.get(DataComponentTypes.PROFILE)
            val textures : Property? = profile?.properties?.get("textures")?.first()
            val texture = textures?.value
            if (texture.equals(cocoonTexture) && lastCocoon + COCOON_COOLDOWN_MS < System.currentTimeMillis()){
                lastCocoon = System.currentTimeMillis()
                if (Diana.announceCocoon){
                    sleep(CHAT_DELAY_MS) {
                        Chat.command("pc Cocoon!")
                    }
                }
                if (Diana.cocoonTitle){
                    showTitle("§r§6§l<§b§l§kO§6§l> §b§lCOCOON! §6§l<§b§l§kO§6§l>", null, 10, 40, 10)
                    playCustomSound(Customization.inqSound[0], Customization.inqVolume)
                }
                return true
            }
        }
        return false
    }

    private fun checkForHealthAlert(name: String, id: Int, health: Double?) {
        if (health == null) return
        if (id in defeated || id in warnedMobs) return
        val alertEnabled = when {
            name.contains("Minos Inquisitor", ignoreCase = true) -> Diana.hpAlert > 0.0
            name.contains("King Minos", ignoreCase = true) -> Diana.hpAlert > 0.0
            name.contains("Sphinx", ignoreCase = true) -> Diana.hpAlert > 0.0
            name.contains("Manticore", ignoreCase = true) -> Diana.hpAlert > 0.0
            else -> false
        }
        if (!alertEnabled) return
        val hpThreshold = if (Diana.hpAlert  > 0.0) Diana.hpAlert * 1_000_000 else 0.0
        if (hpThreshold > 0.0 && health <= hpThreshold) {
            showTitle("§cHP LOW!", null, 10, 40, 10)
            warnedMobs.add(id)
        }
    }

    fun onRareSpawn(mob: String) {
        if (Diana.shareRareMob) {
            val mobType: Diana.ShareList = when (mob) {
                "Minos Inquisitor" -> Diana.ShareList.INQ
                "King Minos" -> Diana.ShareList.KING
                "Sphinx" -> Diana.ShareList.SPHINX
                "Manticore" -> Diana.ShareList.MANTICORE
                else -> return
            }
            if (mobType !in Diana.ShareMobs) return
            val playerPos = Player.getLastPosition()
            Chat.command("pc x: ${playerPos.x.roundToInt()}, y: ${playerPos.y.roundToInt() - 1}, z: ${playerPos.z.roundToInt()} | $mob")
        }

        Diana.announceKilltext.firstOrNull()?.let { killText ->
            if (killText.isNotBlank()) {
                sleep(ANNOUNCE_DELAY_MS) {
                    Chat.command("pc " + Diana.announceKilltext[0])
                }
            }
        }
    }

    private fun getActualMob(amrorstand: ArmorStandEntity): Entity? {
        val world = mc.world ?: return null
        val actualMob: Entity? = world.getEntityById(amrorstand.id - 1)
        return actualMob
    }

    private fun glowMob(entity: ArmorStandEntity, color: Color) {
        getActualMob(entity).let { mob ->
            mob?.setSboGlowColor(color)
            mob?.isSboGlowing = true
        }
    }

    private fun unglowMob(entity: ArmorStandEntity) {
        getActualMob(entity).let { mob ->
            mob?.isSboGlowing = false
        }
    }
}