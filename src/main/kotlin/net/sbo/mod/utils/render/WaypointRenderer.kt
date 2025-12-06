package net.sbo.mod.utils.render

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.sbo.mod.utils.waypoint.WaypointManager

object WaypointRenderer : WorldRenderEvents.AfterTranslucent {
    override fun afterTranslucent(context: WorldRenderContext) {
        WaypointManager.renderAllWaypoints(context)
    }
}