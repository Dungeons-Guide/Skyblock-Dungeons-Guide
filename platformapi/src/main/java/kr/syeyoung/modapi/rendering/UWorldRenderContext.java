package kr.syeyoung.modapi.rendering;

import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.UBlockState;

import java.awt.*;
import java.util.List;

public interface UWorldRenderContext extends URenderContext {
    void highlightBox(AABB aabb, int color,boolean chroma, float chromaSpeed, float partialTicks, boolean depth);

    default void highlightBox(AABB aabb, int color, float partialTicks, boolean depth) {
        highlightBox(aabb, color, false, 0, partialTicks, depth);
    }

    default void highlightBox(double offX, double offY, double offZ, AABB aabb, int color, float partialTicks, boolean depth) {
        highlightBox(new AABB(
                aabb.minX + offX, aabb.minY + offY, aabb.minZ + offZ,
                aabb.maxX + offX, aabb.maxY + offY, aabb.maxX + offZ
        ), color, partialTicks, depth);
    }

    void drawTextAtWorld(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, float partialTicks, boolean depth);

    default void drawTextAtWorld(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, float partialTicks) {
        drawTextAtWorld(text, x, y, z, color, scale, increase, renderBlackBox, partialTicks, false);
    }


    default void highlightBlock(VectorI3D allInBox, Color color, float partialTicks, boolean depth) {
        highlightBox(new AABB(
                allInBox.x, allInBox.y, allInBox.z, allInBox.x + 1, allInBox.y + 1, allInBox.z + 1
        ), color.getRGB(), partialTicks, depth);
    }
    default void highlightBlock(VectorI3D allInBox, int color, float partialTicks, boolean depth) {
        highlightBox(new AABB(
                allInBox.x, allInBox.y, allInBox.z, allInBox.x + 1, allInBox.y + 1, allInBox.z + 1
        ), color, partialTicks, depth);
    }

    AABB highlightBlocksStencil(List<VectorI3D> list, float partialTicks, Color color, boolean b);
    void highlightBlockStencil(VectorI3D pos, float partialTicks, Color rgb, boolean b);

    void pushAndTranslateAccordingToRenderViewEntity(float partialTicks);

    void renderBlock(int x, int y, int z, UBlockState blockState);

    void renderBeaconBeam(double x, double y, double z, int color,boolean chroma, float chromaSpeed, float partialTicks);

    void drawLinesVec3(List<Vector3D> lines, int color, boolean chroma, float chromaSpeed, float thickness, float partialTicks, boolean depth);


    void drawQuad(Vector3D center, Vector3D plane1, Vector3D plane2, int color, float partialTicks);
}
