package net.sbo.mod.utils.data

import com.google.gson.annotations.SerializedName

interface DianaTracker {
    var items: DianaItemsData
    var mobs: DianaMobsData
    var inquis: DianaInquisData

    fun reset(): DianaTracker {
        items = DianaItemsData()
        mobs = DianaMobsData()
        inquis = DianaInquisData()
        if (this is DianaTrackerMayorData) year = 0
        return this
    }

    fun save(): DianaTracker {
        when(this) {
            is DianaTrackerTotalData -> SboDataObject.save("DianaTrackerTotalData")
            is DianaTrackerSessionData -> SboDataObject.save("DianaTrackerSessionData")
            is DianaTrackerMayorData -> SboDataObject.save("DianaTrackerMayorData")
            else -> {}
        }
        return this
    }
}

data class SboConfigBundle(
    var sboData: SboData,
    var achievementsData: AchievementsData,
    var pastDianaEventsData: PastDianaEventsData,
    var dianaTrackerTotalData: DianaTrackerTotalData,
    var dianaTrackerSessionData: DianaTrackerSessionData,
    var dianaTrackerMayorData: DianaTrackerMayorData,
    var partyFinderConfigState: PartyFinderConfigState,
    var partyFinderData: PartyFinderData,
    var overlayData: OverlayData
)

// ------ Data Classes ------

data class Effect(
    var name: String,
    var duration: Double,
    var timeStamp: Long,
    var loggedOff: Boolean
)

// ------ Main Data Class ------

data class SboData(
    var effects: List<Effect> = emptyList(),
    var resetVersion: String = "0.1.3",
    var changelogVersion: String = "1.0.0",
    var downloadMsg: Boolean = false,
    var mobsSinceInq: Int = 0,
    var inqsSinceChim: Int = 0,
    var minotaursSinceStick: Int = 0,
    var champsSinceRelic: Int = 0,
    var inqsSinceLsChim: Int = 0,
    var highestChimMagicFind: Int = 0,
    var highestStickMagicFind: Int = 0,
    var hideTrackerLines: MutableList<String> = mutableListOf(),
    var partyBlacklist: List<String> = emptyList(),
    var achievementFilter: String = "Locked",
    var lastInqDate: Long = 0,
    var b2bStick: Boolean = false,
    var b2bChim: Boolean = false,
    var b2bChimLs: Boolean = false,
    var b2bInq: Boolean = false,
    var b2bChimLsInq: Boolean = false,
    var sboKey: String = "",
    var b2bStreakCounter: MutableMap<String, Int> = mutableMapOf(),
)

data class AchievementsData(
    var achievements: MutableMap<Int, Boolean> = mutableMapOf()
)

data class PastDianaEventsData(
    var events: List<DianaTrackerMayorData> = emptyList()
)

data class DianaTrackerTotalData(
    override var items: DianaItemsData = DianaItemsData(),
    override var mobs: DianaMobsData = DianaMobsData(),
    override var inquis: DianaInquisData = DianaInquisData(),
) : DianaTracker

data class DianaTrackerSessionData(
    override var items: DianaItemsData = DianaItemsData(),
    override var mobs: DianaMobsData = DianaMobsData(),
    override var inquis: DianaInquisData = DianaInquisData()
) : DianaTracker

data class DianaTrackerMayorData(
    var year: Int = 0,
    override var items: DianaItemsData = DianaItemsData(),
    override var mobs: DianaMobsData = DianaMobsData(),
    override var inquis: DianaInquisData = DianaInquisData()
) : DianaTracker

data class PartyFinderConfigState(
    var checkboxes: Checkboxes = Checkboxes(),
    var inputs: Inputs = Inputs(),
    var filters: Filters = Filters()
)

data class PartyFinderData(
    var playerStatsUpdated: Long = 0,
    var playerStats: Map<String, PlayerStats> = emptyMap(),
)

// ------ Diana Data ------
@Suppress("PropertyName")
data class DianaItemsData(
    @SerializedName("coins") var COINS: Long = 0,
    @SerializedName("Griffin Feather") var GRIFFIN_FEATHER: Int = 0,
    @SerializedName("Crown of Greed") var CROWN_OF_GREED: Int = 0,
    @SerializedName("Washed-up Souvenir") var WASHED_UP_SOUVENIR: Int = 0,
    @SerializedName("Chimera") var CHIMERA: Int = 0,
    @SerializedName("ChimeraLs") var CHIMERA_LS: Int = 0,
    @SerializedName("Daedalus Stick") var DAEDALUS_STICK: Int = 0,
    @SerializedName("DWARF_TURTLE_SHELMET") var DWARF_TURTLE_SHELMET: Int = 0,
    @SerializedName("ANTIQUE_REMEDIES") var ANTIQUE_REMEDIES: Int = 0,
    @SerializedName("CROCHET_TIGER_PLUSHIE") var CROCHET_TIGER_PLUSHIE: Int = 0,
    @SerializedName("ENCHANTED_ANCIENT_CLAW") var ENCHANTED_ANCIENT_CLAW: Int = 0,
    @SerializedName("ANCIENT_CLAW") var ANCIENT_CLAW: Int = 0,
    @SerializedName("MINOS_RELIC") var MINOS_RELIC: Int = 0,
    @SerializedName("ENCHANTED_GOLD") var ENCHANTED_GOLD: Int = 0,
    @SerializedName("ENCHANTED_IRON") var ENCHANTED_IRON: Int = 0,
    @SerializedName("Total Burrows") var TOTAL_BURROWS: Int = 0,
    @SerializedName("scavengerCoins") var SCAVENGER_COINS: Long  = 0,
    @SerializedName("fishCoins") var FISH_COINS: Long  = 0,
    @SerializedName("time") var TIME: Long = 0
)

@Suppress("PropertyName")
data class DianaMobsData(
    @SerializedName("Minos Inquisitor") var MINOS_INQUISITOR: Int = 0,
    @SerializedName("Minos Champion") var MINOS_CHAMPION: Int = 0,
    @SerializedName("Minotaur") var MINOTAUR: Int = 0,
    @SerializedName("Gaia Construct") var GAIA_CONSTRUCT: Int = 0,
    @SerializedName("Siamese Lynxes") var SIAMESE_LYNXES: Int = 0,
    @SerializedName("Minos Hunter") var MINOS_HUNTER: Int = 0,
    @SerializedName("TotalMobs") var TOTAL_MOBS: Int = 0,
    @SerializedName("Minos Inquisitor Ls") var MINOS_INQUISITOR_LS: Int = 0
)

@Suppress("PropertyName")
data class DianaInquisData(
    var DWARF_TURTLE_SHELMET: Int = 0,
    var CROCHET_TIGER_PLUSHIE: Int = 0,
    var ANTIQUE_REMEDIES: Int = 0,
    var DWARF_TURTLE_SHELMET_LS: Int = 0,
    var CROCHET_TIGER_PLUSHIE_LS: Int = 0,
    var ANTIQUE_REMEDIES_LS: Int = 0
)

// ------ Party Finder ------

data class Checkboxes(
    var custom: CustomCheckboxes = CustomCheckboxes(),
    var diana: DianaCheckboxes = DianaCheckboxes()
)

data class CustomCheckboxes(
    var eman9: Boolean = false
)

data class DianaCheckboxes(
    var eman9: Boolean = false,
    var looting5: Boolean = false
)

data class Inputs(
    var custom: CustomInputs = CustomInputs(),
    var diana: DianaInputs = DianaInputs()
)

data class CustomInputs(
    var lvl: Int = 0,
    var mp: Int = 0,
    var partySize: Int = 0,
    var note: String = "..."
)

data class DianaInputs(
    var kills: Int = 0,
    var lvl: Int = 0,
    var note: String = "..."
)

data class Filters(
    var custom: CustomFilters = CustomFilters(),
    var diana: DianaFilters = DianaFilters()
)

data class CustomFilters(
    var eman9Filter: Boolean = false,
    var noteFilter: String = ".",
    var canIjoinFilter: Boolean = false
)

data class DianaFilters(
    var eman9Filter: Boolean = false,
    var looting5Filter: Boolean = false,
    var canIjoinFilter: Boolean = false
)

data class PlayerStats(
    var name: String = "",
    var sbLvl: Int = 0,
    var eman9: Boolean = false,
    var looting5daxe: Boolean = false,
    var emanLvl: Int = 0,
    var warnings: List<String> = emptyList(),
    var uuid: String = "",
    var clover: Boolean = false,
    var daxeLootingLvl: Int = 0,
    var daxeChimLvl: Int = 0,
    var invApi: Boolean = false,
    var magicalPower: Int = 0,
    var enrichments: Int = 0,
    var missingEnrichments: Int = 0,
    var griffinRarity: String = "",
    var griffinItem: String? = null,
    var killLeaderboard: Int = 0,
    var mythosKills: Long = 0L
)

data class OverlayData(
    var overlays: MutableMap<String, OverlayValues> = mutableMapOf()
)

data class OverlayValues(
    var x: Float = 0f,
    var y: Float = 0f,
    var scale: Float = 1.0f,
)