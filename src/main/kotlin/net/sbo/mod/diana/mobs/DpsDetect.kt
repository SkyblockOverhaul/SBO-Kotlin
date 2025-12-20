package net.sbo.mod.diana.mobs

import net.minecraft.entity.LivingEntity
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.diana.mobs.indicator.DamageIndicator
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.collection.TimeLimitedSet
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.entity.EntityPlayerDamageEvent
import net.sbo.mod.utils.events.impl.entity.PlayerMeleeAttackEvent
import net.sbo.mod.utils.events.impl.game.TickEvent
import net.sbo.mod.utils.math.SboVec
import net.sbo.mod.diana.mobs.indicator.DamageIndicatorRenderer
import net.sbo.mod.utils.events.impl.render.WorldRenderEvent
import kotlin.time.Duration.Companion.seconds
import kotlin.math.max

object DpsDetect {
    private val healthMap = mutableMapOf<Int, Float>()
    private val mobMaxHealth = mutableMapOf<Int, Float>()
    private val mobSpawnTime = mutableMapOf<Int, Long>()
    private val damageLeaderboard = mutableMapOf<Int, MutableMap<String, Float>>()
    private val mobNames = mutableMapOf<Int, String>()
    private val damageHistory = mutableMapOf<Int, MutableList<Pair<String, Float>>>()

    private val lastDirectAttacker = mutableMapOf<Int, String>()
    private val printedMobs = TimeLimitedSet<Int>(10.seconds)

    private var lastMobHistory: List<Pair<String, Float>>? = null
    private var lastMobName: String = "Unknown"
    private var lastMobMaxHp: Float = 0f

    val activeIndicators = mutableListOf<DamageIndicator>()

    private fun String.isRareDianaMob(): Boolean =
        DianaMobDetect.RareDianaMob.entries.any { contains(it.display, ignoreCase = true) }

    fun primeMob(mob: LivingEntity, owner: String? = null) {
        val mobId = mob.id
        if (!healthMap.containsKey(mobId)) {
            healthMap[mobId] = mob.health
            mobMaxHealth[mobId] = max(mob.health, mob.maxHealth)
            mobNames[mobId] = mob.name.string
            mobSpawnTime[mobId] = System.currentTimeMillis()
            if (owner != null) lastDirectAttacker[mobId] = owner
        }
    }

    @SboEvent
    fun onMeleeAttack(event: PlayerMeleeAttackEvent) {
        if (!Diana.dpsTracker) return
        lastDirectAttacker[event.target.id] = event.attacker.name.string
    }

    @SboEvent
    fun onEntityDamage(event: EntityPlayerDamageEvent) {
        if (!Diana.dpsTracker) return
        val mob = event.entity as? LivingEntity ?: return
        if (!mob.name.string.isRareDianaMob()) return

        primeMob(mob)
        processDamageUpdate(mob.id, mob.health)
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

                val statsMap = damageLeaderboard[mobId]
                val totalDamage = statsMap?.values?.sum() ?: 0f
                lastMobMaxHp = max(mobMaxHealth[mobId] ?: 0f, totalDamage)

                cleanup(mobId)
                return@forEach
            }
            processDamageUpdate(mobId, mob.health)
        }
    }

    @SboEvent
    fun onRender(event: WorldRenderEvent) {
        DamageIndicatorRenderer.onWorldRender(event.context)
    }

    private fun processDamageUpdate(mobId: Int, currentHealth: Float) {
        val lastHealth = healthMap[mobId] ?: return
        if (currentHealth >= lastHealth) {
            healthMap[mobId] = currentHealth
            return
        }

        val damageTaken = lastHealth - currentHealth

        // Anti-sync check
        val spawnTime = mobSpawnTime[mobId] ?: 0L
        if (System.currentTimeMillis() - spawnTime < 150) {
            healthMap[mobId] = currentHealth
            return
        }

        var attacker = lastDirectAttacker[mobId]
        if (attacker == null) {
            val target = mc.targetedEntity
            if (target != null && target.id == mobId) {
                attacker = mc.player?.name?.string
            }
        }

        val finalAttacker = attacker ?: "Unknown/Magic"

        if (finalAttacker == mc.player?.name?.string) {
            val mob = mc.world?.getEntityById(mobId)
            if (mob != null) {
                activeIndicators.add(
                    DamageIndicator(
                        mobId,
                        SboVec(mob.x, mob.y + mob.height, mob.z),
                        damageTaken
                    )
                )
            }
        }

        damageLeaderboard.getOrPut(mobId) { mutableMapOf() }.apply {
            this[finalAttacker] = (this[finalAttacker] ?: 0f) + damageTaken
        }

        damageHistory.getOrPut(mobId) { mutableListOf() }.add(finalAttacker to damageTaken)
        healthMap[mobId] = currentHealth
    }

    private fun displaySummary(mobId: Int) {
        if (!printedMobs.add(mobId)) return
        val statsMap = damageLeaderboard[mobId] ?: return
        val name = mobNames[mobId] ?: "Unknown Mob"

        val stats = statsMap.toList().sortedByDescending { it.second }
        val totalDamage = stats.sumOf { it.second.toDouble() }.toFloat()
        val finalMaxHp = max(mobMaxHealth[mobId] ?: 0f, totalDamage)

        if (stats.isEmpty()) return

        val chatBreak = Chat.getChatBreak("-", "§a")
        Chat.chat(chatBreak)
        Chat.chat("§6§lDPS Summary: §e$name")
        Chat.chat(" §7Total Damage: §c${Helper.formatNumber(totalDamage)} §8/ §7Max HP: §a${Helper.formatNumber(finalMaxHp)}")
        Chat.chat("")

        stats.forEachIndexed { i, (pName, dmg) ->
            val rankColor = when(i) { 0 -> "§e"; 1 -> "§f"; 2 -> "§6"; else -> "§7" }
            val nameColor = if (pName == mc.player?.name?.string) "§d" else "§b"
            val percentVal = if (finalMaxHp > 0) (dmg / finalMaxHp * 100).toInt() else 0

            Chat.chat(" §7${i + 1}. $rankColor$nameColor$pName §7- §c${Helper.formatNumber(dmg)} §8($percentVal%)")
        }

        Chat.chat("")
        Chat.clickableChat(" §b§l[VIEW CHRONOLOGICAL HISTORY]", "§eClick to see every individual hit in order") {
            showFullHistory()
        }
        Chat.chat(chatBreak)
    }

    private fun showFullHistory() {
        val history = lastMobHistory ?: return
        val chatBreak = Chat.getChatBreak(" ", "§a§m")
        Chat.chat(chatBreak)
        Chat.chat("§6§lIndividual Hit Log: §e$lastMobName §8(Max HP: ${Helper.formatNumber(lastMobMaxHp)})")

        history.forEachIndexed { i, (name, dmg) ->
            val nameColor = if (name == mc.player?.name?.string) "§d" else "§b"
            Chat.chat(" §7${i + 1}. $nameColor$name §7- §c${Helper.formatNumber(dmg)}")
        }
        Chat.chat(chatBreak)
    }

    fun getClientTotal(mobId: Int): Float {
        val statsMap = damageLeaderboard[mobId] ?: return 0f
        val playerName = mc.player?.name?.string ?: return 0f
        return statsMap[playerName] ?: 0f
    }

    fun getActiveMobIds(): Set<Int> = healthMap.keys

    private fun clearAllData() {
        healthMap.clear()
        mobMaxHealth.clear()
        mobSpawnTime.clear()
        damageLeaderboard.clear()
        damageHistory.clear()
        lastDirectAttacker.clear()
        mobNames.clear()
    }

    fun cleanup(mobId: Int) {
        displaySummary(mobId)
        healthMap.remove(mobId)
        mobMaxHealth.remove(mobId)
        mobSpawnTime.remove(mobId)
        damageLeaderboard.remove(mobId)
        damageHistory.remove(mobId)
        lastDirectAttacker.remove(mobId)
        mobNames.remove(mobId)
    }
}