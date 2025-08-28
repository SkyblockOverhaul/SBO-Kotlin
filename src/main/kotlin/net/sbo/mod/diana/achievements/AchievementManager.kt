package net.sbo.mod.diana.achievements

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.HypixelModApi.isOnHypixel
import net.sbo.mod.utils.SboTimerManager
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.data.DianaTrackerMayorData
import net.sbo.mod.utils.data.PartyPlayerStats
import net.sbo.mod.utils.data.SboDataObject
import net.sbo.mod.utils.data.SboDataObject.achievementsData
import net.sbo.mod.utils.data.SboDataObject.pastDianaEventsData
import net.sbo.mod.utils.data.SboDataObject.sboData
import net.sbo.mod.utils.events.Register
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

// todo: call all the unlock methods to track achievements also fix the daxe achievements

object AchievementManager {
    val rarityColorDict = mapOf(
        "Common" to "§f",
        "Uncommon" to "§a",
        "Rare" to "§9",
        "Epic" to "§5",
        "Legendary" to "§6",
        "Mythic" to "§d",
        "Divine" to "§b",
        "Impossible" to "§4",
    )

    private val achievements = mutableMapOf<Int, Achievement>()
    var achievementsUnlocked = 0
    private val achievementQueue = ConcurrentLinkedQueue<Int>()
    private val isProcessingQueue = AtomicBoolean(false)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun init() {
        Register.command("sbolockachievements") { args ->
            if (args.getOrNull(0) != "CONFIRM") {
                Chat.chat("§6[SBO] §eYou are about to reset all your achievements. Type §c/sbolockachievements CONFIRM §eto confirm")
                return@command
            }

            achievements.forEach { (_, achievement) ->
                if (achievement.isUnlocked() && achievement.id != 38) {
                    achievement.lock()
                }
            }
            SboDataObject.save("AchievementsData")
            Chat.chat("§6[SBO] §eAchievements locked")
        }

        Register.command("sbobacktrackachievements") {
            backTrackAchievements()
        }
        addAllAchievements()
    }

    fun addAchievement(id: Int, name: String, description: String, rarity: String, previousId: Int? = null, timeout: Int = 1, hidden: Boolean = false) {
        if (achievements.containsKey(id)) {
            throw RuntimeException("Duplicate achievement ID detected: $id. Achievement with this ID already exists: ${achievements[id]?.name}")
        }
        val achievement = Achievement(id, name, description, rarity, previousId, timeout, hidden)
        achievements[id] = achievement
        achievement.loadState()
    }

    fun getAchievement(id: Int): Achievement? {
        return achievements[id]
    }

    fun unlockAchievement(id: Int) {
        if (achievementsData.achievements[id] == null) {
            achievementQueue.add(id)
            processQueue()
        }
    }

    private fun processQueue() {
        if (isProcessingQueue.compareAndSet(false, true)) {
            coroutineScope.launch {
                while (achievementQueue.isNotEmpty()) {
                    val id = achievementQueue.poll()
                    val achievement = getAchievement(id)
                    if (achievement != null && !achievement.isUnlocked()) {
                        if (achievement.previousId != null && !getAchievement(achievement.previousId)?.isUnlocked()!!) {
                            achievementQueue.add(achievement.previousId)
                            achievementQueue.add(id)
                        } else {
                            achievement.unlock()
                            sleep(1000L)
                        }
                    }
                }
                isProcessingQueue.set(false)
            }
        }
    }

    fun lockById(id: Int) {
        if (achievementsData.achievements[id] == null) return
        achievements[id]?.lock()
    }

    fun trackAchievementsItem(tracker: DianaTrackerMayorData) {
        if (isOnHypixel) return
        val itemsData = tracker.items
        val time = itemsData.TIME
        val totalBurrows = itemsData.TOTAL_BURROWS
        val totalChimera = itemsData.CHIMERA + itemsData.CHIMERA_LS
        val daedalusStickCount = itemsData.DAEDALUS_STICK
        val chimeraLsCount = itemsData.CHIMERA_LS

        when {
            totalBurrows >= 25000 -> unlockAchievement(22)
            totalBurrows >= 20000 -> unlockAchievement(21)
            totalBurrows >= 15000 -> unlockAchievement(20)
            totalBurrows >= 10000 -> unlockAchievement(19)
            totalBurrows >= 5000 -> unlockAchievement(18)
        }

        when {
            time >= 86400000L * 3 -> unlockAchievement(27)
            time >= 86400000L * 2 -> unlockAchievement(26)
            time >= 86400000L -> unlockAchievement(25)
            time >= 3600000L * 10 -> unlockAchievement(24)
            time >= 3600000L -> unlockAchievement(23)
        }

        if (itemsData.MINOS_RELIC >= 1) unlockAchievement(16)

        when {
            daedalusStickCount >= 7 -> unlockAchievement(8)
            daedalusStickCount >= 1 -> unlockAchievement(14)
        }

        when {
            totalChimera >= 32 -> unlockAchievement(11)
            totalChimera >= 16 -> unlockAchievement(9)
            totalChimera >= 1 -> unlockAchievement(12)
        }

        when {
            chimeraLsCount >= 16 -> unlockAchievement(10)
            chimeraLsCount >= 1 -> unlockAchievement(13)
        }

        if (time >= 18000000L) { // 5 hours
            val timer = SboTimerManager.timerMayor
            val burrowsPerHour = Helper.getBurrowsPerHr(tracker,timer)
            when {
                burrowsPerHour >= 600 -> unlockAchievement(72)
                burrowsPerHour >= 500 -> unlockAchievement(71)
                burrowsPerHour >= 450 -> unlockAchievement(70)
                burrowsPerHour >= 400 -> unlockAchievement(69)
                burrowsPerHour >= 350 -> unlockAchievement(68)
            }
        }

        if (daedalusStickCount >= 1 && totalChimera >= 2) unlockAchievement(73)
        if (daedalusStickCount >= 1 && itemsData.MINOS_RELIC >= 2) unlockAchievement(74)
    }

    fun trackSinceMob() {
        if (isOnHypixel) return

        when {
            sboData.mobsSinceInq >= 1000 -> unlockAchievement(33)
            sboData.mobsSinceInq >= 500 -> unlockAchievement(32)
            sboData.mobsSinceInq >= 250 -> unlockAchievement(31)
        }

        when {
            sboData.inqsSinceChim >= 100 -> unlockAchievement(37)
            sboData.inqsSinceChim >= 60 -> unlockAchievement(36)
            sboData.inqsSinceChim >= 30 -> unlockAchievement(35)
            sboData.inqsSinceChim >= 15 -> unlockAchievement(34)
        }

        if (sboData.minotaursSinceStick >= 200) unlockAchievement(29)

        when {
            sboData.champsSinceRelic >= 3000 -> unlockAchievement(66)
            sboData.champsSinceRelic >= 1000 -> unlockAchievement(30)
        }
    }

    fun trackMagicFind(magicFind: Int, chimera: Boolean = false) {
        if (isOnHypixel) return

        when {
            magicFind >= 600 -> unlockAchievement(42)
            magicFind >= 500 -> unlockAchievement(41)
            magicFind >= 400 -> unlockAchievement(40)
            magicFind >= 300 -> unlockAchievement(39)
        }

        if (chimera) {
            when {
                magicFind < 100 -> unlockAchievement(43)
                magicFind < 200 -> unlockAchievement(44)
            }
        }
    }

    fun trackBeKills(gaiaKills: Int, champKills: Int, hunterKills: Int, inqKills: Int, minoKills: Int, catKills: Int) {
        if (isOnHypixel) return

        val allMaxed = listOf(
            gaiaKills to 50, inqKills to 45, minoKills to 46,
            champKills to 47, hunterKills to 48, catKills to 49
        ).all { (kills, id) ->
            val isMaxed = when (id) {
                45 -> kills >= 500
                50, 46, 49 -> kills >= 3000
                47, 48 -> kills >= 1000
                else -> false
            }
            if (isMaxed) unlockAchievement(id) else lockById(id)
            isMaxed
        }

        if (allMaxed) unlockAchievement(51) else lockById(51)
    }

    fun backTrackAchievements() {
        Chat.chat("§6[SBO] §eBacktracking Achievements...")
        pastDianaEventsData.events.forEach { eventData ->
            trackAchievementsItem(eventData)
        }
        trackSinceMob()
    }

    fun trackWithCheckPlayer(playerInfo: PartyPlayerStats) {
        if (playerInfo.eman9) unlockAchievement(56)

        when {
            playerInfo.mythosKills >= 150000 -> unlockAchievement(61)
            playerInfo.mythosKills >= 100000 -> unlockAchievement(60)
            playerInfo.mythosKills >= 50000 -> unlockAchievement(59)
            playerInfo.mythosKills >= 25000 -> unlockAchievement(58)
            playerInfo.mythosKills >= 10000 -> unlockAchievement(57)
        }

        when {
            playerInfo.killLeaderboard <= 10 -> unlockAchievement(64)
            playerInfo.killLeaderboard <= 50 -> unlockAchievement(63)
            playerInfo.killLeaderboard <= 100 -> unlockAchievement(62)
        }
    }

    fun addAllAchievements() {
        addAchievement(1, "Back-to-Back Chimera", "Get 2 Chimera in a row", "Mythic")
        addAchievement(2, "b2b2b Chimera", "Get 3 Chimera in a row", "Divine")
        addAchievement(66, "Back-to-Back LS Chimera", "Get 2 Lootshare Chimera in a row", "Divine")
        addAchievement(67, "b2b2b LS Chimera", "Get 3 Lootshare Chimera in a row", "Impossible", 66)
        addAchievement(3, "Back-to-Back Stick", "Get 2 Sticks in a row", "Divine")
        addAchievement(5, "Back-to-Back Relic", "Get 2 Relics in a row", "Impossible")
        addAchievement(6, "Inquisitor Double Trouble", "Get 2 Inquisitors in a row", "Epic")
        addAchievement(7, "b2b2b Inquisitor", "Get 3 Inquisitors in a row", "Divine")
        addAchievement(75, "b2b king", "Get b2b chimera from b2b inquisitor", "Impossible", hidden = true)

        addAchievement(12, "First Chimera", "Get your first Chimera", "Epic")
        addAchievement(9, "Chimera V", "Get 16 chimera in one event", "Mythic", 12)
        addAchievement(11, "Chimera VI", "Get 32 Chimera in one event", "Divine", 9, 2)
        addAchievement(13, "First lootshare Chimera", "Lootshare your first Chimera", "Legendary")
        addAchievement(10, "Tf?", "Get 16 lootshare Chimera in one event", "Divine", 13)

        addAchievement(14, "First Stick", "Get your first Stick", "Uncommon")
        addAchievement(8, "Can i make a ladder now?", "Get 7 Sticks in one event", "Epic", 14)
        addAchievement(15, "1/6250", "Lootshare a Stick (1/6250)", "Impossible", hidden = true)

        addAchievement(16, "First Relic", "Get your first Relic", "Epic")
        addAchievement(17, "1/25000", "Lootshare a Relic (1/25000)", "Impossible", hidden = true)

        addAchievement(18, "Where the grind begins", "Get 5k burrows in one event", "Common")
        addAchievement(19, "Touch some grass", "Get 10k burrows in one event", "Uncommon", 18)
        addAchievement(20, "Please go outside", "Get 15k burrows in one event", "Epic", 19, 2)
        addAchievement(21, "Digging your own grave", "Get 20k burrows in one event", "Legendary", 20, 3)
        addAchievement(22, "Are you mentally stable?", "Get 25k burrows in one event", "Mythic", 21, 4)

        addAchievement(23, "So this is Diana?", "1 hour of playtime in one event", "Common")
        addAchievement(24, "Is this really fun?", "10 hours of playtime in one event", "Uncommon", 23)
        addAchievement(25, "No shower for me", "1 day of playtime in one event", "Rare", 24, 2)
        addAchievement(26, "Are you okay?", "2 days of playtime in one event", "Epic", 25, 3)
        addAchievement(27, "Sleep is downtime!", "3 days of playtime in one event", "Legendary", 26, 4)

        addAchievement(29, "lf Stick", "200 Minotaur since Stick", "Common")
        addAchievement(30, "lf Relic", "1000 Champions since Relic", "Uncommon")
        addAchievement(65, "Where is my Relic?", "3000 champions since Relic", "Mythic", 30, 2)
        addAchievement(31, "lf Inquisitor", "250 mobs since Inquisitor", "Common")
        addAchievement(32, "You have legi Griffin right?", "500 mobs since Inquisitor", "Rare", 31)
        addAchievement(33, "Why do you still play?", "1000 mobs since Inquisitor", "Legendary", 32, 2)
        addAchievement(34, "lf Chimera", "15 Inquisitors since Chimera", "Common")
        addAchievement(35, "So where is my Chimera?", "30 inquisitors since Chimera", "Epic", 34)
        addAchievement(36, "I am done", "60 Inquisitors since Chimera", "Legendary", 35, 2)
        addAchievement(37, "No more Diana", "100 Inquisitors since Chimera", "Divine", 36, 3)

        addAchievement(38, "Real Diana non", "Download SBO", "Divine")

        addAchievement(39, "Fortune seeker", "Get a Diana drop with 300 Magic Find", "Uncommon")
        addAchievement(40, "Blessed by fortune", "Get a Diana drop with 400 Magic Find", "Epic", 39)
        addAchievement(41, "Greed knows no bounds", "Get a Diana drop with 500 Magic Find", "Mythic", 40, 2)
        addAchievement(42, "The principle of luck", "Get a Diana drop with 600 Magic Find", "Divine", 41, 3)
        addAchievement(44, "Magic Find is overrated", "Drop a Chimera, under 200 Magic Find", "Epic")
        addAchievement(43, "I don't need Magic Find", "Drop a Chimera, under 100 Magic Find", "Legendary", 44)

        addAchievement(45, "Inquisitor Slayer", "Max the Inquisitor Bestiary", "Epic")
        addAchievement(46, "Minotaur Slayer", "Max the Minotaur Bestiary", "Legendary")
        addAchievement(47, "Champion Slayer", "Max the Champion Bestiary", "Epic")
        addAchievement(48, "Hunter Slayer", "Max the Hunter Bestiary", "Epic")
        addAchievement(49, "Lynx Slayer", "Max the Siamese Lynx Bestiary", "Epic")
        addAchievement(50, "Gaia Slayer", "Max the Gaia Bestiary", "Legendary")
        addAchievement(51, "Time to get on the leaderboard", "Max all Diana Bestiaries", "Mythic", hidden = true)

        addAchievement(52, "Daedalus Mastery: Chimera V", "Chimera V on Daedalus Axe", "Legendary")
        addAchievement(53, "Daedalus Mastery: Looting V", "Looting V on Daedalus Axe", "Legendary")
        addAchievement(54, "Daedalus Mastery: Divine Gift III", "Divine Gift III on Daedalus Axe", "Legendary")
        addAchievement(55, "Looking Clean", "Get max Divine Gift, Chimera, Looting", "Mythic", hidden = true)
        addAchievement(56, "Now you can't complain", "Obtain Enderman Slayer 9", "Epic", hidden = true)

        addAchievement(57, "Oh look maxed Crest", "Kill 10k Diana Mobs", "Rare")
        addAchievement(58, "Keep the grind going", "Kill 25k Diana Mobs", "Epic", 57)
        addAchievement(59, "I am not addicted", "Kill 50k Diana Mobs", "Legendary", 58, 2)
        addAchievement(60, "100k gang", "Kill 100k Diana Mobs", "Mythic", 59, 3)
        addAchievement(61, "The grind never stops", "Kill 150k Diana Mobs", "Divine", 60, 4, true)
        addAchievement(62, "Mom look i am on the leaderboard", "Top 100 on the kills leaderboard", "Legendary")
        addAchievement(63, "So this is what addiction feels like", "Top 50 on the kills leaderboard", "Mythic", 62)
        addAchievement(64, "Diana is my life", "Top 10 on the kills leaderboard", "Divine", 63, 2)

        addAchievement(68, "Dedicated Digger", "Get 350 burrows/hour (5h playtime)", "Uncommon")
        addAchievement(69, "Burrow Enthusiast", "Get 400 burrows/hour (5h playtime)", "Epic", 68)
        addAchievement(70, "Shovel Expert", "Get 450 burrows/hour (5h playtime)", "Legendary", 69, 2)
        addAchievement(71, "Burrow Maniac", "Get 500 burrows/hour (5h playtime)", "Divine", 70, 3)
        addAchievement(72, "Nice macro!", "Get 600 burrows/hour (5h playtime)", "Impossible", 71, 4, true)

        addAchievement(73, "Can I craft a Chimera sword now?", "Get 1 stick & 2 chimeras in 1 event", "Epic")
        addAchievement(74, "Can I craft a Relic sword now?", "Get 1 stick & 2 relics in 1 event", "Legendary")

        addAchievement(77, "From the ashes", "Drop a Phoenix pet from a Diana mob", "Impossible", hidden = true)
    }
}