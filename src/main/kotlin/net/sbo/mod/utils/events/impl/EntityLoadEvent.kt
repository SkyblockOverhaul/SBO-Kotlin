package net.sbo.mod.utils.events.impl

import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity

class EntityLoadEvent(val entity: Entity, val world: ClientWorld)