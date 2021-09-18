/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.eventlistener;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.pathfinding.JPSPathfinder;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class PathfindListener {
    public JPSPathfinder jpsPathfinder;

    public static final PathfindListener INSTANCE = new PathfindListener();

    private void renderBox(Vec3 node, Color c, float partialTicks) {
        if (node == null) return;
        RenderUtils.highlightBox(AxisAlignedBB.fromBounds(node.xCoord - 0.25f, node.yCoord, node.zCoord - 0.25f, node.xCoord + 0.25f, node.yCoord + 0.5f, node.zCoord + 0.25f),c, partialTicks, false);

    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent postRender) {
        try {
            if (jpsPathfinder == null) return;
            GlStateManager.pushMatrix();
            for (JPSPathfinder.Node node : jpsPathfinder.getOpen()) {
                renderBox(new Vec3(node.getX() / 2.0, node.getY() / 2.0, node.getZ() / 2.0), new Color(255,0,0,50), postRender.partialTicks);
            }

            RenderUtils.drawLinesVec3(jpsPathfinder.getRoute(), new AColor(0, 255, 0,  255), 1, postRender.partialTicks, false);
            for (Vec3 vec3 : jpsPathfinder.getRoute()) {
                renderBox(vec3, new Color(0, 255,0, 50), postRender.partialTicks);
            }
            GlStateManager.popMatrix();
        } catch (Throwable t) {
        }
    }
}
