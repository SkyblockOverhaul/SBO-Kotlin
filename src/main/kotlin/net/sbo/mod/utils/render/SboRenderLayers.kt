package net.sbo.mod.utils.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;

import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import java.util.OptionalDouble

object SboRenderPipelines {
    val FILLED_BOX_THROUGH_WALLS: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of("sbo", "pipeline/debug_filled_box_through_walls"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    )

    val LINES: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(*arrayOf<RenderPipeline.Snippet?>(RenderPipelines.RENDERTYPE_LINES_SNIPPET))
            .withLocation(Identifier.of("sbo", "pipeline/line_strip"))
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, DrawMode.LINES)
            .withCull(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(true)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .build()
    )

    val LINES_THROUGH_WALLS: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(*arrayOf<RenderPipeline.Snippet?>(RenderPipelines.RENDERTYPE_LINES_SNIPPET))
            .withLocation(Identifier.of("sbo", "pipeline/line_through_walls"))
            .withShaderDefine("shad")
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, DrawMode.LINES)
            .withCull(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    )
}

object SboRenderLayers {
    @JvmField
    val FILLED_BOX: RenderLayer.MultiPhase = RenderLayer.of(
        "filled_box",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        RenderPipelines.DEBUG_FILLED_BOX,
        RenderLayer.MultiPhaseParameters.builder()
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false)
    )

    @JvmField
    val FILLED_BOX_THROUGH_WALLS: RenderLayer.MultiPhase = RenderLayer.of(
        "filled_box_through_walls",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        SboRenderPipelines.FILLED_BOX_THROUGH_WALLS,
        RenderLayer.MultiPhaseParameters.builder()
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false)
    )

    fun getLines(lineWidth: Double, throughWalls: Boolean): RenderLayer {
        return if (throughWalls) {
            RenderLayer.of(
                "lines_through_walls",
                RenderLayer.DEFAULT_BUFFER_SIZE,
                false,
                true,
                SboRenderPipelines.LINES_THROUGH_WALLS,
                RenderLayer.MultiPhaseParameters.builder()
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .lineWidth(RenderPhase.LineWidth(OptionalDouble.of(lineWidth)))
                    .build(false)
            )
        } else {
            RenderLayer.of(
                "lines",
                RenderLayer.DEFAULT_BUFFER_SIZE,
                false,
                true,
                SboRenderPipelines.LINES,
                RenderLayer.MultiPhaseParameters.builder()
                    .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                    .lineWidth(RenderPhase.LineWidth(OptionalDouble.of(lineWidth)))
                    .build(false)
            )
        }
    }
}