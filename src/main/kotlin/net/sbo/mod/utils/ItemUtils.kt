package net.sbo.mod.utils

import gg.essential.universal.utils.toFormattedString
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.sbo.mod.utils.chat.Chat

object ItemUtils {

    fun getTimestamp(customData: NbtComponent?): Long {
        if (customData == null) return 0L
        val nbt = customData.copyNbt()
        if (!nbt.contains("timestamp")) return 0L
        return nbt.getLong("timestamp").orElse(0L)
    }

    fun getSBID(customData: NbtComponent?): String {
        if (customData == null) return ""
        val nbt = customData.copyNbt()
        if (!nbt.contains("id")) return ""
        return nbt.getString("id").orElse("")
    }

    fun getUUID(customData: NbtComponent?): String {
        if (customData == null) return ""
        val nbt = customData.copyNbt()
        if (!nbt.contains("uuid")) return ""
        return nbt.getString("uuid").orElse("")
    }

    fun getDisplayName(stack: ItemStack): String {
        return stack.name.toFormattedString()
    }

    fun getLoreList(stack: ItemStack): List<String> {
        val linesList: List<Text> = stack.get(DataComponentTypes.LORE)?.lines ?: listOf()
        return linesList.map { it.toFormattedString() }
    }
}