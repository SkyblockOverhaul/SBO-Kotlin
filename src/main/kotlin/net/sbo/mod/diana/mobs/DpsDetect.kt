package net.sbo.mod.diana.mobs

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.entity.EntityPlayerDamageEvent
import net.sbo.mod.utils.events.impl.game.TickEvent
import net.sbo.mod.utils.math.RaycastUtils
import net.sbo.mod.utils.math.SboVec.Companion.toSboVec

object DpsDetect {
    private val healthMap = mutableMapOf<Int, Float>()
    private val damageLeaderboard = mutableMapOf<Int, MutableMap<String, Float>>()
    private val pendingAttacker = mutableMapOf<Int, String>()
    private val mobNames = mutableMapOf<Int, String>()

    private var lastMobStats: Map<String, Float>? = null
    private var lastMobName: String = "Unknown"

    private fun LivingEntity.sboPos() = pos.toSboVec()
    private fun PlayerEntity.sboLook() = getRotationVec(1.0f).toSboVec()
    private fun PlayerEntity.isRealPlayer() = uuid.version() == 4
    private fun String.isRareDianaMob(): Boolean =
        DianaMobDetect.RareDianaMob.entries.any { contains(it.display, ignoreCase = true) }

    @SboEvent
    fun onEntityDamage(event: EntityPlayerDamageEvent) {
        val mob = event.entity as? LivingEntity ?: return
        if (!mob.name.string.isRareDianaMob()) return

        val mobId = mob.id
        val attackerName = (findGuaranteedAttacker(mob) ?: findPotentialAttacker(mob))?.name?.string

        if (attackerName != null) {
            pendingAttacker[mobId] = attackerName
        } else {
            pendingAttacker.putIfAbsent(mobId, "Unknown Source")
        }

        mobNames[mobId] = mob.name.string

        if (!healthMap.containsKey(mobId)) {
            healthMap[mobId] = mob.maxHealth
            processDamageUpdate(mobId, mob.health)
        }
    }

    @SboEvent
    fun onTick(event: TickEvent) {
        val world = mc.world ?: return
        healthMap.keys.toList().forEach { mobId ->
            val mob = world.getEntityById(mobId) as? LivingEntity
            if (mob == null || !mob.isAlive) {
                val lastHP = healthMap[mobId] ?: 0f
                if (lastHP > 0f) processDamageUpdate(mobId, 0f)

                lastMobStats = damageLeaderboard[mobId]?.toMap()
                lastMobName = mobNames[mobId] ?: "Unknown Mob"

                printSummary(mobId)
                cleanup(mobId)
                return@forEach
            }
            processDamageUpdate(mobId, mob.health)
        }
    }

    private fun processDamageUpdate(mobId: Int, currentHealth: Float) {
        val lastHealth = healthMap[mobId] ?: return
        if (currentHealth >= lastHealth) {
            if (currentHealth > lastHealth) healthMap[mobId] = currentHealth
            return
        }

        val damageTaken = lastHealth - currentHealth
        val attacker = pendingAttacker.getOrDefault(mobId, "Unknown Source")

        damageLeaderboard.getOrPut(mobId) { mutableMapOf() }.apply {
            this[attacker] = (this[attacker] ?: 0f) + damageTaken
        }

        healthMap[mobId] = currentHealth
    }

    private fun findGuaranteedAttacker(target: LivingEntity): PlayerEntity? {
        val world = mc.world ?: return null
        if (mc.targetedEntity == target && mc.options.attackKey.isPressed) return mc.player
        val targetBox = target.boundingBox
        return world.players.firstOrNull { player ->
            player.isRealPlayer() && player.distanceTo(target) < 30.0 && player.handSwinging &&
                    RaycastUtils.intersectAABBWithRay(targetBox, RaycastUtils.Ray(player.eyePos.toSboVec(), player.sboLook().normalize())) != null
        }
    }

    private fun findPotentialAttacker(target: LivingEntity): PlayerEntity? {
        val world = mc.world ?: return null
        val targetPos = target.sboPos()
        return world.players
            .filter { it.isRealPlayer() && it.distanceTo(target) < 30.0 }
            .map { player ->
                val dot = (targetPos - player.sboPos()).normalize().dotProduct(player.sboLook())
                player to (if (player.handSwinging) dot + 0.05 else dot)
            }
            .filter { it.second > 0.85 }
            .maxByOrNull { it.second }?.first
    }

    private fun printSummary(mobId: Int) {
        val statsMap = damageLeaderboard[mobId] ?: return
        val stats = statsMap.toList().sortedByDescending { it.second }
        if (stats.isEmpty()) return

        Chat.chat("§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
        Chat.chat("§6§lDPS Summary: §e${mobNames[mobId] ?: "Mob"}")

        stats.forEachIndexed { i, (name, dmg) ->
            val rankColor = when(i) { 0 -> "§e"; 1 -> "§f"; 2 -> "§6"; else -> "§7" }
            val nameColor = if (name == mc.player?.name?.string) "§d" else "§b"
            Chat.chat(" §7${i + 1}. $rankColor$nameColor$name §7- §c${Helper.formatNumber(dmg)}")
        }

        Chat.clickableChat(" §b§l[REPLAY HISTORY]", "§eClick to re-display this list later") {
            showFullHistory()
        }
        Chat.chat("§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
    }

    private fun showFullHistory() {
        val stats = lastMobStats?.toList()?.sortedByDescending { it.second } ?: return

        Chat.chat("§b§m${Chat.getChatBreak(" ", "§b§m")}")
        Chat.chat("§6§lFull Damage History: §e$lastMobName")

        stats.forEachIndexed { i, (name, dmg) ->
            val nameColor = if (name == mc.player?.name?.string) "§d" else "§b"
            Chat.chat(" §7${i + 1}. $nameColor$name §7- §c${Helper.formatNumber(dmg)}")
        }
        Chat.chat("§b§m${Chat.getChatBreak(" ", "§b§m")}")
    }

    fun cleanup(mobId: Int) {
        healthMap.remove(mobId)
        damageLeaderboard.remove(mobId)
        pendingAttacker.remove(mobId)
        mobNames.remove(mobId)
    }
}