package net.sbo.mod.diana.mobs

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.collection.TimeLimitedSet
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.entity.EntityPlayerDamageEvent
import net.sbo.mod.utils.events.impl.game.TickEvent
import net.sbo.mod.utils.math.RaycastUtils
import net.sbo.mod.utils.math.SboVec.Companion.toSboVec
import kotlin.time.Duration.Companion.seconds

object DpsDetect {
    private val healthMap = mutableMapOf<Int, Float>()
    private val damageLeaderboard = mutableMapOf<Int, MutableMap<String, Float>>()
    private val pendingAttacker = mutableMapOf<Int, String>()
    private val mobNames = mutableMapOf<Int, String>()
    private val damageHistory = mutableMapOf<Int, MutableList<Pair<String, Float>>>()

    private val printedMobs = TimeLimitedSet<Int>(10.seconds)

    private var lastMobHistory: List<Pair<String, Float>>? = null
    private var lastMobName: String = "Unknown"

    //#if MC >= 1.21.9
    //$$ private fun LivingEntity.sboPos() = entityPos.toSboVec()
    //#else
    private fun LivingEntity.sboPos() = pos.toSboVec()
    //#endif
    private fun PlayerEntity.sboLook() = getRotationVec(1.0f).toSboVec()
    private fun PlayerEntity.isRealPlayer() = uuid.version() == 4
    private fun String.isRareDianaMob(): Boolean =
        DianaMobDetect.RareDianaMob.entries.any { contains(it.display, ignoreCase = true) }

    @SboEvent
    fun onEntityDamage(event: EntityPlayerDamageEvent) {
        if (!Diana.dpsTracker) return

        val mob = event.entity as? LivingEntity ?: return
        val name = mob.name.string
        if (!name.isRareDianaMob()) return

        val mobId = mob.id

        val attacker = findGuaranteedAttacker(mob) ?: findPotentialAttacker(mob)
        val attackerName = attacker?.name?.string

        if (attackerName != null) {
            pendingAttacker[mobId] = attackerName
        } else {
            pendingAttacker.putIfAbsent(mobId, "Unknown Source")
        }

        mobNames[mobId] = name

        if (!healthMap.containsKey(mobId)) {
            healthMap[mobId] = mob.health
        }

        processDamageUpdate(mobId, mob.health)
    }

    @SboEvent
    fun onTick(event: TickEvent) {
        if (!Diana.dpsTracker) {
            if (healthMap.isNotEmpty()) clearAllData()
            return
        }
        val world = mc.world ?: return
        healthMap.keys.toList().forEach { mobId ->
            val mob = world.getEntityById(mobId) as? LivingEntity

            if (mob == null || !mob.isAlive) {
                val lastHP = healthMap[mobId] ?: 0f
                if (lastHP > 0f) processDamageUpdate(mobId, 0f)

                lastMobHistory = damageHistory[mobId]?.toList()
                lastMobName = mobNames[mobId] ?: "Unknown Mob"

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
        damageHistory.getOrPut(mobId) { mutableListOf() }.add(attacker to damageTaken)

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

    private fun displaySummary(mobId: Int) {
        if (!printedMobs.add(mobId)) return

        val statsMap = damageLeaderboard[mobId] ?: return
        val name = mobNames[mobId] ?: "Unknown Mob"
        if (statsMap.isEmpty()) return

        val stats = statsMap.toList().sortedByDescending { it.second }

        val chatBreak = Chat.getChatBreak(" ", "§a§m")
        Chat.chat(chatBreak)

        Chat.chat("§6§lDPS Summary: §e$name")
        stats.forEachIndexed { i, (pName, dmg) ->
            val rankColor = when(i) { 0 -> "§e"; 1 -> "§f"; 2 -> "§6"; else -> "§7" }
            val nameColor = if (pName == mc.player?.name?.string) "§d" else "§b"
            Chat.chat(" §7${i + 1}. $rankColor$nameColor$pName §7- §c${Helper.formatNumber(dmg)}")
        }

        Chat.clickableChat(" §b§l[VIEW CHRONOLOGICAL HISTORY]", "§eClick to see every individual hit in order") {
            showFullHistory()
        }

        Chat.chat(chatBreak)
    }

    private fun showFullHistory() {
        val history = lastMobHistory ?: return
        val chatBreak = Chat.getChatBreak(" ", "§a§m")
        Chat.chat(chatBreak)
        Chat.chat("§6§lIndividual Hit Log: §e$lastMobName")
        history.forEachIndexed { i, (name, dmg) ->
            val nameColor = if (name == mc.player?.name?.string) "§d" else "§b"
            Chat.chat(" §7${i + 1}. $nameColor$name §7- §c${Helper.formatNumber(dmg)}")
        }
        Chat.chat(chatBreak)
    }

    private fun clearAllData() {
        healthMap.clear()
        damageLeaderboard.clear()
        damageHistory.clear()
        pendingAttacker.clear()
        mobNames.clear()
    }

    fun cleanup(mobId: Int) {
        displaySummary(mobId)
        healthMap.remove(mobId)
        damageLeaderboard.remove(mobId)
        damageHistory.remove(mobId)
        pendingAttacker.remove(mobId)
        mobNames.remove(mobId)
    }
}