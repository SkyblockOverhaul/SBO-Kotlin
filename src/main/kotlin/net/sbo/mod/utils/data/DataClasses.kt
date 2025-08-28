package net.sbo.mod.utils.data

import gg.essential.elementa.UIComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.Packet
import net.minecraft.world.World

@Serializable
data class GetAllParties(
    @SerialName("Success")
    val success: Boolean,

    @SerialName("Parties")
    val parties: List<Party>
)

@Serializable
data class PartyInfo(
    @SerialName("Success")
    val success: Boolean,

    @SerialName("PartyInfo")
    val partyInfo: List<PartyPlayerStats>
)

@Serializable
data class PartyAddResponse(
    @SerialName("Success")
    val success: Boolean,

    @SerialName("Message")
    val message: String? = null,

    @SerialName("PartyInfo")
    val partyInfo: List<PartyPlayerStats>? = null,

    @SerialName("PartyReqs")
    val partyReqs: Reqs? = null,

    @SerialName("Error")
    val error: String? = null
)

@Serializable
data class PartyUpdateResponse(
    @SerialName("Success")
    val success: Boolean,

    @SerialName("Message")
    val message: String? = null,

    @SerialName("PartyReqs")
    val partyReqs: Reqs? = null,

    @SerialName("Error")
    val error: String? = null
)

@Serializable
data class HypixelBazaarResponse(
    val success: Boolean,
    val lastUpdated: Long,
    val products: Map<String, Product>
)

@Suppress("PropertyName")
@Serializable
data class Product(
    val product_id: String,
    val sell_summary: List<SummaryItem>,
    val buy_summary: List<SummaryItem>,
    val quick_status: QuickStatus
)

@Serializable
data class SummaryItem(
    val amount: Int,
    val pricePerUnit: Double,
    val orders: Int
)

@Serializable
data class QuickStatus(
    val productId: String,
    val sellPrice: Double,
    val sellVolume: Int,
    val sellMovingWeek: Int,
    val sellOrders: Int,
    val buyPrice: Double,
    val buyVolume: Int,
    val buyMovingWeek: Int,
    val buyOrders: Int
)

@Serializable
data class Party(
    @SerialName("partyinfo")
    val partyInfo: List<PartyPlayerStats>,
    val reqs: Reqs,
    val leader: String,
    @SerialName("partymembers")
    val partyMembersCount: Int,
    val leaderName: String,
    val note: String,
    val partySize: Int
)


@Serializable
data class PartyPlayerStats(
    val name: String = "",
    val sbLvl: Int = -1,
    val eman9: Boolean = false,
    val looting5daxe: Boolean = false,
    val emanLvl: Int = 0,
    val warnings: List<String> = emptyList(),
    val uuid: String = "",
    val clover: Boolean = false,
    val daxeLootingLvl: Int = 0,
    val daxeChimLvl: Int = 0,
    val invApi: Boolean = false,
    val magicalPower: Int = 0,
    val enrichments: Int = 0,
    val missingEnrichments: Int = 0,
    val griffinRarity: String = "",
    val griffinItem: JsonPrimitive? = null,
    val killLeaderboard: Int = 999999,
    val mythosKills: Int = 0
)

@Serializable
data class Reqs(
    val lvl: Int = -1,
    val kills: Int = 0,
    val eman9: Boolean = false,
    val looting5: Boolean = false,
    val mp: Int = 0
)

@Serializable
data class MayorResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("lastUpdated") val lastUpdated: Long,
    @SerialName("mayor") val mayor: MayorData,
    @SerialName("current") val current: ElectionData? = null,
    @SerialName("error") val error: String? = null
)

@Serializable
data class MayorData(
    @SerialName("key") val key: String,
    @SerialName("name") val name: String,
    @SerialName("perks") val perks: List<PerkData>,
    @SerialName("minister") val minister: MinisterData? = null,
    @SerialName("election") val election: ElectionData
)

@Serializable
data class PerkData(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String? = null,
    @SerialName("minister") val minister: Boolean? = null
)

@Serializable
data class MinisterData(
    @SerialName("key") val key: String,
    @SerialName("name") val name: String,
    @SerialName("perk") val perk: PerkData
)

@Serializable
data class ElectionData(
    @SerialName("year") val year: Int,
    @SerialName("candidates") val candidates: List<CandidateData>
)

@Serializable
data class CandidateData(
    @SerialName("key") val key: String,
    @SerialName("name") val name: String,
    @SerialName("perks") val perks: List<PerkData>,
    @SerialName("votes") val votes: Int? = null
)

data class HighlightElement(
    val page: String,
    val obj: UIComponent,
    val type: String
)

data class PlayerInteractEvent(
    val player: PlayerEntity,
    val world: World,
    var isCanceled: Boolean = false
)

data class PacketActionPair<T : Packet<*>>(
    val packetClass: Class<T>?, // Made nullable
    val action: (packet: T) -> Unit
)

data class Item(
    val itemId: String,
    val itemUUID: String,
    val name: String,
    val creation: Long,
    var count: Int
)