package net.sbo.mod.compat

import net.irisshaders.iris.api.v0.IrisApi
import net.irisshaders.iris.api.v0.IrisProgram
import net.sbo.mod.utils.render.SboRenderPipelines

private typealias Pipelines = SboRenderPipelines

object IrisCompatibility {

    fun init() {
        IrisApi.getInstance().apply {
            assignPipeline(Pipelines.FILLED_BOX_THROUGH_WALLS, IrisProgram.BASIC)
            assignPipeline(Pipelines.LINES, IrisProgram.LINES)
            assignPipeline(Pipelines.LINES_THROUGH_WALLS, IrisProgram.LINES)
            assignPipeline(Pipelines.BEACON_BEAM_OPAQUE, IrisProgram.BEACON_BEAM)
            assignPipeline(Pipelines.BEACON_BEAM_OPAQUE_THROUGH_WALLS, IrisProgram.BEACON_BEAM)
            assignPipeline(Pipelines.BEACON_BEAM_TRANSLUCENT, IrisProgram.BEACON_BEAM)
            assignPipeline(Pipelines.BEACON_BEAM_TRANSLUCENT_THROUGH_WALLS, IrisProgram.BEACON_BEAM)
        }
    }

}