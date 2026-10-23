/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package kr.syeyoung.dungeonsguide.mod.render.world;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Through-wall versions of vanilla's world render types (D81): 1.8.9 drew these with the depth test off.
 * Depth tested draws use vanilla's render types as they are.
 */
public class DGRenderTypes {
    private static final DepthStencilState NO_DEPTH = new DepthStencilState(CompareOp.ALWAYS_PASS, false);

    private static final RenderPipeline FILLED_SEE_THROUGH_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/filled_see_through"))
            .withCull(false)
            .withDepthStencilState(NO_DEPTH)
            .build();
    private static final RenderPipeline LINES_SEE_THROUGH_PIPELINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/lines_see_through"))
            .withDepthStencilState(NO_DEPTH)
            .build();
    private static final RenderPipeline BEACON_BEAM_SEE_THROUGH_PIPELINE = RenderPipeline.builder(RenderPipelines.BEACON_BEAM_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/beacon_beam_see_through"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(NO_DEPTH)
            .build();

    private static final RenderType FILLED_SEE_THROUGH = RenderType.create("dungeonsguide_filled_see_through",
            RenderSetup.builder(FILLED_SEE_THROUGH_PIPELINE).sortOnUpload().createRenderSetup());
    private static final RenderType LINES_SEE_THROUGH = RenderType.create("dungeonsguide_lines_see_through",
            RenderSetup.builder(LINES_SEE_THROUGH_PIPELINE).createRenderSetup());
    private static final RenderType BEACON_BEAM_SEE_THROUGH = RenderType.create("dungeonsguide_beacon_beam_see_through",
            RenderSetup.builder(BEACON_BEAM_SEE_THROUGH_PIPELINE).withTexture("Sampler0", BeaconRenderer.BEAM_LOCATION).sortOnUpload().createRenderSetup());

    // DG gui images are premultiplied (see StencilImage)
    private static final RenderPipeline IMAGE_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/image"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .build();
    private static final RenderPipeline IMAGE_SEE_THROUGH_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/image_see_through"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA))
            .withCull(false)
            .withDepthStencilState(NO_DEPTH)
            .build();
    // plain (straight alpha) textures
    private static final RenderPipeline TEXTURED_QUAD_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/textured_quad"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .build();
    private static final RenderPipeline TEXTURED_QUAD_SEE_THROUGH_PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("dungeonsguide", "pipeline/textured_quad_see_through"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(NO_DEPTH)
            .build();
    private static final Map<Identifier, RenderType> IMAGE = new HashMap<>();
    private static final Map<Identifier, RenderType> IMAGE_SEE_THROUGH = new HashMap<>();
    private static final Map<Identifier, RenderType> TEXTURED_QUAD = new HashMap<>();
    private static final Map<Identifier, RenderType> TEXTURED_QUAD_SEE_THROUGH = new HashMap<>();

    public static RenderType filledSeeThrough() {
        return FILLED_SEE_THROUGH;
    }

    public static RenderType linesSeeThrough() {
        return LINES_SEE_THROUGH;
    }

    public static RenderType beaconBeamSeeThrough() {
        return BEACON_BEAM_SEE_THROUGH;
    }

    public static RenderType image(Identifier texture) {
        return IMAGE.computeIfAbsent(texture, t -> RenderType.create("dungeonsguide_image",
                RenderSetup.builder(IMAGE_PIPELINE).withTexture("Sampler0", t).sortOnUpload().createRenderSetup()));
    }

    public static RenderType imageSeeThrough(Identifier texture) {
        return IMAGE_SEE_THROUGH.computeIfAbsent(texture, t -> RenderType.create("dungeonsguide_image_see_through",
                RenderSetup.builder(IMAGE_SEE_THROUGH_PIPELINE).withTexture("Sampler0", t).sortOnUpload().createRenderSetup()));
    }

    public static RenderType texturedQuad(Identifier texture) {
        return TEXTURED_QUAD.computeIfAbsent(texture, t -> RenderType.create("dungeonsguide_textured_quad",
                RenderSetup.builder(TEXTURED_QUAD_PIPELINE).withTexture("Sampler0", t).sortOnUpload().createRenderSetup()));
    }

    public static RenderType texturedQuadSeeThrough(Identifier texture) {
        return TEXTURED_QUAD_SEE_THROUGH.computeIfAbsent(texture, t -> RenderType.create("dungeonsguide_textured_quad_see_through",
                RenderSetup.builder(TEXTURED_QUAD_SEE_THROUGH_PIPELINE).withTexture("Sampler0", t).sortOnUpload().createRenderSetup()));
    }
}
