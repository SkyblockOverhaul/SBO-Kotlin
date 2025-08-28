package net.sbo.mod.diana

import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.decoration.ArmorStandEntity
import net.sbo.mod.utils.events.Register
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.chat.ChatUtils.formattedString
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayTextLine
import kotlin.math.roundToInt

object DianaMobDetect {
    private val trackedArmorStands = mutableMapOf<Int, String>()
    private val defeatedMobs = mutableSetOf<Int>()
    private val mobDeathListeners = mutableListOf<(String, ArmorStandEntity) -> Unit>() // <-- NEW
    private val mobHpOverlay: Overlay = Overlay("mythosMobHp", 10f, 10f, 1f, listOf("Chat screen")).setCondition { Diana.mythosMobHp }

    fun init() {
        mobHpOverlay.init()
        Register.onTick(1) {
            val world = mc.world
            if (world != null) {
                updateTrackedArmorStands(world)
            }
        }
    }

    fun onMobDeath(listener: (String, ArmorStandEntity) -> Unit) {
        mobDeathListeners.add(listener)
    }

    private fun updateTrackedArmorStands(world: ClientWorld) {
        val currentArmorStands = getMatchingArmorStands(world)
        val overlayLines: MutableList<OverlayTextLine> = mutableListOf()

        val removed = trackedArmorStands.keys - currentArmorStands.keys
        defeatedMobs.removeAll(removed)
        trackedArmorStands.keys.retainAll(currentArmorStands.keys)

        for ((id, name) in currentArmorStands) {
            trackedArmorStands[id] = name
            overlayLines.add(OverlayTextLine(name))
        }
        mobHpOverlay.setLines(overlayLines)
    }

    private fun getMatchingArmorStands(world: ClientWorld): Map<Int, String> {
        val armorstands = mutableMapOf<Int, String>()
        for (entity in world.entities) {
            if (entity is ArmorStandEntity) {
                val name = entity.customName?.formattedString() ?: entity.name.formattedString()
                if (name.isEmpty() || name == "Armor Stand") {
                    continue
                }
                if (name.contains("§2✿", ignoreCase = true)) {
                    val currentHealth = extractHealth(name)
                    if (currentHealth != null && currentHealth <= 0 && entity.id !in defeatedMobs) {
                        defeatedMobs.add(entity.id)
                        mobDeathListeners.forEach { it(name, entity) }
                    }
                    armorstands[entity.id] = name
                }
            }
        }
        return armorstands
    }

    private fun extractHealth(name: String?): Double? {
        if (name == null) return null
        val regex = """([0-9]+(?:\.[0-9]+)?[MK]?)§f/""".toRegex()
        val matchResult = regex.find(name)
        val healthString = matchResult?.groups?.get(1)?.value ?: return null
        return parseHealth(healthString)
    }

    private fun parseHealth(health: String): Double? {
        return when {
            health.endsWith("M") -> health.dropLast(1).toDoubleOrNull()?.times(1_000_000)
            health.endsWith("K") -> health.dropLast(1).toDoubleOrNull()?.times(1_000)
            else -> health.toDoubleOrNull()
        }
    }

    fun onInqSpawn() {
        if (Diana.shareInq) {
            val playerPos = Player.getLastPosition()
            Chat.command("pc x: ${playerPos.x.roundToInt()}, y: ${playerPos.y.roundToInt() - 1}, z: ${playerPos.z.roundToInt()}")
        }

        Diana.announceKilltext.firstOrNull()?.let { killText ->
            if (killText.isNotBlank()) {
                Helper.sleep(5000) {
                    Chat.command("pc " + Diana.announceKilltext[0])
                }
            }
        }
    }
}
