package net.sbo.mod.utils.render

import net.sbo.mod.utils.waypoint.WaypointManager

//#if MC > 1.21.9
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
//$$ import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
//$$
//$$ object WaypointRenderer : WorldRenderEvents.BeforeTranslucent {
//$$     override fun beforeTranslucent(context: WorldRenderContext) {
//$$         WaypointManager.renderAllWaypoints(context)
//$$     }
//$$ }
//#else
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object WaypointRenderer : WorldRenderEvents.AfterTranslucent {
    override fun afterTranslucent(context: WorldRenderContext) {
        WaypointManager.renderAllWaypoints(context)
    }
}
//#endif