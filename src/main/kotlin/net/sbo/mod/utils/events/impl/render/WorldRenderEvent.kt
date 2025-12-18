package net.sbo.mod.utils.events.impl.render

//#if MC >= 1.21.9
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
//#else
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
//#endif

class WorldRenderEvent (
    val context: WorldRenderContext
)