package kr.syeyoung.modapi.v1_8_9.render;


import kr.syeyoung.dungeonsguide.mod.utils.MathUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import kr.syeyoung.modapi.world.UBlockState;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.List;

public class UWorldRenderContextImpl implements UWorldRenderContext {
    public static final UWorldRenderContextImpl INSTANCE = new UWorldRenderContextImpl();
    private UWorldRenderContextImpl() {}


    @Override
    public void pushMatrix() {
        GlStateManager.pushMatrix();
    }

    @Override
    public void popMatrix() {
        GlStateManager.popMatrix();
    }

    @Override
    public void translate(double x, double y, double z) {
        GlStateManager.translate(x,y,z);
    }

    @Override
    public void scale(double x, double y, double z) {
        GlStateManager.scale(x,y,z);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        GlStateManager.rotate(angle, x, y, z);
    }



    public void pushAndTranslateAccordingToRenderViewEntity(float partialTicks) {
        UEntity viewing_from = ModAPI.getAPI().getRenderViewEntity();

        double x_fix = viewing_from.getPrevPosX() + ((viewing_from.getPosX() - viewing_from.getPrevPosX()) * partialTicks);
        double y_fix = viewing_from.getPrevPosY() + ((viewing_from.getPosY() - viewing_from.getPrevPosY()) * partialTicks);
        double z_fix = viewing_from.getPrevPosZ() + ((viewing_from.getPosZ() - viewing_from.getPrevPosZ()) * partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x_fix, -y_fix, -z_fix);
    }

    @Override
    public void highlightBox(AABB axisAlignedBB, int color, boolean isChroma, float chromaSpeed, float partialTicks, boolean depth) {

        pushAndTranslateAccordingToRenderViewEntity(partialTicks);

        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();

        if (!depth) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }

        int rgb = getColorAt(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, color, isChroma, chromaSpeed);
        GlStateManager.color(((rgb >> 16) &0XFF)/ 255.0f, ((rgb>>8) &0XFF)/ 255.0f, (rgb & 0xff)/ 255.0f, ((rgb >> 24) & 0xFF) / 255.0f);

        GlStateManager.translate(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);

        double x = axisAlignedBB.maxX - axisAlignedBB.minX;
        double y = axisAlignedBB.maxY - axisAlignedBB.minY;
        double z = axisAlignedBB.maxZ - axisAlignedBB.minZ;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, z);
        GL11.glVertex3d(0, y, z);
        GL11.glVertex3d(0, y, 0); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(x, 0, z);
        GL11.glVertex3d(x, 0, 0);
        GL11.glVertex3d(x, y, 0);
        GL11.glVertex3d(x, y, z);

        GL11.glVertex3d(0, y, z);
        GL11.glVertex3d(0, 0, z);
        GL11.glVertex3d(x, 0, z);
        GL11.glVertex3d(x, y, z); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, y, 0);
        GL11.glVertex3d(x, y, 0);
        GL11.glVertex3d(x, 0, 0);

        GL11.glVertex3d(0,y,0);
        GL11.glVertex3d(0,y,z);
        GL11.glVertex3d(x,y,z);
        GL11.glVertex3d(x,y,0);

        GL11.glVertex3d(0,0,z);
        GL11.glVertex3d(0,0,0);
        GL11.glVertex3d(x,0,0);
        GL11.glVertex3d(x,0,z);



        GL11.glEnd();


        if (!depth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.enableAlpha();

    }

    @Override
    public void drawTextAtWorld(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, float partialTicks, boolean depth) {
        float lScale = scale;

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        if (increase) {
            double distSq = ModAPI.getAPI().getPlayer().getPositionEyes(partialTicks).distanceSq(x, y, z);
            double distance = Math.sqrt(distSq);
            double multiplier = distance / 120f; //mobs only render ~120 blocks away
            lScale *= 0.45f * multiplier;
        }

        GlStateManager.color(1f, 1f, 1f, 0.5f);
        pushAndTranslateAccordingToRenderViewEntity(partialTicks);
        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-lScale, -lScale, lScale);
        GlStateManager.disableLighting();
        if (!depth){
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
        }
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
        if (!depth) {
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
        }
        GlStateManager.popMatrix();
        GlStateManager.enableAlpha();
    }


    @Override
    public void highlightBlockStencil(VectorI3D pos, float partialTicks, Color color, boolean depth) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        pushAndTranslateAccordingToRenderViewEntity(partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GlStateManager.enableAlpha();

        GL11.glStencilMask(0xFF);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, depth ? GL11.GL_KEEP : GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0f, -1.0f);


        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
        vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);

        if (depth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(false);
        } else {
            GlStateManager.disableDepth();
        }
        GlStateManager.enableTexture2D();
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        double minX = 1e9, minY = 1e9, minZ = 1e9, maxX = -1e9, maxY = -1e9, maxZ = -1e9;
        {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX() + 1);
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY() + 1);
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ() + 1);
            UBlockState iBlockState = ModAPI.getAPI().getWorld().getBlockStateAt(pos);
            if (iBlockState.hasTileEntity()) {
                TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos(pos.x, pos.y, pos.z));
                TileEntitySpecialRenderer specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);
                if (specialRenderer != null) {
                    specialRenderer.renderTileEntityAt(tileEntity,pos.getX(),pos.getY(),pos.getZ(), partialTicks, -1);
                    for (kr.syeyoung.modapi.data.EnumFacing value : kr.syeyoung.modapi.data.EnumFacing.HORIZONTALS) {
                        VectorI3D newPos = pos.add(value.getDirectionVec());
                        iBlockState = ModAPI.getAPI().getWorld().getBlockStateAt(newPos);

                        if (iBlockState.hasTileEntity()) {
                            tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos(newPos.x, newPos.y, newPos.z));
                            specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);
                            if (specialRenderer != null)
                                specialRenderer.renderTileEntityAt(tileEntity, newPos.getX(), newPos.getY(), newPos.getZ(), partialTicks, -1);
                        }
                    }
                } else {
                    blockrendererdispatcher.getBlockModelRenderer().renderModelStandard(Minecraft.getMinecraft().theWorld,
                            blockrendererdispatcher.getModelFromBlockState((IBlockState) iBlockState.getIBlockState(), Minecraft.getMinecraft().theWorld, new BlockPos(pos.x, pos.y, pos.z)),
                            ((IBlockState)iBlockState.getIBlockState()).getBlock(), new BlockPos(pos.x, pos.y, pos.z), vertexBuffer, depth ? true : false);
                }
            } else {
                blockrendererdispatcher.getBlockModelRenderer().renderModelStandard(Minecraft.getMinecraft().theWorld,
                        blockrendererdispatcher.getModelFromBlockState((IBlockState) iBlockState.getIBlockState(), Minecraft.getMinecraft().theWorld, new BlockPos(pos.x, pos.y, pos.z)),
                        ((IBlockState)iBlockState.getIBlockState()).getBlock(), new BlockPos(pos.x, pos.y, pos.z), vertexBuffer, depth ? true : false);
            }
        }


        tessellator.draw();
        if (depth) {
            GlStateManager.depthMask(true);
            GlStateManager.disableDepth();
        }

        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glColorMask(true, true, true, true);

        // now render highlight.

        GlStateManager.popMatrix();

        AABB bb= new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        highlightBox(bb, color.getRGB(), partialTicks, false);

        GL11.glDisable(GL11.GL_STENCIL_TEST);

        GlStateManager.enableDepth();
//        return bb;
    }
    @Override
    public AABB highlightBlocksStencil(List<VectorI3D> blockPos, float partialTicks, Color color, boolean depth) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        pushAndTranslateAccordingToRenderViewEntity(partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GlStateManager.enableAlpha();

        GL11.glStencilMask(0xFF);
        GL11.glColorMask(false, false, false, false);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0f, -1.0f);


        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
        vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);

        if (depth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(false);
        } else {
            GlStateManager.disableDepth();
        }
        GlStateManager.enableTexture2D();
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        double minX = 1e9, minY = 1e9, minZ = 1e9, maxX = -1e9, maxY = -1e9, maxZ = -1e9;
        for (VectorI3D pos : blockPos) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX() + 1);
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY() + 1);
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ() + 1);
            UBlockState iBlockState = ModAPI.getAPI().getWorld().getBlockStateAt(pos);
            if (iBlockState.hasTileEntity()) {
                TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos(pos.x, pos.y, pos.z));
                TileEntitySpecialRenderer specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);
                if (specialRenderer != null) {
                    specialRenderer.renderTileEntityAt(tileEntity,pos.getX(),pos.getY(),pos.getZ(), partialTicks, -1);
                    for (kr.syeyoung.modapi.data.EnumFacing value : kr.syeyoung.modapi.data.EnumFacing.HORIZONTALS) {
                        VectorI3D newPos = pos.add(value.getDirectionVec());
                        iBlockState = ModAPI.getAPI().getWorld().getBlockStateAt(newPos);

                        if (iBlockState.hasTileEntity()) {
                            tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(new BlockPos(newPos.x, newPos.y, newPos.z));
                            specialRenderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);
                            if (specialRenderer != null)
                                specialRenderer.renderTileEntityAt(tileEntity, newPos.getX(), newPos.getY(), newPos.getZ(), partialTicks, -1);
                        }
                    }
                } else {
                    blockrendererdispatcher.getBlockModelRenderer().renderModelStandard(Minecraft.getMinecraft().theWorld,
                            blockrendererdispatcher.getModelFromBlockState((IBlockState) iBlockState.getIBlockState(), Minecraft.getMinecraft().theWorld, new BlockPos(pos.x, pos.y, pos.z)),
                            ((IBlockState)iBlockState.getIBlockState()).getBlock(), new BlockPos(pos.x, pos.y, pos.z), vertexBuffer, depth ? true : false);
                }
            } else {
                blockrendererdispatcher.getBlockModelRenderer().renderModelStandard(Minecraft.getMinecraft().theWorld,
                        blockrendererdispatcher.getModelFromBlockState((IBlockState) iBlockState.getIBlockState(), Minecraft.getMinecraft().theWorld, new BlockPos(pos.x, pos.y, pos.z)),
                        ((IBlockState)iBlockState.getIBlockState()).getBlock(), new BlockPos(pos.x, pos.y, pos.z), vertexBuffer, depth ? true : false);
            }
        }

        tessellator.draw();
        if (depth) {
            GlStateManager.depthMask(true);
            GlStateManager.disableDepth();
        }
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glColorMask(true, true, true, true);

        // now render highlight.

        GlStateManager.popMatrix();

        AABB bb= new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        highlightBox(bb, color.getRGB(), partialTicks, false);

        GL11.glDisable(GL11.GL_STENCIL_TEST);

        GlStateManager.enableDepth();
        return bb;
    }


    @Override
    public void renderBlock(int x, int y, int z, UBlockState blockState) {
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
//                        GlStateManager.disableDepth();
//                        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
        vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
//                        GlStateManager.color(1.0f,1.0f,1.0f,0.1f);
        blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().theWorld,
                blockrendererdispatcher.getBlockModelShapes().getModelForState((IBlockState) blockState.getIBlockState()),
                (IBlockState) blockState.getIBlockState(), new BlockPos(0,0,0), vertexBuffer, false);
        tessellator.draw();

        GlStateManager.enableLighting();
    }
    private static final ResourceLocation beaconBeam = new ResourceLocation("textures/entity/beacon_beam.png");


    public static int getColorAt(double x, double y,double z, int color, boolean chroma, float chromaSpeed) {
        if (!chroma)
            return color;

        double blah = ((double)(chromaSpeed) * (System.currentTimeMillis() / 2)) % 360;
        float[] hsv = new float[3];
        Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, hsv);


        return (Color.HSBtoRGB((float) (((blah - ((x + y+z) / 2.0f) % 360)) / 360.0f), hsv[1],hsv[2]) & 0xffffff)
                | ((color) & 0xff000000);
    }


    @Override
    public void renderBeaconBeam(double x, double y, double z, int color, boolean chroma, float chromaSpeed, float partialTicks) {
        pushAndTranslateAccordingToRenderViewEntity(partialTicks);

        _renderBeaconBeam(x,y,z, color, chroma, chromaSpeed, partialTicks);
        GlStateManager.popMatrix();
    }

    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     * And modified to fit out need.
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     * @author Moulberry
     */

    private void _renderBeaconBeam(double x, double y, double z, int color,boolean chroma, float chromaSpeed, float partialTicks) {
        int height = 300;
        int bottomOffset = 0;
        int topOffset = bottomOffset + height;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        Minecraft.getMinecraft().getTextureManager().bindTexture(beaconBeam);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        double time = System.currentTimeMillis() / 50 + (double)partialTicks;
        double d1 = MathUtils.frac(-time * 0.2D - Math.floor(-time * 0.1D));

        int c = getColorAt(x,y,z, color, chroma, chromaSpeed);
        float alpha = ((c >> 24) & 0xFF) / 255.0f;

        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        double d2 = time * 0.025D * -1.5D;
        double d4 = 0.5D + Math.cos(d2 + 2.356194490192345D) * 0.2D;
        double d5 = 0.5D + Math.sin(d2 + 2.356194490192345D) * 0.2D;
        double d6 = 0.5D + Math.cos(d2 + (Math.PI / 4D)) * 0.2D;
        double d7 = 0.5D + Math.sin(d2 + (Math.PI / 4D)) * 0.2D;
        double d8 = 0.5D + Math.cos(d2 + 3.9269908169872414D) * 0.2D;
        double d9 = 0.5D + Math.sin(d2 + 3.9269908169872414D) * 0.2D;
        double d10 = 0.5D + Math.cos(d2 + 5.497787143782138D) * 0.2D;
        double d11 = 0.5D + Math.sin(d2 + 5.497787143782138D) * 0.2D;
        double d14 = -1.0D + d1;
        double d15 = (double)(height) * 2.5D + d14;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0D, d15).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0D, d14).color(r, g, b, 1.0F).endVertex();
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0D, d15).color(r, g, b, alpha).endVertex();
        tessellator.draw();

        double d12 = -1.0D + d1;
        double d13 = height + d12;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.2D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.2D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + bottomOffset, z + 0.8D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.8D, y + topOffset, z + 0.8D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.8D).tex(1.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.8D).tex(1.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + bottomOffset, z + 0.2D).tex(0.0D, d12).color(r, g, b, 0.25F).endVertex();
        worldrenderer.pos(x + 0.2D, y + topOffset, z + 0.2D).tex(0.0D, d13).color(r, g, b, 0.25F*alpha).endVertex();
        tessellator.draw();

        GlStateManager.enableDepth();
    }

    public void drawLinesVec3(List<Vector3D> poses, int color, boolean chroma, float chromaSpeed, float thickness, float partialTicks, boolean depth) {
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        pushAndTranslateAccordingToRenderViewEntity(partialTicks);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GL11.glLineWidth(thickness);
        if (!depth) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

//        GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue()/ 255f, colour.getAlpha() / 255f);
        GlStateManager.color(1,1,1,1);
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int num = 0;
        for (Vector3D pos:poses) {
            int i = UGuiRenderContextImpl.getColorAt(num++ * 10,0, color, chroma, chromaSpeed);
            worldRenderer.pos(pos.x, pos.y, pos.z).color(
                    ((i >> 16) &0xFF)/255.0f,
                    ((i >> 8) &0xFF)/255.0f,
                    (i &0xFF)/255.0f,
                    ((i >> 24) &0xFF)/255.0f
            ).endVertex();
        }
        Tessellator.getInstance().draw();

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        if (!depth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GL11.glLineWidth(1);
    }

    @Override
    public void drawQuad(Vector3D center, Vector3D plane1, Vector3D plane2, int color, float partialTicks) {

    }
}
