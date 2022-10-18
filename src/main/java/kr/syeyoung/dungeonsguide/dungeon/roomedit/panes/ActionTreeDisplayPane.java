/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.roomedit.panes;

import kr.syeyoung.dungeonsguide.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionTree;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionTreeUtil;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ActionTreeDisplayPane extends MPanel {

    private int offsetX = 0;
    private int offsetY = 0;

    private float scale;

    private final DungeonRoom dungeonRoom;
    private final ActionTree tree;
    private List<AbstractAction> linearified;
    public ActionTreeDisplayPane(DungeonRoom dungeonRoom, ActionTree tree) {
        this.dungeonRoom = dungeonRoom;
        this.tree = tree;
        try {
            this.linearified = ActionTreeUtil.linearifyActionTree(tree);
        } catch (Exception e) {
            linearified = new ArrayList<AbstractAction>();
            e.printStackTrace();
        }
        scale = 1.0f;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX, offsetY, 0);
        GlStateManager.scale(scale,scale,1);
        int x = renderTree(tree, 5, 5, Minecraft.getMinecraft().fontRendererObj, null, new HashMap<ActionTree, Point>());
        renderLinearified(linearified, x, 5, fr);
        GlStateManager.popMatrix();
    }

    public int renderTree(ActionTree actionTree, int x, int y, FontRenderer fr, Point drawLineFrom, HashMap<ActionTree, Point> drawmPoints) {
        if (drawmPoints.containsKey(actionTree)) {
            // recursive, fu
            Point pt = drawmPoints.get(actionTree);

            GlStateManager.pushMatrix();

            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableBlend();
            GL11.glLineWidth(1);
            GlStateManager.color(1, 0, 0, 1);
            GL11.glBegin(2);
            GL11.glVertex2d(drawLineFrom.x, drawLineFrom.y);
            GL11.glVertex2d(pt.x, pt.y);
            GL11.glEnd();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();

            return 0;
        }

        Dimension dim = renderAction(actionTree.getCurrent(), x, y, fr);
        if (drawLineFrom != null) {
            GlStateManager.pushMatrix();


            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableBlend();
            GL11.glLineWidth(1);
            GlStateManager.color(1, 1, 1, 1);
            GL11.glBegin(2);
            GL11.glVertex2d(drawLineFrom.x, drawLineFrom.y);
            GL11.glVertex2d(x + dim.width / 2, y);
            GL11.glEnd();
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();

        }
        Point pt = new Point(x + dim.width / 2, y + dim.height);

        drawmPoints.put(actionTree, new Point(x + dim.width / 2, y + dim.height / 2));
        int xOff = 0;
        for (ActionTree tree:actionTree.getChildren()) {
            xOff += renderTree(tree, x + xOff, y + dim.height + 10, fr, pt, drawmPoints) + 10;
        }
        return Math.max(xOff, dim.width);
    }

    public void renderLinearified(List<AbstractAction> actions, int x, int y, FontRenderer fr) {
        Point lastPt = null;
        int y2 = y;

        for (AbstractAction action : actions) {
            Dimension dim = renderAction(action, x, y2, fr);
            if (lastPt != null) {
                GlStateManager.pushMatrix();


                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.enableBlend();
                GL11.glLineWidth(1);
                GlStateManager.color(1, 1, 1, 1);
                GL11.glBegin(2);
                GL11.glVertex2d(lastPt.x, lastPt.y);
                GL11.glVertex2d(x + dim.width / 2, y2);
                GL11.glEnd();
                GlStateManager.disableBlend();
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();

            }
            lastPt = new Point(x + dim.width / 2, y2 + dim.height);

            y2 += dim.height + 10;
        }
    }

    public Dimension renderAction(AbstractAction action, int x, int y, FontRenderer fr) {
        String[] lines = action.toString().split("\n");
        int maxWidth = 0;
        for (String line : lines) {
            if (fr.getStringWidth(line) > maxWidth) maxWidth= fr.getStringWidth(line);
        }
        int offset = 2;
        int height = (fr.FONT_HEIGHT + offset) * lines.length;

        Gui.drawRect(x,y,x + maxWidth +10, y + height + 10, 0xff000000);
        Gui.drawRect(x+1,y+1,x + maxWidth +8, y + height + 8, 0xff4d4d4d);
        for (int i = 0; i < lines.length; i++) {
            fr.drawString(lines[i], x + 5, y + 5 + i*(fr.FONT_HEIGHT + offset), 0xffffffff);
        }

        return new Dimension(maxWidth + 10, height + 10);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,25,parentWidth,parentHeight-25));
    }


    private int lastX;
    private int lastY;
    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        lastX = absMouseX;
        lastY = absMouseY;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        int dX = absMouseX - lastX;
        int dY = absMouseY - lastY;
        offsetX += dX;
        offsetY += dY;
        lastX = absMouseX;
        lastY = absMouseY;
    }

    @Override
    public void mouseScrolled0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (scrollAmount > 0) scale += 0.1;
        if (scrollAmount < 0) scale -= 0.1;

        if (scale < 0) scale = 0.1f;
    }
}
