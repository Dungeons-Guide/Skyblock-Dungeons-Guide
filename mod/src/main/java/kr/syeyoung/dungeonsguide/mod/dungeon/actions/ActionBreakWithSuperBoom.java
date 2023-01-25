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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionBreakWithSuperBoom extends AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<AbstractAction>();
    private OffsetPoint target;

    @Override
    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getBlock(dungeonRoom) == Blocks.air;
    }

    public ActionBreakWithSuperBoom(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        BlockPos blockpos = target.getBlockPos(dungeonRoom);

        Entity viewing_from = Minecraft.getMinecraft().getRenderViewEntity();

        double x_fix = viewing_from.lastTickPosX + ((viewing_from.posX - viewing_from.lastTickPosX) * partialTicks);
        double y_fix = viewing_from.lastTickPosY + ((viewing_from.posY - viewing_from.lastTickPosY) * partialTicks);
        double z_fix = viewing_from.lastTickPosZ + ((viewing_from.posZ - viewing_from.lastTickPosZ) * partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x_fix, -y_fix, -z_fix);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
        vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);

        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().theWorld,
                blockrendererdispatcher.getBlockModelShapes().getModelForState(Blocks.tnt.getDefaultState()),
                Blocks.tnt.getDefaultState(), blockpos, vertexBuffer, false);
        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        RenderUtils.highlightBlock(blockpos, new Color(0, 255,255,50), partialTicks, true);
        RenderUtils.drawTextAtWorld("Superboom", blockpos.getX() + 0.5f, blockpos.getY() + 0.5f, blockpos.getZ() + 0.5f, 0xFFFFFF00, 0.03f, false, false, partialTicks);
    }

    @Override
    public String toString() {
        return "BreakWithSuperboom\n- target: "+target.toString();
    }
}
