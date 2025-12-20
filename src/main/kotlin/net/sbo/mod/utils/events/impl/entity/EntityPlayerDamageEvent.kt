package net.sbo.mod.utils.events.impl.entity

import net.minecraft.entity.player.PlayerEntity

/**
 * Event triggered when a player entity takes damage.
 *
 * @param entityId The unique identifier of the entity.
 * @param entity The player entity that took damage.
 * @param name The name of the player entity.
 */
class EntityPlayerDamageEvent(val entityId: Int, val entity: PlayerEntity, val name: String, val status: Byte)