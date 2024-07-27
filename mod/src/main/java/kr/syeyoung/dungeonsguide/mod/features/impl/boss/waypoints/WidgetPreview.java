/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.waypoints;

import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureF7TerminalWaypoints;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.PathfindLineProperties;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.PreviewWorld;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.glu.Project;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.List;

public class WidgetPreview extends Widget implements Renderer {

    private FeatureF7TerminalWaypoints waypoints;

    public WidgetPreview(FeatureF7TerminalWaypoints lineProperties) {
        this.waypoints = lineProperties;
    }
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    protected Renderer createRenderer() {
        return this;
    }

    public static void drawTextAtWorld(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, float partialTicks) {
        float lScale = scale;

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        Vector3f renderPos = new Vector3f(x,y,z);

        if (increase) {
            double distance = Math.sqrt(renderPos.x * renderPos.x + renderPos.y * renderPos.y + renderPos.z * renderPos.z);
            double multiplier = distance / 120f; //mobs only render ~120 blocks away
            lScale *= 0.45f * multiplier;
        }

        GlStateManager.color(1f, 1f, 1f, 0.5f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPos.x, renderPos.y, renderPos.z);
        GlStateManager.rotate(35, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(25, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-lScale, -lScale, lScale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false); GL11.glDisable(GL11.GL_DEPTH_TEST);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int textWidth = fontRenderer.getStringWidth(text);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        if (renderBlackBox) {
            double j = textWidth / 2;
            GlStateManager.disableTexture2D();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(-j - 1, -1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(-j - 1, 8, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(j + 1, 8, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(j + 1, -1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fontRenderer.drawString(text, -textWidth / 2, 0, color);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        GL11.glViewport(
                (int) buildContext.getAbsBounds().getX(),
                (int) (Minecraft.getMinecraft().displayHeight - buildContext.getAbsBounds().getY() - buildContext.getAbsBounds().getHeight()),
                (int) buildContext.getAbsBounds().getWidth(),
                (int) buildContext.getAbsBounds().getHeight());


        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(70F,
                (float) (buildContext.getSize().getWidth() / buildContext.getSize().getHeight())
                , 0.05F, 4 * 16 * MathHelper.SQRT_2);

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.rotate(25, 1, 0, 0);
        GlStateManager.rotate(125, 0, 1, 0);

        GlStateManager.translate(4, -7, 2);

        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
        vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);

        GlStateManager.enableTexture2D();
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        PreviewWorld previewWorld = new PreviewWorld(Blocks.chest.getStateFromMeta(1), new BlockPos(2,2,4));

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                BlockState toDraw = (x+z) % 2 == 0 ? Blocks.snow.getBlockState() : Blocks.coal_block.getBlockState();;
//                if (z == 0) toDraw = Blocks.clay.getBlockState();
//                if (x == 0 && z == 0) toDraw = Blocks.tnt.getBlockState();

                blockrendererdispatcher.getBlockModelRenderer().renderModel(previewWorld,
                        blockrendererdispatcher.getBlockModelShapes().getModelForState(toDraw.getBaseState()),
                        toDraw.getBaseState(), new BlockPos(x,0,z ), vertexBuffer, false);
            }
        }

        IBlockState iBlockState = Blocks.command_block.getDefaultState();

        blockrendererdispatcher.getBlockModelRenderer().renderModel(previewWorld,
                blockrendererdispatcher.getBlockModelShapes().getModelForState(iBlockState),
                iBlockState, new BlockPos(2,2,4), vertexBuffer, false);
        tessellator.draw();


        if (waypoints.isBeam())
            RenderUtils._renderBeaconBeam(2.5,2.5,4.5, waypoints.getBeamColor(), partialTicks);

        if (waypoints.isBeacon()) {
            GlStateManager.pushMatrix();
            RenderUtils._highlightBlock(new BlockPos(2, 2, 4), waypoints.getHighlightColor(), partialTicks, false);
            GlStateManager.popMatrix();
        }

        if (waypoints.isStatus()) {
            float scale = 0.2f;
            int lol = (int) (System.currentTimeMillis() % 6000);
            int type = lol / 2000;

            if (type == 0) {
                drawTextAtWorld("Done", 2.5f, 2.5f + scale, 4.5f, 0xFF00FF00, 2f, true, false, partialTicks);
            } else if (type == 1) {
                drawTextAtWorld("syeyoung", 2.5f, 2.5f + scale, 4.5f, 0xFFFFFF00, 2f, true, false, partialTicks);
            } else if (type == 2){
                drawTextAtWorld("Incomplete", 2.5f, 2.5f + scale, 4.5f, 0xFFFF0000, 2f, true, false, partialTicks);
            }
        }


        GlStateManager.disableLighting();



        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();




        GL11.glViewport(0,0,Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);

    }
}
