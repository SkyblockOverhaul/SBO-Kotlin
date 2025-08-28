package net.sbo.mod.utils

import net.minecraft.client.gui.screen.Screen
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.diana.DianaMobDetect
import net.sbo.mod.diana.DianaTracker
import net.sbo.mod.utils.data.DianaTracker as DianaTrackerDataClass
import net.sbo.mod.overlays.DianaLoot
import net.sbo.mod.settings.categories.Debug
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.data.SboDataObject
import kotlin.concurrent.thread
import net.sbo.mod.utils.data.DianaItemsData
import net.sbo.mod.utils.data.DianaMobsData
import net.sbo.mod.utils.data.HypixelBazaarResponse
import net.sbo.mod.utils.data.Item
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.game.Mayor
import net.sbo.mod.utils.game.ScoreBoard
import net.sbo.mod.utils.game.World
import net.sbo.mod.utils.http.Http
import net.sbo.mod.utils.waypoint.WaypointManager.onLootshare
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.full.memberProperties
import java.text.DecimalFormat
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.roundToLong

object Helper {
    var lastLootShare: Long = 0L
    var allowSackTracking: Boolean = true
    var hasSpade: Boolean = false
    var lastDianaMobDeath: Long = 0L
    var lastInqDeath: Long = 0L
    var currentScreen: Screen? = null

    private var hasTrackedInq: Boolean = false
    private var prevInv = mutableMapOf<String, Item>()
    private var dianaMobNames: List<String> = listOf("Minos Inquisitor", "Minotaur", "Minos Champion", "Gaia Construct", "Azrael", "Bagheera", "Minos Hunter")
    private var priceDataAh: Map<String, Long> = emptyMap()
    private var priceDataBazaar: HypixelBazaarResponse? = null

    fun init() {
        Register.onChatMessageCancable(Pattern.compile("^§e§lLOOT SHARE §fYou received loot for assisting (.*?)$", Pattern.DOTALL)) { message, matchResult ->
            onLootshare()
            lastLootShare = System.currentTimeMillis()
            true
        }

        Register.onGuiOpen { screen, ci ->
            sleep(200) {
                currentScreen = screen
                if (screen.title.string == "Sack of Sacks") allowSackTracking = false
            }
        }

        Register.onGuiClose { screen ->
            sleep(200) {
                currentScreen = null
            }
        }

        Register.onTick(20) { // maybe better way to register this
            hasSpade = playerHasItem("ANCESTRAL_SPADE")
        }

        Register.onTick(20 * 60 * 5) {
            updateItemPriceInfo()
        }
        updateItemPriceInfo()

        DianaMobDetect.onMobDeath { name, entity ->
            val dist = entity.distanceTo(mc.player)
            if (name.contains("Minos Inquisitor")) {
                if (getSecondsPassed(lastLootShare) < 2 && !hasTrackedInq) {
                    hasTrackedInq = true
                    DianaTracker.trackItem("MINOS_INQUISITOR_LS", 1)
                    sleep(2000) {
                        hasTrackedInq = false
                    }
                }
                lastInqDeath = System.currentTimeMillis()
            }
            if (dist <= 30) {
                allowSackTracking = true
                lastDianaMobDeath = System.currentTimeMillis()
            }
        }
    }

    /**
     * Sleeps for the specified number of milliseconds and then executes the callback.
     * This is done in a separate thread to avoid blocking the main thread.
     *
     * @param milliseconds The number of milliseconds to sleep.
     * @param callback The function to execute after sleeping.
     */
    fun sleep(milliseconds: Long, callback: () -> Unit) {
        thread(isDaemon = true) {
            Thread.sleep(milliseconds)
            callback()
        }
    }

    fun getPlayerName(player: String): String {
        var name = player
        val num = name.indexOf(']')
        if (num != -1) {
            name = name.substring(num + 2)
        }
        name = name.replace(Regex("§[0-9a-fk-or]"), "")
        name = name.replace(Regex("[^a-zA-Z0-9_]"), "")
        return name.trim()
    }

    fun calcPercentOne(items: DianaItemsData, mobs: DianaMobsData, itemName: String, mobName: String? = null): String {
        val result: Double = if (mobName != null) {
            val itemCount = items::class.memberProperties.firstOrNull { it.name == itemName }
                ?.call(items) as? Int ?: 0
            val mobCount = mobs::class.memberProperties.firstOrNull { it.name == mobName }
                ?.call(mobs) as? Int ?: 0

            if (mobCount <= 0) 0.0
            else (itemCount.toDouble() / mobCount.toDouble() * 100)
        } else {
            val mobCount = mobs::class.memberProperties.firstOrNull { it.name == itemName }
                ?.call(mobs) as? Int ?: 0
            val totalMobsCount = mobs.TOTAL_MOBS

            if (totalMobsCount <= 0) 0.0
            else (mobCount.toDouble() / totalMobsCount.toDouble() * 100)
        }
        return "%.2f".format(Locale.US, result)
    }

    fun formatNumber(number: Number?, withCommas: Boolean = false): String {
        val num = number?.toDouble() ?: 0.0

        if (withCommas) {
            // Format with commas
            val formatter = DecimalFormat("#,###")
            return formatter.format(num)
        } else {
            // Format with suffixes (k, m, b)
            return when {
                num >= 1_000_000_000 -> "%.2fb".format(num / 1_000_000_000)
                num >= 1_000_000 -> "%.1fm".format(num / 1_000_000)
                num >= 1_000 -> "%.1fk".format(num / 1_000)
                else -> "%.0f".format(num)
            }
        }
    }

    fun formatTime(milliseconds: Long): String {
        if (milliseconds <= 0) {
            return "0s"
        }

        val totalSeconds = (milliseconds / 1000).toInt()
        val totalMinutes = totalSeconds / 60
        val totalHours = totalMinutes / 60
        val days = totalHours / 24
        val hours = totalHours % 24
        val minutes = totalMinutes % 60
        val seconds = totalSeconds % 60

        val builder = StringBuilder()

        if (days > 0) {
            builder.append("${days}d ")
        }
        if (hours > 0 || days > 0) {
            builder.append("${hours}h ")
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            builder.append("${minutes}m ")
        }
        if (builder.isEmpty()) {
            builder.append("${seconds}s")
        }

        return builder.toString().trim()
    }

    fun String.removeFormatting(): String {
        return this.replace(Regex("§."), "")
    }

    fun matchLvlToColor(lvl: Int): String {
        return when {
            lvl >= 480 -> "§4$lvl"
            lvl >= 440 -> "§c$lvl"
            lvl >= 400 -> "§6$lvl"
            lvl >= 360 -> "§5$lvl"
            lvl >= 320 -> "§d$lvl"
            lvl >= 280 -> "§9$lvl"
            lvl >= 240 -> "§3$lvl"
            lvl >= 200 -> "§b$lvl"
            else -> "§7$lvl"
        }
    }

    fun getNumberColor(number: Int, range: Int): String {
        return when (number) {
            range -> "§c$number"
            range - 1 -> "§6$number"
            else -> "§9$number"
        }
    }

    fun getGriffinItemColor(item: String?): String {
        if (item.isNullOrEmpty()) return ""
        val name = item.replace("PET_ITEM_", "").replace("_", " ").replaceFirstChar { it.uppercase() }
        return when (name) {
            "Four Eyed Fish" -> "§5$name"
            "Dwarf Turtle Shelmet" -> "§a$name"
            "Crochet Tiger Plushie" -> "§5$name"
            "Antique Remedies" -> "§5$name"
            "Lucky Clover" -> "§a$name"
            "Minos Relic" -> "§5$name"
            else -> "§7$name"
        }
    }

    fun getRarity(item: String): String {
        return when (item.lowercase().trim()) {
            "common" -> "§f$item"
            "uncommon" -> "§a$item"
            "rare" -> "§9$item"
            "epic" -> "§5$item"
            "legendary" -> "§6$item"
            "mythic" -> "§d$item"
            else -> "§7$item"
        }
    }

    fun matchDianaKillsToColor(kills: Int): String {
        return when {
            kills >= 200_000 -> "§6${formatNumber(kills, true)}"
            kills >= 150_000 -> "§e${formatNumber(kills, true)}"
            kills >= 100_000 -> "§c${formatNumber(kills, true)}"
            kills >= 75_000 -> "§d${formatNumber(kills, true)}"
            kills >= 50_000 -> "§9${formatNumber(kills, true)}"
            kills >= 25_000 -> "§a${formatNumber(kills, true)}"
            kills >= 10_000 -> "§2${formatNumber(kills, true)}"
            else -> "§7${formatNumber(kills, true)}"
        }
    }

    fun getPurse(): Long {
        val lines = ScoreBoard.getLines()
        if (lines.isEmpty()) return 0L
        val purseLine = lines.find { it.contains("Purse: ") }
        return if (purseLine != null) {
            val purseValue = purseLine.substringAfter("Purse: ")
            val numericValue = purseValue.split(" ")[0]
            numericValue.replace(",", "").toLongOrNull() ?: 0L
        } else {
            0L
        }
    }

    fun getCursorItemStack(): ItemStack? {
        val handler = mc.player?.currentScreenHandler ?: return null
        return handler.cursorStack
    }

    fun readPlayerInv(): MutableMap<String, Item> {
        val inventory = Player.getPlayerInventory()
        val invItems = mutableMapOf<String, Item>()

        if (getCursorItemStack()?.count != 0) return prevInv // todo: do this but only of the overlay

        for (slot in 0 until inventory.size) {
            if (slot == 8) continue // Skip SB Star
            val stack: ItemStack = inventory[slot]

            if (!stack.isEmpty) {
                val customData = stack.get(DataComponentTypes.CUSTOM_DATA)
                val item = Item(
                    ItemUtils.getSBID(customData),
                    ItemUtils.getUUID(customData),
                    ItemUtils.getDisplayName(stack),
                    ItemUtils.getTimestamp(customData),
                    stack.count
                )
                val id = if (item.itemUUID != "") item.itemUUID else item.itemId
                if (invItems.containsKey(id)) {
                    invItems[id]?.count += item.count
                } else {
                    invItems[id] = item
                }
            }
        }
        prevInv = invItems
        return invItems
    }

    fun timestampToDate(timestamp: Long): String {
        if (timestamp <= 0) return "Unknown"
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return format.format(date)
    }

    fun toUpperSnakeCase(input: String): String {
        return input.replace("-", " ").split(" ").joinToString("_") { it.uppercase() }
    }

    fun getSecondsPassed(timestamp: Long): Long {
        return (System.currentTimeMillis() - timestamp) / 1000
    }

    fun playerHasItem(sbId: String): Boolean {
        val inv = Player.getPlayerInventory()
        for (i in inv.indices) {
            val stack = inv[i]
            if (!stack.isEmpty && ItemUtils.getSBID(stack.get(DataComponentTypes.CUSTOM_DATA)) == sbId) {
                return true
            }
        }
        return false
    }

    fun checkDiana(): Boolean {
        val diana = (Debug.itsAlwaysDiana || ((Mayor.perks.contains("Mythological Ritual") || Mayor.mayor == "Jerry") && hasSpade && World.getWorld() == "Hub"))
        return diana
    }

    fun getGuiName(): String {
        return currentScreen?.title?.string ?: ""
    }

    fun showTitle(title: String?, subtitle: String?, fadeIn: Int, time: Int, fadeOut: Int) {
        mc.inGameHud.apply {
            setTitleTicks(fadeIn, time, fadeOut)
            if (title != null)
                setTitle(net.minecraft.text.Text.of(title))
            if (subtitle != null)
                setSubtitle(net.minecraft.text.Text.of(subtitle))
        }
    }

    fun checkCustomChimMessage(magicFind: Int): Pair<Boolean, String> {
        val text = Diana.customChimMessage[0].trim()
        val trackerMayor = SboDataObject.dianaTrackerMayor
        if (!Diana.chimMessageBool) {
            return Pair(false, "")
        }

        if (text.isNotEmpty()) {
            var resultText = text.replace('&', '§')

            if (resultText.contains("{mf}")) {
                val mfMessage = if (magicFind > 0) {
                    "(+$magicFind% ✯ Magic Find)"
                } else {
                    ""
                }
                resultText = resultText.replace("{mf}", mfMessage)
            }

            if (resultText.contains("{amount}")) {
                val amount = trackerMayor.items.CHIMERA + trackerMayor.items.CHIMERA_LS
                resultText = resultText.replace("{amount}", amount.toString())
            }

            if (resultText.contains("{percentage}")) {
                val minosInquisitorCount = trackerMayor.mobs.MINOS_INQUISITOR
                val chimeraCount = trackerMayor.items.CHIMERA
                val percentage = if (minosInquisitorCount > 0) {
                    (chimeraCount.toDouble() / minosInquisitorCount.toDouble()) * 100
                } else {
                    0.0
                }
                resultText = resultText.replace("{percentage}", "%.2f%%".format(percentage))
            }

            return Pair(true, resultText)
        } else {
            return Pair(false, "")
        }
    }

    fun toTitleCase(input: String): String {
        return input.lowercase().replaceFirstChar { char -> char.uppercase() }
    }

    fun getMagicFind(mf: String): Int {
        val mfMatch = Regex("""§b\(\+§b(\d+)""").find(mf)
        if (mfMatch != null) {
            val mfValue = mfMatch.groupValues[1].toIntOrNull() ?: 0
            return mfValue
        }
        return 0
    }

    fun updateItemPriceInfo() {
        Http.sendGetRequest("https://api.skyblockoverhaul.com/ahItems")
            .toJson<List<Map<String, Map<String, Long>>>> { json ->
                priceDataAh = json.flatMap { it.entries }.associate { it.key to it.value["price"]!! }
                DianaLoot.updateLines()
            }.error { error ->
//                Chat.chat("§6[SBO] §4Unexpected error while fetching AH item prices: $error")
            }
        Http.sendGetRequest("https://api.hypixel.net/skyblock/bazaar?product")
            .toJson<HypixelBazaarResponse> {
                priceDataBazaar = it
                DianaLoot.updateLines()
            }.error { error ->
//                Chat.chat("§6[SBO] §4Unexpected error while fetching Bazaar item prices: $error")
            }
    }


    fun getItemPrice(sbId: String, amount: Int = 1): Long {
        val id = if (sbId == "CHIMERA") "ENCHANTMENT_ULTIMATE_CHIMERA_1" else sbId
        var ahPrice = priceDataAh[id]?.toDouble() ?: 0.0
        if (id == "CROWN_OF_GREED")
            ahPrice = if (ahPrice < 1000000.0) 1000000.0 else ahPrice

        val bazaarPrice = if (Diana.bazaarSettingDiana == Diana.SettingDiana.INSTASELL) {
            priceDataBazaar?.products?.get(id)?.quick_status?.sellPrice
        }
        else {
            priceDataBazaar?.products?.get(id)?.quick_status?.buyPrice
        }

        return when {
            ahPrice != 0.0 -> (ahPrice * amount).roundToLong()
            bazaarPrice != null -> (bazaarPrice * amount).roundToLong()
            else -> 0L
        }
    }

    fun getItemPriceFormatted(sbId: String, amount: Int = 1): String {
        val price = getItemPrice(sbId, amount)
        return formatNumber(price)
    }

    /**
     * Checks if the player has received loot share recently.
     * @param timeframe The timeframe in seconds to check against. Default is 2 seconds.
     */
    fun gotLootShareRecently(timeframe: Long = 2): Boolean {
        return getSecondsPassed(lastLootShare) <= timeframe
    }

    fun dianaMobDiedRecently(seconds: Long = 2): Boolean {
        return getSecondsPassed(lastDianaMobDeath) <= seconds
    }

    fun getBurrowsPerHr(tracker: DianaTrackerDataClass, timer: SboTimerManager.SBOTimer): Double {
        val hours = timer.getHourTime()
        if (hours <= 0.01) return 0.0
        val totalBurrows = tracker.items.TOTAL_BURROWS.toDouble()
        val burrowsPerHr = totalBurrows / hours
        return BigDecimal(burrowsPerHr).setScale(2, RoundingMode.HALF_UP).toDouble()
    }
}

