package net.sbo.mod.utils.events.impl.entity

import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity

/**
 * Event fired when an entity is unloaded from the world.
 * @param entity The entity that is being unloaded.
 * @param world The world from which the entity is being unloaded.
 */
class EntityUnloadEvent(val entity: Entity, val world: ClientWorld)