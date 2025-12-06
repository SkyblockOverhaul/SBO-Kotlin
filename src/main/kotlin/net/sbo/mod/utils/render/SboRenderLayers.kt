package net.sbo.mod.utils.render

import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.util.TriState
import java.util.OptionalDouble

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

    val BEACON_BEAM_OPAQUE: RenderLayer = RenderLayer.of(
        "beacon_beam_opaque",
        1536,
        false,
        true,
        SboRenderPipelines.BEACON_BEAM_OPAQUE,
        RenderLayer.MultiPhaseParameters.builder()
            .texture(
                RenderPhase.Texture(
                    BeaconBlockEntityRenderer.BEAM_TEXTURE,
                    //#if MC < 1.21.7
                    TriState.FALSE,
                    //#endif
                    false
                )
            )
            .build(false)
    )

    val BEACON_BEAM_OPAQUE_THROUGH_WALLS: RenderLayer = RenderLayer.of(
        "beacon_beam_opaque_through_walls",
        1536,
        false,
        true,
        SboRenderPipelines.BEACON_BEAM_OPAQUE_THROUGH_WALLS,
        RenderLayer.MultiPhaseParameters.builder()
            .texture(
                RenderPhase.Texture(
                    BeaconBlockEntityRenderer.BEAM_TEXTURE,
                    //#if MC < 1.21.7
                    TriState.FALSE,
                    //#endif
                    false
                )
            )
            .build(false)
    )

    val BEACON_BEAM_TRANSLUCENT: RenderLayer = RenderLayer.of(
        "beacon_beam_translucent",
        1536,
        false,
        true,
        SboRenderPipelines.BEACON_BEAM_TRANSLUCENT,
        RenderLayer.MultiPhaseParameters.builder()
            .texture(
                RenderPhase.Texture(
                    BeaconBlockEntityRenderer.BEAM_TEXTURE,
                    //#if MC < 1.21.7
                    TriState.FALSE,
                    //#endif
                    false
                )
            )
            .build(false)
    )

    val BEACON_BEAM_TRANSLUCENT_THROUGH_WALLS: RenderLayer = RenderLayer.of(
        "devonian_beacon_beam_translucent_esp",
        1536,
        false,
        true,
        SboRenderPipelines.BEACON_BEAM_TRANSLUCENT_THROUGH_WALLS,
        RenderLayer.MultiPhaseParameters.builder()
            .texture(
                RenderPhase.Texture(
                    BeaconBlockEntityRenderer.BEAM_TEXTURE,
                    //#if MC < 1.21.7
                    TriState.FALSE,
                    //#endif
                    false
                )
            )
            .build(false)
    )
}