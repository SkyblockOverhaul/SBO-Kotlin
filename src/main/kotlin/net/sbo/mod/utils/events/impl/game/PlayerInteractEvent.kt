package net.sbo.mod.utils.events.impl.game

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.world.World
import net.minecraft.util.math.BlockPos

/**
 * Called when the player interacts with the world.
 * @param action The action performed (e.g., "RIGHT_CLICK", "LEFT_CLICK").
 * @param pos The position of the block interacted with, can be null.
 * @param player The player performing the action.
 * @param world The world the action is performed in.
 * @param isCanceled Whether the event is canceled. Can be modified by event listeners.
 */
class PlayerInteractEvent(
    val action: String,
    val pos: BlockPos?,
    val player: ClientPlayerEntity,
    val world: World,
    var isCanceled: Boolean = false
)
