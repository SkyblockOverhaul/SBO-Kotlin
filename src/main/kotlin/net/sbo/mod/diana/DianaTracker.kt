package net.sbo.mod.diana

import net.sbo.mod.diana.achievements.AchievementManager.trackMagicFind
import net.sbo.mod.diana.achievements.AchievementManager.unlockAchievement
import net.sbo.mod.overlays.DianaLoot
import net.sbo.mod.overlays.DianaMobs
import net.sbo.mod.overlays.DianaStats
import net.sbo.mod.overlays.InquisLoot
import net.sbo.mod.overlays.MagicFind
import net.sbo.mod.settings.categories.Customization
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.settings.categories.QOL
import net.sbo.mod.utils.chat.Chat
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.Helper.allowSackTracking
import net.sbo.mod.utils.Helper.checkDiana
import net.sbo.mod.utils.Helper.dianaMobDiedRecently
import net.sbo.mod.utils.Helper.gotLootShareRecently
import net.sbo.mod.utils.Helper.lastDianaMobDeath
import net.sbo.mod.utils.Helper.lastInqDeath
import net.sbo.mod.utils.Helper.lastLootShare
import net.sbo.mod.utils.Helper.removeFormatting
import net.sbo.mod.utils.Helper.sleep
import net.sbo.mod.utils.Mayor
import net.sbo.mod.utils.Mayor.getMayor
import net.sbo.mod.utils.Player
import net.sbo.mod.utils.SboTimerManager
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.SboTimerManager.timerSession
import net.sbo.mod.utils.SoundHandler.playCustomSound
import net.sbo.mod.utils.World.isInSkyblock
import net.sbo.mod.utils.data.DianaTracker
import net.sbo.mod.utils.data.DianaTrackerMayorData
import net.sbo.mod.utils.data.DianaTrackerSessionData
import net.sbo.mod.utils.data.Item
import net.sbo.mod.utils.data.SboDataObject
import net.sbo.mod.utils.data.SboDataObject.dianaTrackerMayor
import net.sbo.mod.utils.data.SboDataObject.dianaTrackerSession
import net.sbo.mod.utils.data.SboDataObject.dianaTrackerTotal
import net.sbo.mod.utils.data.SboDataObject.pastDianaEventsData
import net.sbo.mod.utils.data.SboDataObject.saveTrackerData
import net.sbo.mod.utils.data.SboDataObject.sboData
import java.util.regex.Pattern

object DianaTracker {
    private val rareDrops = mapOf<String, String>("DWARF_TURTLE_SHELMET" to "§9", "CROCHET_TIGER_PLUSHIE" to "§5", "ANTIQUE_REMEDIES" to "§5", "MINOS_RELIC" to "§5")
    private val otherDrops = listOf("ENCHANTED_ANCIENT_CLAW", "ANCIENT_CLAW", "ENCHANTED_GOLD", "ENCHANTED_IRON")
    private val sackDrops = listOf("Enchanted Gold", "Enchanted Iron", "Ancient Claw", "Enchanted Ancient Claw")
    private val forbiddenCoins = listOf(1L, 5L, 20L, 1000L, 2000L, 3000L, 4000L, 5000L, 7500L, 8000L, 10000L, 12000L, 15000L, 20000L, 25000L, 40000L, 50000L)

    private val lootAnnouncerBuffer: MutableList<String> = mutableListOf()
    private var lootAnnouncerBool: Boolean = false

    fun init() {
        Register.command("sboresetsession") {
            dianaTrackerSession.reset().save()
            Chat.chat("§6[SBO] §aDiana session tracker has been reset.")
            DianaMobs.updateLines()
            DianaLoot.updateLines()
            InquisLoot.updateLines()
        }

        Register.command("sboresetstatstracker") {
            sboData.mobsSinceInq = 0
            sboData.inqsSinceChim = 0
            sboData.minotaursSinceStick = 0
            sboData.champsSinceRelic = 0
            sboData.inqsSinceLsChim = 0
            SboDataObject.save("SboData")
            DianaStats.updateLines()
        }

        Register.onChatMessageCancable(
            Pattern.compile("^§eThe election room is now closed\\. Clerk Seraphine is doing a final count of the votes\\.\\.\\.$", Pattern.DOTALL)
        ) { _, _ ->
            checkMayorTracker()
            true
        }

        Register.onChatMessageCancable(Pattern.compile("(.*?) §efound a §cPhoenix §epet!(.*?)$", Pattern.DOTALL)) { message, matchResult ->
            if (QOL.phoenixAnnouncer) {
                Chat.chat("§6[SBO] §cGG §eFound a §cPhoenix §epet!")
                Helper.showTitle("§c§lPhoenix Pet!", "", 0, 25, 35)
            }
            if (Helper.getSecondsPassed(lastDianaMobDeath) > 2) return@onChatMessageCancable true
            val player = matchResult.group(2).removeFormatting()
            if (Player.getName() != Helper.getPlayerName(player)) return@onChatMessageCancable true
            sleep(1000) {
                if (isInSkyblock() && checkDiana() && dianaMobDiedRecently(3)) unlockAchievement(77) // phoenix pet
            }
            true
        }

        trackBurrowsWithChat()
        trackMobsWithChat()
        trackCoinsWithChat()
        trackTreasuresWithChat()
        trackRngDropsWithChat()
    }

    fun trackWithPickuplog(item: Item) {
        val isLootShare = gotLootShareRecently(2)
        if (Helper.getSecondsPassed(item.creation) > 2) return
        if (!checkDiana()) return
        if (item.itemId in rareDrops.keys) {
            val msg = Helper.toTitleCase(item.itemId.replace("_", " "))
            if (item.itemId == "MINOS_RELIC") {
                playCustomSound(Customization.relicSound[0], Customization.relicVolume)
                if (Diana.sendSinceMessage) Chat.chat("§6[SBO] §eTook §c${sboData.champsSinceRelic} §eChampions to get a Minos Relic!")
                if (sboData.champsSinceRelic == 1) {
                    Chat.chat("&6[SBO] &cb2b Minos Relic!")
                    unlockAchievement(5) // b2b relic
                }
                if (isLootShare) {
                    Chat.chat("§6[SBO] §cLootshared a Minos Relic!")
                    unlockAchievement(17) // relic ls
                }
                sboData.champsSinceRelic = 0

                if (Diana.lootAnnouncerScreen) {
                    val subTitle = if (Diana.lootAnnouncerPrice) "§6${Helper.getItemPriceFormatted(item.itemId)} coins" else ""
                    Helper.showTitle("§d§lMinos Relic!", subTitle, 0, 25, 35)
                }

                announceLootToParty(item.itemId)
                SboDataObject.save("SboData")
            } else {
                playCustomSound(Customization.sprSound[0], Customization.sprVolume)
            }

            if (Diana.lootAnnouncerChat) {
                Chat.chat("§6[SBO] §6§lRARE DROP! ${rareDrops[item.itemId]}$msg")
            }

            if (Helper.getSecondsPassed(lastInqDeath) > 2 || item.itemId == "MINOS_RELIC") {
                trackItem(item.itemId, item.count)
            } else {
                announceLootToParty(item.itemId)
                if (!isLootShare)
                    trackItem(item.itemId, item.count, true)
                else
                    trackItem(item.itemId + "_LS", item.count, true)
            }
        }
    }

    fun trackWithPickuplogStackable(item: Item, amount: Int) {
        sleep (1000) {
            if (!dianaMobDiedRecently(3) && gotLootShareRecently(3)) return@sleep
            if (!checkDiana()) return@sleep
            if (item.itemId in otherDrops) {
                trackItem(item.itemId, amount)
                return@sleep
            }
        }
    }

    fun trackWithSacksMessage(itemName: String, amount: Int) {
        if (!allowSackTracking) return
        if (!checkDiana()) return
        if (sackDrops.contains(itemName)) {
            trackItem(itemName, amount)
        }
    }

    fun trackScavengerCoins(amount: Long) {
        if (amount <= 0) return
        if (!dianaMobDiedRecently(4) && !gotLootShareRecently(4)) return
        if (!checkDiana()) return
        if (amount in forbiddenCoins) return
        trackItem("SCAVENGER_COINS", amount.toInt())
        trackItem("COINS", amount.toInt())
    }

    fun trackMobsWithChat() {
        Register.onChatMessageCancable(Pattern.compile("(.*?) §eYou dug (.*?)§2(.*?)§e!(.*?)$", Pattern.DOTALL)) { message, matchResult ->
            val mob = matchResult.group(3)
            when (mob) {
                "Minos Inquisitor" -> {
                    DianaMobDetect.onInqSpawn()
                    sboData.inqsSinceChim += 1
                    trackMob(mob, 1)

                    if (Diana.sendSinceMessage) {
                        val timeSinceInq = Helper.formatTime(dianaTrackerTotal.items.TIME - sboData.lastInqDate)
                        if (sboData.lastInqDate != 0L) {
                            Chat.chat("§6[SBO] §eTook §c${sboData.mobsSinceInq} §eMobs and §c$timeSinceInq §eto get an Inquis!")
                        } else {
                            Chat.chat("§6[SBO] §eTook §c${sboData.mobsSinceInq} §eMobs to get an Inquis!")
                        }
                    }
                    sboData.lastInqDate = dianaTrackerTotal.items.TIME

                    if (sboData.b2bInq && sboData.mobsSinceInq == 1) {
                        Chat.chat("§6[SBO] §cb2b2b Inquisitor!")
                        unlockAchievement(7) // b2b2b inq
                    }
                    if (sboData.mobsSinceInq == 1 && !sboData.b2bInq) {
                        Chat.chat("§6[SBO] §cb2b Inquisitor!")
                        unlockAchievement(6) // b2b inq
                        sboData.b2bInq = true
                    }
                    if (sboData.inqsSinceChim >= 2) sboData.b2bChim = false

                    sboData.mobsSinceInq = 0
                }
                "Minos Champion" -> {
                    sboData.champsSinceRelic += 1
                    trackMob(mob, 1)
                }
                "Minotaur" -> {
                    sboData.minotaursSinceStick += 1
                    if (sboData.minotaursSinceStick >= 2) sboData.b2bStick = false
                    trackMob(mob, 1)
                }
                "Gaia Construct" -> trackMob(mob, 1)
                "Siamese Lynxes" -> trackMob(mob, 1)
                "Minos Hunter" -> trackMob(mob, 1)
            }
            SboDataObject.save("SboData")
            !QOL.dianaMessageHider
        }
    }

    fun trackCoinsWithChat() {
        Register.onChatMessageCancable(Pattern.compile("^§6§lWow! §eYou dug out §6(.*?) coins§e!$", Pattern.DOTALL)) { message, matchResult ->
            val coins = matchResult.group(1).replace(",", "").toIntOrNull() ?: 0
            if (coins > 0) trackItem("COINS", coins)
            true
        }
    }

    fun trackTreasuresWithChat() {
        Register.onChatMessageCancable(Pattern.compile("^§6§lRARE DROP! §eYou dug out a (.*?)§e!$", Pattern.DOTALL)) { message, matchResult ->
            val drop = matchResult.group(1).drop(2)
            when (drop) {
                "Griffin Feather" -> trackItem(drop, 1)
                "Crown of Greed" -> trackItem(drop, 1)
                "Washed-up Souvenir" -> trackItem(drop, 1)
            }
            true
        }
    }

    fun trackRngDropsWithChat() {
        Register.onChatMessageCancable(Pattern.compile("^§6§lRARE DROP! (.*?)$", Pattern.DOTALL)) { message, matchResult ->
            if (!checkDiana()) return@onChatMessageCancable true
            val drop = matchResult.group(1)
            val isLootShare = gotLootShareRecently(2)

            val magicfind = Helper.getMagicFind(drop)
            var mfPrefix = ""
            if (magicfind > 0) mfPrefix = " (+$magicfind ✯ Magic Find)"
            when (drop.substring(2, 16)) {
                "Enchanted Book" -> {
                    if (!drop.contains("Chimera")) return@onChatMessageCancable true

                    playCustomSound(Customization.chimSound[0], Customization.chimVolume)
                    if (Diana.lootAnnouncerScreen) {
                        val subTitle = if (Diana.lootAnnouncerPrice) "§6${Helper.getItemPriceFormatted("CHIMERA")} coins" else ""
                        Helper.showTitle("§d§lChimera!", subTitle, 0, 25, 35)
                    }

                    if (!isLootShare) {
                        // normal chim
                        trackMagicFind(magicfind, true)
                        if (magicfind > sboData.highestChimMagicFind) sboData.highestChimMagicFind = magicfind
                        if (Diana.sendSinceMessage) Chat.chat("§6[SBO] §eTook §c${sboData.inqsSinceChim} §eInquisitors to get a Chimera!")

                        trackItem("CHIMERA", 1)
                        if (sboData.b2bChim && sboData.inqsSinceChim == 1) {
                            Chat.chat("&6[SBO] &cb2b2b Chimera!")
                            unlockAchievement(2) // b2b2b chim
                        }
                        if (sboData.inqsSinceChim == 1 && !sboData.b2bChim) {
                            Chat.chat("&6[SBO] &cb2b Chimera!")
                            sboData.b2bChim = true
                            unlockAchievement(1) // b2b chim
                        }
                        if (sboData.b2bChim && sboData.b2bInq) {
                            unlockAchievement(75) // b2b chim from b2b inq
                        }
                        sboData.inqsSinceChim = 0
                    } else {
                        // lootshare chim
                        if (Diana.sendSinceMessage) Chat.chat("§6[SBO] §eTook §c${sboData.inqsSinceLsChim} §eInquisitors to lootshare a Chimera!")

                        trackItem("CHIMERA_LS", 1)

                        sleep(200) {
                            if (sboData.b2bChimLs && sboData.inqsSinceLsChim == 1) {
                                Chat.chat("&6[SBO] &cb2b2b Lootshare Chimera!")
                                unlockAchievement(67) // b2b2b chim ls
                            }
                            if (sboData.inqsSinceLsChim == 1 && !sboData.b2bChimLs) {
                                Chat.chat("&6[SBO] &cb2b Lootshare Chimera!")
                                sboData.b2bChimLs = true
                                unlockAchievement(65) // b2b chim ls
                            }
                            sboData.inqsSinceLsChim = 0
                        }
                    }

                    val customChimMsg = Helper.checkCustomChimMessage(magicfind)
                    if (customChimMsg.first) {
                        Chat.chat(customChimMsg.second)
                        announceLootToParty("Chimera!", customChimMsg.second, true)
                    } else {
                        announceLootToParty("Chimera!", "Chimera!$mfPrefix")
                    }
                }
                "Daedalus Stick" -> {
                    playCustomSound(Customization.stickSound[0], Customization.stickVolume)
                    if (Diana.lootAnnouncerScreen) {
                        val subTitle = if (Diana.lootAnnouncerPrice) "§6${Helper.getItemPriceFormatted("DAEDALUS_STICK")} coins" else ""
                        Helper.showTitle("§d§lDaedalus Stick!", subTitle, 0, 25, 35)
                    }

                    if (Diana.sendSinceMessage) Chat.chat("§6[SBO] §eTook §c${sboData.minotaursSinceStick} §eMinotaurs to get a Daedalus Stick!")
                    announceLootToParty("Daedalus Stick!", "Daedalus Stick!$mfPrefix")

                    if (isLootShare) { // lootshare stick
                        Chat.chat("§6[SBO] §cLootshared a Daedalus Stick!")
                        unlockAchievement(15)
                    } else if (magicfind > sboData.highestStickMagicFind) {
                        sboData.highestStickMagicFind = magicfind
                        trackMagicFind(magicfind)
                    }

                    trackItem("DAEDALUS_STICK", 1)
                    if (sboData.b2bStick && sboData.minotaursSinceStick == 1) {
                        Chat.chat("&6[SBO] &cb2b2b Daedalus Stick!")
                        unlockAchievement(4) // b2b2b stick
                    }
                    if (sboData.minotaursSinceStick == 1 && !sboData.b2bStick) {
                        Chat.chat("&6[SBO] &cb2b Daedalus Stick!")
                        sboData.b2bStick = true
                        unlockAchievement(3) // b2b stick
                    }
                    sboData.minotaursSinceStick = 0
                }
            }
            SboDataObject.save("SboData")
            true
        }
    }

    fun trackBurrowsWithChat() {
        Register.onChatMessageCancable(Pattern.compile("^§eYou (.*?) Griffin [Bb]urrow(.*?)$", Pattern.DOTALL)) { message, matchResult ->
            val burrow = matchResult.group(2).removeFormatting()
            trackItem("TOTAL_BURROWS", 1)
            if (Diana.fourEyedFish) {
                if (burrow.contains("(2/4)") || burrow.contains("(3/4)")) {
                    trackItem("FISH_COINS", 4000)
                    trackItem("COINS", 4000)
                } else {
                    trackItem("FISH_COINS", 2000)
                    trackItem("COINS", 2000)
                }
            }
            !QOL.dianaMessageHider
        }
    }

    fun announceLootToParty(item: String, customMsg: String? = null, replaceChimMessage: Boolean = false) {
        if (!Diana.lootAnnouncerParty) return
        var msg = Helper.toTitleCase(item.replace("_LS", "").replace("_", " "))
        if (customMsg != null) msg = customMsg.removeFormatting()

        if (replaceChimMessage) {
            Chat.command("pc $msg")
        } else {
            lootAnnouncerBuffer.add(msg)
            if (!lootAnnouncerBool) {
                lootAnnouncerBool = true
                sleep(1500) {
                    sendLootAnnouncement()
                    lootAnnouncerBool = false
                }
            }
        }
    }

    fun sendLootAnnouncement() {
        if (lootAnnouncerBuffer.isEmpty()) return
        val msg = lootAnnouncerBuffer.joinToString(", ")
        lootAnnouncerBuffer.clear()
        Chat.command("pc [SBO] RARE DROP! $msg")
    }

    fun getB2BMessage(itemName: String, streak: Int): String? { // not used yet
        if (streak <= 1) return null

        val prettyName = Helper.toTitleCase(itemName.replace("_", " "))
        val streakText = "b" + "2b".repeat(streak - 1)

        return "§6[SBO] §c$streakText $prettyName!"
    }

    fun checkMayorTracker() {
        if (dianaTrackerMayor.year == 0 || dianaTrackerMayor.year >= Mayor.mayorElectedYear) return
        var allZero = true
        for (item in dianaTrackerMayor.mobs::class.java.declaredFields) {
            item.isAccessible = true
            if (item.get(dianaTrackerMayor.mobs) is Int) {
                if (item.getInt(dianaTrackerMayor.mobs) > 0) {
                    allZero = false
                    break
                }
            }
        }
        if (!allZero) {
            pastDianaEventsData.events += dianaTrackerMayor
            SboDataObject.save("PastDianaEventsData")
        }
        dianaTrackerMayor.reset()
        dianaTrackerMayor.year = Mayor.mayorElectedYear
        dianaTrackerMayor.save()
        getMayor()
        DianaMobs.updateLines()
        DianaLoot.updateLines()
        InquisLoot.updateLines()
    }

    fun trackMob(item: String, amount: Int) {
        trackItem(item, amount)
        trackItem("TOTAL_MOBS", amount)
        sboData.mobsSinceInq += amount
        if (sboData.mobsSinceInq >= 2) sboData.b2bInq = false
        SboDataObject.save("SboData")
    }

    fun trackItem(item: String, amount: Int, fromInq: Boolean = false) {
        checkMayorTracker()
        val itemName = Helper.toUpperSnakeCase(item)
        if (itemName == "MINOS_INQUISITOR_LS") sboData.inqsSinceLsChim += 1

        trackOne(dianaTrackerMayor, itemName, amount, fromInq)
        trackOne(dianaTrackerSession, itemName, amount, fromInq)
        trackOne(dianaTrackerTotal, itemName, amount, fromInq)
        saveTrackerData()
        DianaStats.updateLines()
        MagicFind.updateLines()
        DianaMobs.updateLines()
        DianaLoot.updateLines()
        if (fromInq) InquisLoot.updateLines()
        SboTimerManager.updateAllActivity()
    }

    fun trackOne(tracker: DianaTracker, item: String, amount: Int, fromInq: Boolean = false) {
        when (item) {
            // ITEMS
            "COINS" -> tracker.items.COINS += amount
            "GRIFFIN_FEATHER" -> tracker.items.GRIFFIN_FEATHER += amount
            "CROWN_OF_GREED" -> tracker.items.CROWN_OF_GREED += amount
            "WASHED_UP_SOUVENIR" -> tracker.items.WASHED_UP_SOUVENIR += amount
            "CHIMERA" -> tracker.items.CHIMERA += amount
            "CHIMERA_LS" -> tracker.items.CHIMERA_LS += amount
            "DAEDALUS_STICK" -> tracker.items.DAEDALUS_STICK += amount
            "DWARF_TURTLE_SHELMET" -> tracker.items.DWARF_TURTLE_SHELMET += amount
            "CROCHET_TIGER_PLUSHIE" -> tracker.items.CROCHET_TIGER_PLUSHIE += amount
            "ANTIQUE_REMEDIES" -> tracker.items.ANTIQUE_REMEDIES += amount
            "ENCHANTED_ANCIENT_CLAW" -> tracker.items.ENCHANTED_ANCIENT_CLAW += amount
            "ANCIENT_CLAW" -> tracker.items.ANCIENT_CLAW += amount
            "MINOS_RELIC" -> tracker.items.MINOS_RELIC += amount
            "ENCHANTED_GOLD" -> tracker.items.ENCHANTED_GOLD += amount
            "ENCHANTED_IRON" -> tracker.items.ENCHANTED_IRON += amount
            "SCAVENGER_COINS" -> tracker.items.SCAVENGER_COINS += amount
            "FISH_COINS" -> tracker.items.FISH_COINS += amount
            "TOTAL_BURROWS" -> tracker.items.TOTAL_BURROWS += amount
            // MOBS
            "MINOS_INQUISITOR" -> tracker.mobs.MINOS_INQUISITOR += amount
            "MINOS_INQUISITOR_LS" -> tracker.mobs.MINOS_INQUISITOR_LS += amount
            "MINOS_CHAMPION" -> tracker.mobs.MINOS_CHAMPION += amount
            "MINOTAUR" -> tracker.mobs.MINOTAUR += amount
            "GAIA_CONSTRUCT" -> tracker.mobs.GAIA_CONSTRUCT += amount
            "SIAMESE_LYNXES" -> tracker.mobs.SIAMESE_LYNXES += amount
            "MINOS_HUNTER" -> tracker.mobs.MINOS_HUNTER += amount
            "TOTAL_MOBS" -> tracker.mobs.TOTAL_MOBS += amount
        }

        if (fromInq) {
            when (item) {
                // ITEMS from inquis
                "DWARF_TURTLE_SHELMET" -> tracker.inquis.DWARF_TURTLE_SHELMET += amount
                "DWARF_TURTLE_SHELMET_LS" -> tracker.inquis.DWARF_TURTLE_SHELMET_LS += amount
                "CROCHET_TIGER_PLUSHIE" -> tracker.inquis.CROCHET_TIGER_PLUSHIE += amount
                "CROCHET_TIGER_PLUSHIE_LS" -> tracker.inquis.CROCHET_TIGER_PLUSHIE_LS += amount
                "ANTIQUE_REMEDIES" -> tracker.inquis.ANTIQUE_REMEDIES += amount
                "ANTIQUE_REMEDIES_LS" -> tracker.inquis.ANTIQUE_REMEDIES_LS += amount
            }
        }
    }
}