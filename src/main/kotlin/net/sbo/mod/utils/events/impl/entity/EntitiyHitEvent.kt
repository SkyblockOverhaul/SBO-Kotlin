package net.sbo.mod.utils.events.impl.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.world.World
import net.minecraft.util.hit.EntityHitResult

/**
 * Called when a player hits an entity.
 * @param player The player who hit the entity.
 * @param world The world where the entity is located.
 * @param hand The hand used to hit the entity.
 * @param entity The entity that was hit.
 * @param hitResult The result of the hit.
 */
class EntitiyHitEvent (
    val player: PlayerEntity,
    val world: World,
    val hand: Hand,
    val entity: Entity,
    val hitResult: EntityHitResult?
)