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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.panes;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MTooltipText;
import kr.syeyoung.dungeonsguide.mod.utils.ArrayUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.Arrays;
import java.util.UUID;

public class RoomMatchDisplayPane extends MPanel {

    private int offsetX = 0;
    private int offsetY = 0;

    private final DungeonRoom dungeonRoom;

    private final int[][] currentBlocks;
    private int[][] targetBlocks;
    public RoomMatchDisplayPane(DungeonRoom dungeonRoom, UUID uid, int rotation) {
        this.dungeonRoom = dungeonRoom;

        currentBlocks = dungeonRoom.getRoomMatcher().createNew().getBlocks();
        targetBlocks = DungeonRoomInfoRegistry.getByUUID(uid).getBlocks();
        for (int i = 0; i < rotation; i++)
            targetBlocks = ArrayUtils.rotateCounterClockwise(targetBlocks);
    }

    MTooltip mTooltip;
    int lastTooltipX = -1, lastTooltipZ = -1;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int height = Math.max(currentBlocks.length, targetBlocks.length);
        int width = Math.max(currentBlocks[0].length, targetBlocks[0].length);

        // draw Axis;
        Gui.drawRect(0,0,10,10,0x77777777);
        clip(clip.x + 10, clip.y, clip.width - 10, 10);
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0x77777777);
        for (int x = 0; x < width; x++) {
            fr.drawString(x+"", x * 16 +10 + offsetX, 0, 0xFFFFFFFF);
        }
        clip(clip.x, clip.y +10, 10, clip.height-10);
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0x77777777);
        for (int z = 0; z < height; z++) {
            fr.drawString(z+"", 2, z * 16 + 10 + offsetY, 0xFFFFFFFF);
        }

        int hoverX = (relMousex0 - offsetX - 10) / 16;
        int hoverY = (relMousey0 - offsetY - 10) / 16;
        // draw Content
        clip(clip.x + 10, clip.y +10, clip.width - 10, clip.height - 10);
        boolean tooltiped=false;
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int data1;
                try { data1 = currentBlocks[z][x]; } catch (Exception e) {
                    data1 = -2;
                }
                int data2;
                try { data2 = targetBlocks[z][x]; } catch (Exception e) {
                    data2 = -2;
                }

                if (z == hoverY && x == hoverX) {
                    Gui.drawRect(x *16 +10+offsetX, z *16 +10 + offsetY, x *16 +26 +offsetX, z *16 +26 + offsetY, 0xAA505050);
                }

                if (data1 == data2) drawItemStack(new ItemStack(Item.getItemFromBlock(Block.getBlockById(data1)), 1), x * 16 +10 + offsetX, z *16 +10 + offsetY);
                else if (data2 == -1 || data1 == -1) {
                    drawItemStack(new ItemStack(Item.getItemFromBlock(Block.getBlockById(data1 == -1 ? data2 : data1)), 1), x * 16 +10 + offsetX, z *16 +10 + offsetY);
                    fr.drawString("S", x *16 +10 + offsetX, z *16 +10 + offsetY,0xFFFFFF00);
                } else {
                    fr.drawString("N", x *16 +10 + offsetX, z *16 +10 + offsetY,0xFFFF0000);
                }
                if (z == hoverY && x == hoverX) {
                    tooltiped = true;
                    if (lastTooltipX != x || lastTooltipZ != z){
                        if (mTooltip != null) mTooltip.close();
                        mTooltip = new MTooltipText(Arrays.asList("Expected "+data2 +" But found "+data1));
                        mTooltip.open(this);
                    }
                }
            }
        }
        if (!tooltiped && mTooltip != null) {
            mTooltip.close();
            mTooltip = null;
        }

    }
    private void drawItemStack(ItemStack stack, int x, int y)
    {
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10,parentHeight-10));
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
}
