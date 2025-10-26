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
import net.sbo.mod.utils.events.annotations.SboEvent
import net.sbo.mod.utils.events.impl.guis.GuiCloseEvent
import net.sbo.mod.utils.events.impl.guis.GuiOpenEvent
import net.sbo.mod.utils.render.RenderUtils2D
import java.util.concurrent.TimeUnit

object DianaLoot {
    private var isSellTypeHovered = false
    val timerLine: OverlayTextLine = OverlayTextLine("")
    val overlay = Overlay("Diana Loot", 10f, 10f, 1f, listOf("Chat screen", "Crafting")).setCondition { Diana.lootTracker != Diana.Tracker.OFF && (Helper.checkDiana() || Helper.hasSpade) }
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
        Register.onTick(1) {
            updateTimerText()
        }
    }

    @SboEvent
    fun onGuiClose(event: GuiCloseEvent) {
        if (event.screen.title.string == "Crafting") {
            overlay.removeLine(changeView)
            overlay.removeLine(delimiter)
            overlay.removeLine(changeSellType)
            overlay.removeLine(resetSession)
        }
    }

    @SboEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.screen.title.string == "Crafting") {
            updateLines("CraftingOpen")
        }
    }

    fun hideLine(name: String) {
        if (mc.currentScreen?.title?.string != "Crafting") return
        if (SBOConfigBundle.sboData.hideTrackerLines.contains(name)) {
            SBOConfigBundle.sboData.hideTrackerLines.remove(name)
        } else {
            SBOConfigBundle.sboData.hideTrackerLines.add(name)
        }
        updateLines()
    }

    fun createLine(name: String, formattedText: String, amount: Int) : OverlayTextLine {
        val line = OverlayTextLine(formattedText).onClick { hideLine(name) }
            .setCondition {
                val meetsZeroValueCondition = amount > 0 || !Diana.hideUnobtainedItems
                val meetsManualHideCondition = !(mc.currentScreen?.title?.string != "Crafting" && SBOConfigBundle.sboData.hideTrackerLines.contains(name))
                meetsZeroValueCondition && meetsManualHideCondition
            }
        if (SBOConfigBundle.sboData.hideTrackerLines.contains(name)) {
            line.text = "$GRAY$STRIKETHROUGH${formattedText.removeFormatting()}"
        }
        return line
    }

    fun createCombinedLine(nameBase: String, formattedTextBase: String, amountBase: Int, formattedTextLS: String, amountLS: Int): OverlayTextLine {
        val combinedText = "$formattedTextBase $GRAY[${AQUA}LS$GRAY:$AQUA${Helper.formatNumber(amountLS, true)}$GRAY]"
        val line = OverlayTextLine(combinedText).onClick { hideLine(nameBase) }
            .setCondition {
                val totalAmount = amountBase + amountLS
                val meetsZeroValueCondition = totalAmount > 0 || !Diana.hideUnobtainedItems
                val meetsManualHideCondition = !(mc.currentScreen?.title?.string != "Crafting" && SBOConfigBundle.sboData.hideTrackerLines.contains(nameBase))
                meetsZeroValueCondition && meetsManualHideCondition
            }
            .onHover { drawContext, textRenderer ->
                val scaleFactor = mc.window.scaleFactor
                val mouseX = mc.mouse.x / scaleFactor
                val mouseY = mc.mouse.y / scaleFactor

                RenderUtils2D.drawHoveringString(drawContext,
                    "$YELLOW${Helper.toTitleCase(nameBase.replace("_", " ").lowercase())} LS Details:\n" +
                            formattedTextLS,
                    mouseX, mouseY, textRenderer, overlay.scale)
            }
        if (SBOConfigBundle.sboData.hideTrackerLines.contains(nameBase)) {
            line.text = "$GRAY$STRIKETHROUGH${combinedText.removeFormatting()}"
        }
        return line
    }

    fun updateLines(screen: String = "") {
        val lines = mutableListOf<OverlayTextLine>()
        val lsLines = mutableListOf<OverlayTextLine>()
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

        val shimmPercent = calcPercentOne(tracker.items, tracker.mobs, "SHIMMERING_WOOL", "KING_MINOS")
        val shimmLsPercent = calcPercentOne(tracker.items, tracker.mobs, "SHIMMERING_WOOL_LS", "KING_MINOS_LS")
        val mantiPercent = calcPercentOne(tracker.items, tracker.mobs, "MANT_CORE", "MANTICORE")
        val mantiLsPercent = calcPercentOne(tracker.items, tracker.mobs, "MANT_CORE_LS", "MANTICORE_LS")
        val stingerPercent = calcPercentOne(tracker.items, tracker.mobs, "FABLED_STINGER", "MANTICORE")
        val stingerLsPercent = calcPercentOne(tracker.items, tracker.mobs, "FABLED_STINGER_LS", "MANTICORE_LS")
        val chimPercent = calcPercentOne(tracker.items, tracker.mobs, "CHIMERA", "MINOS_INQUISITOR")
        val chimLsPercent = calcPercentOne(tracker.items, tracker.mobs, "CHIMERA_LS", "MINOS_INQUISITOR_LS")
        val brainPercent = calcPercentOne(tracker.items, tracker.mobs, "BRAIN_FOOD", "SPHINX")
        val brainLsPercent = calcPercentOne(tracker.items, tracker.mobs, "BRAIN_FOOD_LS", "SPHINX_LS")
        val relicPercent = calcPercentOne(tracker.items, tracker.mobs, "MINOS_RELIC", "MINOS_CHAMPION")
        val stickPercent = calcPercentOne(tracker.items, tracker.mobs, "DAEDALUS_STICK", "MINOTAUR")
        val playTimeHrs = tracker.items.TIME.toDouble() / TimeUnit.HOURS.toMillis(1)
        val burrowsPerHr = Helper.getBurrowsPerHr(tracker, timer)
        val bphText = if (burrowsPerHr.isNaN() || burrowsPerHr == 0.0) {
            ""
        } else {
            " $GRAY[$AQUA$burrowsPerHr$GRAY/${AQUA}hr$GRAY]"
        }
        val fabledStingerPrice = Helper.getItemPriceFormatted("FABLED_STINGER", tracker.items.FABLED_STINGER)
        val fabledStingerLsPrice = Helper.getItemPriceFormatted("FABLED_STINGER", tracker.items.FABLED_STINGER_LS)
        val shimmPrice = Helper.getItemPriceFormatted("SHIMMERING_WOOL", tracker.items.SHIMMERING_WOOL)
        val shimmLsPrice = Helper.getItemPriceFormatted("SHIMMERING_WOOL", tracker.items.SHIMMERING_WOOL_LS)
        val mantiPrice = Helper.getItemPriceFormatted("MANTI_CORE", tracker.items.MANTI_CORE)
        val mantiLsPrice = Helper.getItemPriceFormatted("MANTI_CORE", tracker.items.MANTI_CORE_LS)
        val chimPrice = Helper.getItemPriceFormatted("CHIMERA", tracker.items.CHIMERA)
        val chimLsPrice = Helper.getItemPriceFormatted("CHIMERA", tracker.items.CHIMERA_LS)
        val brainPrice = Helper.getItemPriceFormatted("BRAIN_FOOD", tracker.items.BRAIN_FOOD)
        val brainLsPrice = Helper.getItemPriceFormatted("BRAIN_FOOD", tracker.items.BRAIN_FOOD_LS)
        val relicPrice = Helper.getItemPriceFormatted("MINOS_RELIC", tracker.items.MINOS_RELIC)
        val braidedPrice = Helper.getItemPriceFormatted("BRAIDED_GRIFFIN_FEATHER", tracker.items.BRAIDED_GRIFFIN_FEATHER)
        val stickPrice = Helper.getItemPriceFormatted("DAEDALUS_STICK", tracker.items.DAEDALUS_STICK)
        val crownPrice = Helper.getItemPriceFormatted("CROWN_OF_GREED", tracker.items.CROWN_OF_GREED)
        val sovenirPrice = Helper.getItemPriceFormatted("WASHED_UP_SOUVENIR", tracker.items.WASHED_UP_SOUVENIR)
        val mythoFragPrice = Helper.getItemPriceFormatted("MYTHOS_FRAGMENT", tracker.items.MYTHOS_FRAGMENT)
        val featherPrice = Helper.getItemPriceFormatted("GRIFFIN_FEATHER", tracker.items.GRIFFIN_FEATHER)
        val shelmetPrice = Helper.getItemPriceFormatted("DWARF_TURTLE_SHELMET", tracker.items.DWARF_TURTLE_SHELMET)
        val plushiePrice = Helper.getItemPriceFormatted("CROCHET_TIGER_PLUSHIE", tracker.items.CROCHET_TIGER_PLUSHIE)
        val remediesPrice = Helper.getItemPriceFormatted("ANTIQUE_REMEDIES", tracker.items.ANTIQUE_REMEDIES)
        val hiltPrice = Helper.getItemPriceFormatted("HILT_OF_REVELATIONS", tracker.items.HILT_OF_REVELATIONS)
        val clawPrice= Helper.getItemPriceFormatted("ANCIENT_CLAW", tracker.items.ANCIENT_CLAW)
        val echClawPrice = Helper.getItemPriceFormatted("ENCHANTED_ANCIENT_CLAW", tracker.items.ENCHANTED_ANCIENT_CLAW)
        val echGoldPrice = Helper.getItemPriceFormatted("ENCHANTED_GOLD", tracker.items.ENCHANTED_GOLD)
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

        if (Diana.combineLootLines) {
            val shimmCombinedPrice = Helper.getItemPriceFormatted("SHIMMERING_WOOL", tracker.items.SHIMMERING_WOOL + tracker.items.SHIMMERING_WOOL_LS)
            val mantiCombinedPrice = Helper.getItemPriceFormatted("MANTI_CORE", tracker.items.MANTI_CORE + tracker.items.MANTI_CORE_LS)
            val stingerCombinedPrice = Helper.getItemPriceFormatted("FABLED_STINGER", tracker.items.FABLED_STINGER + tracker.items.FABLED_STINGER_LS)
            val chimCombinedPrice = Helper.getItemPriceFormatted("CHIMERA", tracker.items.CHIMERA + tracker.items.CHIMERA_LS)
            val brainCombinedPrice = Helper.getItemPriceFormatted("BRAIN_FOOD", tracker.items.BRAIN_FOOD + tracker.items.BRAIN_FOOD_LS)
            lsLines.addAll(
                listOf(
                    createCombinedLine("SHIMMERING_WOOL", "$GOLD$shimmCombinedPrice $GRAY|$RED Shimmering Wool: $AQUA${Helper.formatNumber(tracker.items.SHIMMERING_WOOL, true)}", tracker.items.SHIMMERING_WOOL, "$GOLD$shimmLsPrice $GRAY|$RED Shimmering Wool $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.SHIMMERING_WOOL_LS, true)} $GRAY($AQUA${shimmLsPercent}%$GRAY)", tracker.items.SHIMMERING_WOOL_LS),
                    createCombinedLine("MANTI_CORE", "$GOLD$mantiCombinedPrice $GRAY|$RED Manti-core: $AQUA${Helper.formatNumber(tracker.items.MANTI_CORE, true)}", tracker.items.MANTI_CORE, "$GOLD$mantiLsPrice $GRAY|$RED Manti-core $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.MANTI_CORE_LS, true)} $GRAY($AQUA${mantiLsPercent}%$GRAY)", tracker.items.MANTI_CORE_LS),
                    createCombinedLine("FABLED_STINGER", "$GOLD$stingerCombinedPrice $GRAY|$LIGHT_PURPLE Fabled Stinger: $AQUA${Helper.formatNumber(tracker.items.FABLED_STINGER, true)}", tracker.items.FABLED_STINGER, "$GOLD$fabledStingerLsPrice $GRAY|$LIGHT_PURPLE Fabled Stinger $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.FABLED_STINGER_LS, true)} $GRAY($AQUA${stingerLsPercent}%$GRAY)", tracker.items.FABLED_STINGER_LS),
                    createCombinedLine("CHIMERA", "$GOLD$chimCombinedPrice $GRAY|$LIGHT_PURPLE Chimera: $AQUA${Helper.formatNumber(tracker.items.CHIMERA, true)}", tracker.items.CHIMERA, "$GOLD$chimLsPrice $GRAY|$LIGHT_PURPLE Chimera $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.CHIMERA_LS, true)} $GRAY($AQUA${chimLsPercent}%$GRAY)", tracker.items.CHIMERA_LS),
                    createCombinedLine("BRAIN_FOOD", "$GOLD$brainCombinedPrice $GRAY|$DARK_PURPLE Brain Food: $AQUA${Helper.formatNumber(tracker.items.BRAIN_FOOD, true)}", tracker.items.BRAIN_FOOD, "$GOLD$brainLsPrice $GRAY|$DARK_PURPLE Brain Food $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.BRAIN_FOOD_LS, true)} $GRAY($AQUA${brainLsPercent}%$GRAY)", tracker.items.BRAIN_FOOD_LS)
                )
            )
        }
        else {
            lsLines.addAll(
                listOf(
                    createLine("SHIMMERING_WOOL", "$GOLD$shimmPrice $GRAY|$RED Shimmering Wool: $AQUA${Helper.formatNumber(tracker.items.SHIMMERING_WOOL, true)} $GRAY($AQUA${shimmPercent}%$GRAY)", tracker.items.SHIMMERING_WOOL),
                    createLine("SHIMMERING_WOOL_LS", "$GOLD$shimmLsPrice $GRAY|$RED Shimmering Wool $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.SHIMMERING_WOOL_LS, true)} $GRAY($AQUA${shimmLsPercent}%$GRAY)", tracker.items.SHIMMERING_WOOL_LS),
                    createLine("MANTI_CORE", "$GOLD$mantiPrice $GRAY|$RED Manti-core: $AQUA${Helper.formatNumber(tracker.items.MANTI_CORE, true)} $GRAY($AQUA${mantiPercent}%$GRAY)", tracker.items.MANTI_CORE),
                    createLine("MANTI_CORE_LS", "$GOLD$mantiLsPrice $GRAY|$RED Manti-core $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.MANTI_CORE_LS, true)} $GRAY($AQUA${mantiLsPercent}%$GRAY)", tracker.items.MANTI_CORE_LS),
                    createLine("FABLED_STINGER", "$GOLD$fabledStingerPrice $GRAY|$LIGHT_PURPLE Fabled Stinger: $AQUA${Helper.formatNumber(tracker.items.FABLED_STINGER, true)} $GRAY($AQUA${stingerPercent}%$GRAY)", tracker.items.FABLED_STINGER),
                    createLine("FABLED_STINGER_LS", "$GOLD$fabledStingerLsPrice $GRAY|$LIGHT_PURPLE Fabled Stinger $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.FABLED_STINGER_LS, true)} $GRAY($AQUA${stingerLsPercent}%$GRAY)", tracker.items.FABLED_STINGER_LS),
                    createLine("CHIMERA", "$GOLD$chimPrice $GRAY|$LIGHT_PURPLE Chimera: $AQUA${Helper.formatNumber(tracker.items.CHIMERA, true)} $GRAY($AQUA${chimPercent}%$GRAY)", tracker.items.CHIMERA),
                    createLine("CHIMERA_LS", "$GOLD$chimLsPrice $GRAY|$LIGHT_PURPLE Chimera $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.CHIMERA_LS, true)} $GRAY($AQUA${chimLsPercent}%$GRAY)", tracker.items.CHIMERA_LS),
                    createLine("BRAIN_FOOD", "$GOLD$brainPrice $GRAY|$DARK_PURPLE Brain Food: $AQUA${Helper.formatNumber(tracker.items.BRAIN_FOOD, true)} $GRAY($AQUA${brainPercent}%$GRAY)", tracker.items.BRAIN_FOOD),
                    createLine("BRAIN_FOOD_LS", "$GOLD$brainLsPrice $GRAY|$DARK_PURPLE Brain Food $GRAY[${AQUA}LS$GRAY]: $AQUA${Helper.formatNumber(tracker.items.BRAIN_FOOD_LS, true)} $GRAY($AQUA${brainLsPercent}%$GRAY)", tracker.items.BRAIN_FOOD_LS),
                )
            )
        }

        lines.addAll(lsLines)
        lines.addAll(
            listOf(
                createLine("MINOS_RELIC", "$GOLD$relicPrice $GRAY|$DARK_PURPLE Minos Relic: $AQUA${Helper.formatNumber(tracker.items.MINOS_RELIC, true)} $GRAY($AQUA${relicPercent}%$GRAY)", tracker.items.MINOS_RELIC),
                createLine("BRAIDED_GRIFFIN_FEATHER", "$GOLD$braidedPrice $GRAY|$DARK_PURPLE Braided Griffin Feather: $AQUA${Helper.formatNumber(tracker.items.BRAIDED_GRIFFIN_FEATHER, true)}", tracker.items.BRAIDED_GRIFFIN_FEATHER),
                createLine("DAEDALUS_STICK", "$GOLD$stickPrice $GRAY|$GOLD Daedalus Stick: $AQUA${Helper.formatNumber(tracker.items.DAEDALUS_STICK, true)} $GRAY($AQUA${stickPercent}%$GRAY)", tracker.items.DAEDALUS_STICK),
                createLine("CROWN_OF_GREED", "$GOLD$crownPrice $GRAY|$GOLD Crown of Greed: $AQUA${Helper.formatNumber(tracker.items.CROWN_OF_GREED, true)}", tracker.items.CROWN_OF_GREED),
                createLine("WASHED_UP_SOUVENIR", "$GOLD$sovenirPrice $GRAY|$GOLD Washed-up Souvenir: $AQUA${Helper.formatNumber(tracker.items.WASHED_UP_SOUVENIR, true)}", tracker.items.WASHED_UP_SOUVENIR),
                createLine("GRIFFIN_FEATHER", "$GOLD$featherPrice $GRAY|$GOLD Griffin Feather: $AQUA${Helper.formatNumber(tracker.items.GRIFFIN_FEATHER, true)}", tracker.items.GRIFFIN_FEATHER),
                createLine("MYTHOS_FRAGMENT", "$GOLD$mythoFragPrice $GRAY|$GOLD Mytho Fragment: $AQUA${Helper.formatNumber(tracker.items.MYTHOS_FRAGMENT, true)}", tracker.items.MYTHOS_FRAGMENT),
                createLine("CRETAN_URN", "$GOLD$crownPrice $GRAY|$DARK_GREEN Cretan Urn: $AQUA${Helper.formatNumber(tracker.items.CRETAN_URN, true)}", tracker.items.CRETAN_URN),
                createLine("DWARF_TURTLE_SHELMET", "$GOLD$shelmetPrice $GRAY|$DARK_GREEN Dwarf Turtle Helmet: $AQUA${Helper.formatNumber(tracker.items.DWARF_TURTLE_SHELMET, true)}", tracker.items.DWARF_TURTLE_SHELMET),
                createLine("CROCHET_TIGER_PLUSHIE", "$GOLD$plushiePrice $GRAY|$DARK_GREEN Crochet Tiger Plushie: $AQUA${Helper.formatNumber(tracker.items.CROCHET_TIGER_PLUSHIE, true)}", tracker.items.CROCHET_TIGER_PLUSHIE),
                createLine("ANTIQUE_REMEDIES", "$GOLD$remediesPrice $GRAY|$DARK_GREEN Antique Remedies: $AQUA${Helper.formatNumber(tracker.items.ANTIQUE_REMEDIES, true)}", tracker.items.ANTIQUE_REMEDIES),
                createLine("HILT_OF_REVELATIONS", "$GOLD$hiltPrice $GRAY|$BLUE Hilt of Revelations: $AQUA${Helper.formatNumber(tracker.items.HILT_OF_REVELATIONS)}", tracker.items.HILT_OF_REVELATIONS),
                createLine("ANCIENT_CLAW", "$GOLD$clawPrice $GRAY|$BLUE Ancient Claw: $AQUA${Helper.formatNumber(tracker.items.ANCIENT_CLAW)}", tracker.items.ANCIENT_CLAW),
                createLine("ENCHANTED_ANCIENT_CLAW", "$GOLD$echClawPrice $GRAY|$BLUE Enchanted Ancient Claw: $AQUA${Helper.formatNumber(tracker.items.ENCHANTED_ANCIENT_CLAW)}", tracker.items.ENCHANTED_ANCIENT_CLAW),
                createLine("ENCHANTED_GOLD", "$GOLD$echGoldPrice $GRAY|$BLUE Enchanted Gold: $AQUA${Helper.formatNumber(tracker.items.ENCHANTED_GOLD)}", tracker.items.ENCHANTED_GOLD),
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
        if ((screen == "CraftingOpen" || mc.currentScreen?.title?.string == "Crafting") && type == Diana.Tracker.SESSION) lines.add(resetSession)
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