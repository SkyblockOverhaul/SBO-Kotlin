package net.sbo.mod.utils.events.impl.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack

class PlayerMeleeAttackEvent (
    val attacker: PlayerEntity,
    val target: Entity,
    val item: ItemStack
)