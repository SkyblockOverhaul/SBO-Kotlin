package net.sbo.mod.diana

import net.minecraft.entity.decoration.ArmorStandEntity
import net.sbo.mod.utils.events.Register
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.Helper.checkCocoon
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.chat.ChatUtils.formattedString
import net.sbo.mod.utils.events.SBOEvent
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.entity.DianaMobDeathEvent
import net.sbo.mod.utils.events.impl.entity.EntityLoadEvent
import net.sbo.mod.utils.events.impl.entity.EntityUnloadEvent
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayExamples
import net.sbo.mod.utils.overlay.OverlayTextLine
import kotlin.math.roundToInt

object DianaMobDetect {
    private val tracked = mutableMapOf<Int, ArmorStandEntity>()
    private val defeated = mutableSetOf<Int>()
    private val mobHpOverlay: Overlay = Overlay("mythosMobHp", 10f, 10f, 1f, listOf("Chat screen"), OverlayExamples.mythosMobHpExample).setCondition { Diana.mythosMobHp }

    fun init() {
        mobHpOverlay.init()
        Register.onTick(1) {
            val world = mc.world ?: return@onTick
            val overlayLines = mutableListOf<OverlayTextLine>()

            val iterator = tracked.iterator()
            while (iterator.hasNext()) {
                val (id, armorStand) = iterator.next()

                if (!armorStand.isAlive || armorStand.world != world) {
                    iterator.remove()
                    defeated.remove(id)
                    continue
                }

                checkCocoon(armorStand)

                val name = armorStand.customName?.formattedString() ?: armorStand.name.formattedString()
                if (name.isEmpty() || name == "Armor Stand") continue
                if (name.contains("§2✿", ignoreCase = true)) {
                    val health = extractHealth(name)
                    if (health != null && health <= 0 && id !in defeated) {
                        defeated.add(id)
                        SBOEvent.emit(DianaMobDeathEvent(name, armorStand))
                    }
                    overlayLines.add(OverlayTextLine(name))
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