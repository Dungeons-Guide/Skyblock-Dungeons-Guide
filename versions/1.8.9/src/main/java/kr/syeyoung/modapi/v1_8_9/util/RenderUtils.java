package kr.syeyoung.modapi.v1_8_9.util;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_GREATER;

public class RenderUtils {
    public static void preRenderGui() {
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, 0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void postRenderGui() {
        GlStateManager.alphaFunc(GL_GREATER, 0.1f);
        GlStateManager.enableDepth();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }


    public static void _highlightBlock(VectorI3D blockpos, Color c, float partialTicks, boolean depth) {

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();

        if (!depth) {
            GlStateManager.disableDepth(); GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.depthMask(false);
        }
        GlStateManager.color(c.getRed() /255.0f, c.getGreen() / 255.0f, c.getBlue()/ 255.0f, c.getAlpha()/ 255.0f);

        GlStateManager.translate(blockpos.getX(), blockpos.getY(), blockpos.getZ());

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 1, 0); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 0, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 1, 1);

        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 1, 1); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 1, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 0, 0);

        GL11.glVertex3d(0,1,0);
        GL11.glVertex3d(0,1,1);
        GL11.glVertex3d(1,1,1);
        GL11.glVertex3d(1,1,0);

        GL11.glVertex3d(0,0,1);
        GL11.glVertex3d(0,0,0);
        GL11.glVertex3d(1,0,0);
        GL11.glVertex3d(1,0,1);



        GL11.glEnd();


        if (!depth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();



//...

    }


    public static void pushAndTranslateAccordingToRenderViewEntity(float partialTicks) {
        UEntity viewing_from = ModAPI.getAPI().getRenderViewEntity();

        double x_fix = viewing_from.getPrevPosX() + ((viewing_from.getPosX() - viewing_from.getPrevPosX()) * partialTicks);
        double y_fix = viewing_from.getPrevPosY() + ((viewing_from.getPosY() - viewing_from.getPrevPosY()) * partialTicks);
        double z_fix = viewing_from.getPrevPosZ() + ((viewing_from.getPosZ() - viewing_from.getPrevPosZ()) * partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x_fix, -y_fix, -z_fix);
    }

    public static int getChromaColorAt(int x, int y, float speed, float s, float b, float alpha) {
        double blah = ((double)(speed) * (System.currentTimeMillis() / 2)) % 360;
        return (Color.HSBtoRGB((float) (((blah - (x + y) / 2.0f) % 360) / 360.0f), s,b) & 0xffffff)
                | (((int)(alpha * 255)<< 24) & 0xff000000);
    }
    public static int getColorAt(double x, double y, AColor color) {
        if (!color.isChroma())
            return color.getRGB();

        double blah = ((double)(color.getChromaSpeed()) * (System.currentTimeMillis() / 2)) % 360;
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), hsv);


        return (Color.HSBtoRGB((float) (((blah - (x + y) / 2.0f) % 360) / 360.0f), hsv[1],hsv[2]) & 0xffffff)
                | ((color.getAlpha() << 24) & 0xff000000);
    }
    public static int getColorAt(double x, double y,double z, AColor color) {
        if (!color.isChroma())
            return color.getRGB();

        double blah = ((double)(color.getChromaSpeed()) * (System.currentTimeMillis() / 2)) % 360;
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), hsv);


        return (Color.HSBtoRGB((float) (((blah - ((x + y+z) / 2.0f) % 360)) / 360.0f), hsv[1],hsv[2]) & 0xffffff)
                | ((color.getAlpha() << 24) & 0xff000000);
    }
}
