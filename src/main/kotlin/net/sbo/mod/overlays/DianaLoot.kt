package net.sbo.mod.overlays
/* todo: Refactoring
* This Code definetly needs refactoring, but I don't have the time to do it right now.
* I will do it in the future, but for now it works and I don't want to break it.
* If you want to refactor it, feel free to do so, but please keep the functionality intact.
*/
import net.sbo.mod.settings.categories.Diana
import net.sbo.mod.utils.overlay.Overlay
import net.sbo.mod.utils.overlay.OverlayTextLine
import net.minecraft.util.Formatting.*
import net.sbo.mod.SBOKotlin.mc
import net.sbo.mod.utils.Helper
import net.sbo.mod.utils.Helper.calcPercentOne
import net.sbo.mod.utils.Helper.removeFormatting
import net.sbo.mod.utils.SboTimerManager
import net.sbo.mod.utils.data.DianaTracker
import net.sbo.mod.utils.data.SboDataObject.SBOConfigBundle
import net.sbo.mod.utils.events.Register
import net.sbo.mod.utils.render.RenderUtils2D
import java.util.concurrent.TimeUnit

object DianaLoot {
    private var isSellTypeHovered = false
    val timerLine: OverlayTextLine = OverlayTextLine("")
    val overlay = Overlay("Diana Loot", 10f, 10f, 1f, listOf("Chat screen", "Crafting")).setCondition { Diana.lootTracker != Diana.Tracker.OFF }
    val changeView: OverlayTextLine = OverlayTextLine("${YELLOW}Change View", linebreak = false)
        .onClick {
            Diana.lootTracker = Diana.lootTracker.next()
            updateLines()
        }
        .onMouseEnter {
            changeView.text = "$YELLOW${UNDERLINE}Change View"
        }
        .onMouseLeave {
            changeView.text = "${YELLOW}Change View"
        }

    val delimiter = OverlayTextLine(" | ", linebreak = false)

    val changeSellType: OverlayTextLine = OverlayTextLine("")
        .onClick {
            Diana.bazaarSettingDiana = Diana.bazaarSettingDiana.next()
            updateLines()
        }
        .onMouseEnter {
            isSellTypeHovered = true
            updateLines()
        }
        .onMouseLeave {
            isSellTypeHovered = false
            updateLines()
        }

    val resetSession : OverlayTextLine = OverlayTextLine("${RED}Reset Session")
        .onClick {
            SboTimerManager.timerSession.reset()
            SBOConfigBundle.dianaTrackerSessionData.reset().save()
            updateLines()
            DianaMobs.updateLines()
        }
        .onMouseEnter {
            resetSession.text = "$RED${UNDERLINE}Reset Session"
        }
        .onMouseLeave {
            resetSession.text = "${RED}Reset Session"
        }

    fun init() {
        overlay.init()
        updateLines()
        updateTimerText()
        Register.onGuiOpen { screen, ci ->
            if (screen.title.string == "Crafting") {
                updateLines("CraftingOpen")
            }
        }
        Register.onGuiClose { screen ->
            if (screen.title.string == "Crafting") {
                overlay.removeLine(changeView)
                overlay.removeLine(delimiter)
                overlay.removeLine(changeSellType)
                overlay.removeLine(resetSession)
            }
        }
        Register.onTick(1) {
            updateTimerText()
        }
    }

    fun createLine(name: String, formattedText: String) : OverlayTextLine {
        val line = OverlayTextLine(formattedText).onClick {
            if (mc.currentScreen?.title?.string != "Crafting") return@onClick
            if (SBOConfigBundle.sboData.hideTrackerLines.contains(name)) {
                SBOConfigBundle.sboData.hideTrackerLines.remove(name)
            } else {
                SBOConfigBundle.sboData.hideTrackerLines.add(name)
            }
            updateLines()
        }
            .setCondition { !(mc.currentScreen?.title?.string != "Crafting" && SBOConfigBundle.sboData.hideTrackerLines.contains(name)) }
        if (SBOConfigBundle.sboData.hideTrackerLines.contains(name)) {
            line.text = "$GRAY$STRIKETHROUGH${formattedText.removeFormatting()}"
        }
        return line
    }

    fun updateLines(screen: String = "") {
        val lines = mutableListOf<OverlayTextLine>()
        val type = Diana.lootTracker
        val totalEvents: Int = SBOConfigBundle.pastDianaEventsData.events.size
        val tracker = when (type) {
            Diana.Tracker.TOTAL -> SBOConfigBundle.dianaTrackerTotalData
            Diana.Tracker.EVENT -> SBOConfigBundle.dianaTrackerMayorData
            Diana.Tracker.SESSION -> SBOConfigBundle.dianaTrackerSessionData
            Diana.Tracker.OFF -> {
                overlay.setLines(emptyList())
                return
            }
        }
        val timer = when (type) {
            Diana.Tracker.TOTAL -> SboTimerManager.timerTotal
            Diana.Tracker.EVENT -> SboTimerManager.timerMayor
            Diana.Tracker.SESSION -> SboTimerManager.timerSession
            else -> SboTimerManager.timerMayor
        }

        val chimPercent = calcPercentOne(tracker.items, tracker.mobs, "CHIMERA", "MINOS_INQUISITOR")
        val chimLsPercent = calcPercentOne(tracker.items, tracker.mobs, "CHIMERA_LS", "MINOS_INQUISITOR_LS")
        val relicPercent = calcPercentOne(tracker.items, tracker.mobs, "MINOS_RELIC", "MINOS_CHAMPION")
        val stickPercent = calcPercentOne(tracker.items, tracker.mobs, "DAEDALUS_STICK", "MINOTAUR")
        val playTimeHrs = tracker.items.TIME.toDouble() / TimeUnit.HOURS.toMillis(1)
        val burrowsPerHr = Helper.getBurrowsPerHr(tracker, timer)
        val bphText = if (burrowsPerHr.isNaN() || burrowsPerHr == 0.0) {
            ""
        } else {
            " $GRAY[$AQUA$burrowsPerHr$GRAY/${AQUA}hr$GRAY]"
        }
        val chimPrice = Helper.getItemPriceFormatted("CHIMERA", tracker.items.CHIMERA)
        val chimLsPrice = Helper.getItemPriceFormatted("CHIMERA", tracker.items.CHIMERA_LS)
        val relicPrice = Helper.getItemPriceFormatted("MINOS_RELIC", tracker.items.MINOS_RELIC)
        val stickPrice = Helper.getItemPriceFormatted("DAEDALUS_STICK", tracker.items.DAEDALUS_STICK)
        val crownPrice = Helper.getItemPriceFormatted("CROWN_OF_GREED", tracker.items.CROWN_OF_GREED)
        val sovenirPrice = Helper.getItemPriceFormatted("WASHED_UP_SOUVENIR", tracker.items.WASHED_UP_SOUVENIR)
        val featherPrice = Helper.getItemPriceFormatted("GRIFFIN_FEATHER", tracker.items.GRIFFIN_FEATHER)
        val shelmetPrice = Helper.getItemPriceFormatted("DWARF_TURTLE_SHELMET", tracker.items.DWARF_TURTLE_SHELMET)
        val plushiePrice = Helper.getItemPriceFormatted("CROCHET_TIGER_PLUSHIE", tracker.items.CROCHET_TIGER_PLUSHIE)
        val remediesPrice = Helper.getItemPriceFormatted("ANTIQUE_REMEDIES", tracker.items.ANTIQUE_REMEDIES)
        val clawPrice= Helper.getItemPriceFormatted("ANCIENT_CLAW", tracker.items.ANCIENT_CLAW)
        val echClawPrice = Helper.getItemPriceFormatted("ENCHANTED_ANCIENT_CLAW", tracker.items.ENCHANTED_ANCIENT_CLAW)
        val echGoldPrice = Helper.getItemPriceFormatted("ENCHANTED_GOLD", tracker.items.ENCHANTED_GOLD)
        val echIronPrice = Helper.getItemPriceFormatted("ENCHANTED_IRON", tracker.items.ENCHANTED_IRON)
        val profitPerHr = if (playTimeHrs > 0) {
            Helper.formatNumber(totalProfit(tracker) / playTimeHrs)
        } else 0.0
        val poriftPerBurrow = if (tracker.items.TOTAL_BURROWS > 0) {
            Helper.formatNumber(totalProfit(tracker) / tracker.items.TOTAL_BURROWS)
        } else 0.0
        val sellTypeText = if (isSellTypeHovered) {
            "$YELLOW${UNDERLINE}${Helper.toTitleCase(Diana.bazaarSettingDiana.toString())}"
        } else {
            "${YELLOW}${Helper.toTitleCase(Diana.bazaarSettingDiana.toString())}"
        }
        changeSellType.text = sellTypeText

        if (screen == "CraftingOpen" || mc.currentScreen?.title?.string == "Crafting") {
            lines.add(changeView)
            lines.add(delimiter)
            lines.add(changeSellType)
        }

        lines.add(OverlayTextLine("$YELLOW${BOLD}Diana Loot $GRAY($YELLOW${Helper.toTitleCase(type.toString())}$GRAY)"))

        lines.addAll(
            listOf(
                createLine("CHIMERA", "$GOLD$chimPrice $GRAY|$LIGHT_PURPLE Chimera: $AQUA${Helper.formatNumber(tracker.items.CHIMERA, true)} $GRAY($AQUA${chimPercent}%$GRAY)"),
                createLine("CHIMERA_LS", "$GOLD$chimLsPrice $GRAY|$LIGHT_PURPLE Chimera $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.CHIMERA_LS, true)} $GRAY($AQUA${chimLsPercent}%$GRAY)"),
                createLine("MINOS_RELIC", "$GOLD$relicPrice $GRAY|$DARK_PURPLE Minos Relic: $AQUA${Helper.formatNumber(tracker.items.MINOS_RELIC, true)} $GRAY($AQUA${relicPercent}%$GRAY)"),
                createLine("DAEDALUS_STICK", "$GOLD$stickPrice $GRAY|$GOLD Daedalus Stick: $AQUA${Helper.formatNumber(tracker.items.DAEDALUS_STICK, true)} $GRAY($AQUA${stickPercent}%$GRAY)"),
                createLine("CROWN_OF_GREED", "$GOLD$crownPrice $GRAY|$GOLD Crown of Greed: $AQUA${Helper.formatNumber(tracker.items.CROWN_OF_GREED, true)}"),
                createLine("WASHED_UP_SOUVENIR", "$GOLD$sovenirPrice $GRAY|$GOLD Washed-up Souvenir: $AQUA${Helper.formatNumber(tracker.items.WASHED_UP_SOUVENIR, true)}"),
                createLine("GRIFFIN_FEATHER", "$GOLD$featherPrice $GRAY|$GOLD Griffin Feather: $AQUA${Helper.formatNumber(tracker.items.GRIFFIN_FEATHER, true)}"),
                createLine("DWARF_TURTLE_SHELMET", "$GOLD$shelmetPrice $GRAY|$DARK_GREEN Dwarf Turtle Helmet: $AQUA${Helper.formatNumber(tracker.items.DWARF_TURTLE_SHELMET, true)}"),
                createLine("CROCHET_TIGER_PLUSHIE", "$GOLD$plushiePrice $GRAY|$DARK_GREEN Crochet Tiger Plushie: $AQUA${Helper.formatNumber(tracker.items.CROCHET_TIGER_PLUSHIE, true)}"),
                createLine("ANTIQUE_REMEDIES", "$GOLD$remediesPrice $GRAY|$DARK_GREEN Antique Remedies: $AQUA${Helper.formatNumber(tracker.items.ANTIQUE_REMEDIES, true)}"),
                createLine("ANCIENT_CLAW", "$GOLD$clawPrice $GRAY|$BLUE Ancient Claw: $AQUA${Helper.formatNumber(tracker.items.ANCIENT_CLAW)}"),
                createLine("ENCHANTED_ANCIENT_CLAW", "$GOLD$echClawPrice $GRAY|$BLUE Enchanted Ancient Claw: $AQUA${Helper.formatNumber(tracker.items.ENCHANTED_ANCIENT_CLAW,)}"),
                createLine("ENCHANTED_GOLD", "$GOLD$echGoldPrice $GRAY|$BLUE Enchanted Gold: $AQUA${Helper.formatNumber(tracker.items.ENCHANTED_GOLD)}"),
                createLine("ENCHANTED_IRON", "$GOLD$echIronPrice $GRAY|$BLUE Enchanted Iron: $AQUA${Helper.formatNumber(tracker.items.ENCHANTED_IRON)}"),
                OverlayTextLine("${GRAY}Total Burrows: $AQUA${Helper.formatNumber(tracker.items.TOTAL_BURROWS, true)}$bphText"),
                OverlayTextLine("${GOLD}Total Coins: $AQUA${Helper.formatNumber(tracker.items.COINS)}")
                    .onHover { drawContext, textRenderer ->
                        val scaleFactor = mc.window.scaleFactor
                        val mouseX = mc.mouse.x / scaleFactor
                        val mouseY = mc.mouse.y / scaleFactor
                        RenderUtils2D.drawHoveringString(drawContext,
                                "$YELLOW${BOLD}Coin Break Down:\n" +
                                "${GOLD}Treasure: $AQUA${Helper.formatNumber(tracker.items.COINS - tracker.items.FISH_COINS - tracker.items.SCAVENGER_COINS)}\n" +
                                "${GOLD}Four-Eyed Fish: $AQUA${Helper.formatNumber(tracker.items.FISH_COINS)}\n" +
                                "${GOLD}Scavenger: $AQUA${Helper.formatNumber(tracker.items.SCAVENGER_COINS)}",
                            mouseX, mouseY, textRenderer, overlay.scale)
                    },
                OverlayTextLine("${YELLOW}Total Profit: $AQUA${Helper.formatNumber(totalProfit(tracker))} coins")
                    .onHover { drawContext, textRenderer ->
                        val scaleFactor = mc.window.scaleFactor
                        val mouseX = mc.mouse.x / scaleFactor
                        val mouseY = mc.mouse.y / scaleFactor
                        RenderUtils2D.drawHoveringString(drawContext,
                            "$GOLD$profitPerHr coins/hr\n" +
                                "$GOLD$poriftPerBurrow coins/burrow",
                            mouseX, mouseY, textRenderer, overlay.scale)
                    }
            )
        )
        lines.add(timerLine)
        if (type == Diana.Tracker.TOTAL) lines.add(OverlayTextLine("${YELLOW}Total Events: $AQUA$totalEvents"))
        if (screen == "CraftingOpen" || mc.currentScreen?.title?.string == "Crafting" && type == Diana.Tracker.SESSION) lines.add(resetSession)
        overlay.setLines(lines)
    }

    fun totalProfit(tracker: DianaTracker): Long {
        var totalProfit = 0L
        for (item in tracker.items::class.java.declaredFields) {
            item.isAccessible = true
            val itemName = item.name
            if (itemName == "TIME" || itemName == "TOTAL_BURROWS" || itemName == "COINS" || itemName == "SCAVENGER_COINS" || itemName == "FISH_COINS" || itemName == "CHIMERA_LS") continue
            val itemValue = item.get(tracker.items) as Int
            if (itemValue <= 0) continue
            val itemPrice = Helper.getItemPrice(itemName)
            if (itemPrice > 0) {
                totalProfit += itemPrice * itemValue
            }
        }
        return totalProfit + tracker.items.COINS + Helper.getItemPrice("CHIMERA", tracker.items.CHIMERA_LS)
    }

    fun updateTimerText() {
        val type = Diana.lootTracker
        val tracker = when (type) {
            Diana.Tracker.TOTAL -> SBOConfigBundle.dianaTrackerTotalData
            Diana.Tracker.EVENT -> SBOConfigBundle.dianaTrackerMayorData
            Diana.Tracker.SESSION -> SBOConfigBundle.dianaTrackerSessionData
            Diana.Tracker.OFF -> {
                timerLine.text = ""
                return
            }
        }

        val timer = when (type) {
            Diana.Tracker.TOTAL -> SboTimerManager.timerTotal
            Diana.Tracker.EVENT -> SboTimerManager.timerMayor
            Diana.Tracker.SESSION -> SboTimerManager.timerSession
            else -> return
        }

        val formattedTime = Helper.formatTime(tracker.items.TIME)
        val text = if (timer.running) {
            "${YELLOW}Playtime: $AQUA$formattedTime"
        } else {
            "${YELLOW}Playtime: $AQUA$formattedTime ${GRAY}[${RED}PAUSED${GRAY}]"
        }
        timerLine.text = text
    }
}