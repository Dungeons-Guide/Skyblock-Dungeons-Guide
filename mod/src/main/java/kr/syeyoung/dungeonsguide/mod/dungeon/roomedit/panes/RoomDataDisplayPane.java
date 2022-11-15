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
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class RoomDataDisplayPane extends MPanel {

    private int offsetX = 0;
    private int offsetY = 0;

    private int selectedX = -1;
    private int selectedY = -1;

    private final DungeonRoom dungeonRoom;
    public RoomDataDisplayPane(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int[][] blocks = dungeonRoom.getDungeonRoomInfo().getBlocks();
        // draw Axis;
        Gui.drawRect(0,0,10,10,0x77777777);
        clip(clip.x + 10, clip.y, clip.width - 10, 10);
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0x77777777);
        for (int x = 0; x < blocks[0].length; x++) {
            fr.drawString(x+"", x * 16 +10 + offsetX, 0, 0xFFFFFFFF);
        }
        clip(clip.x, clip.y +10, 10, clip.height-10);
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0x77777777);
        for (int z = 0; z < blocks.length; z++) {
            fr.drawString(z+"", 2, z * 16 + 10 + offsetY, 0xFFFFFFFF);
        }

        int hoverX = (relMousex0 - offsetX - 10) / 16;
        int hoverY = (relMousey0 - offsetY - 10) / 16;
        // draw Content
        clip(clip.x + 10, clip.y +10, clip.width - 10, clip.height - 10);
        for (int z = 0; z < blocks.length; z++) {
            for (int x = 0; x < blocks[z].length; x++) {
                int data = blocks[z][x];
                if (z == selectedY && x == selectedX){
                    Gui.drawRect(x *16 +10+offsetX, z *16 +10 + offsetY, x *16 +26 +offsetX, z *16 +26 + offsetY, 0xAA707070);
                } else if (z == hoverY && x == hoverX) {
                    Gui.drawRect(x *16 +10+offsetX, z *16 +10 + offsetY, x *16 +26 +offsetX, z *16 +26 + offsetY, 0xAA505050);
                }


                if (data == -1) fr.drawString("X", x *16 +10 + offsetX, z *16 +10 + offsetY,0xFFFFFF);
                else drawItemStack(new ItemStack(Item.getItemFromBlock(Block.getBlockById(data)), 1), x * 16 +10 + offsetX, z *16 +10 + offsetY);
            }
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

    @Override
    public void keyPressed(char typedChar, int keyCode) {
        int[][] blocks = dungeonRoom.getDungeonRoomInfo().getBlocks();
        if (selectedX != -1 && selectedY != -1 && selectedY < blocks.length && selectedX < blocks[0].length) {
            dungeonRoom.getDungeonRoomInfo().getBlocks()[selectedY][selectedX] = -1;
        }
    }

    private int lastX;
    private int lastY;
    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        lastX = absMouseX;
        lastY = absMouseY;

        if (lastAbsClip.contains(absMouseX, absMouseY)) {
            selectedX = (relMouseX - offsetX - 10) / 16;
            selectedY = (relMouseY - offsetY - 10) / 16;
        }
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
