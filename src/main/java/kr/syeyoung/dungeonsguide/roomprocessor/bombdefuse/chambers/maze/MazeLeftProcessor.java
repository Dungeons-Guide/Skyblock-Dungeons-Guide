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

package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.maze;

import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MazeLeftProcessor extends GeneralDefuseChamberProcessor {
    public MazeLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
    }

    @Override
    public String getName() {
        return "mazeLeft";
    }


    @Override
    public void drawScreen(float partialTicks) {
        if (Minecraft.getMinecraft().objectMouseOver == null ) return;
        MovingObjectPosition pos = Minecraft.getMinecraft().objectMouseOver;
        if (pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        BlockPos block = pos.getBlockPos();
        Block b = getChamber().getRoom().getContext().getWorld().getBlockState(block).getBlock();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        String str = "Press "+ Keyboard.getKeyName(Keybinds.sendBombdefuse.getKeyCode()) + " to request open "+b.getLocalizedName();
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString(str, (sr.getScaledWidth() - fr.getStringWidth(str)) / 2, (sr.getScaledHeight() - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
    }

    @Override
    public void onSendData() {
        if (Minecraft.getMinecraft().objectMouseOver == null ) return;
        MovingObjectPosition pos = Minecraft.getMinecraft().objectMouseOver;
        if (pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        BlockPos block = pos.getBlockPos();
        Block b = getChamber().getRoom().getContext().getWorld().getBlockState(block).getBlock();

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("a", (byte) 5);
        nbt.setInteger("b", Block.getIdFromBlock(b));
        getSolver().communicate(nbt);
    }
}
