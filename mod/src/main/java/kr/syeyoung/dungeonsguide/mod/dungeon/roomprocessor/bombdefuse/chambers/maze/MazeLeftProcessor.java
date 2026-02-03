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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.maze;


import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.rendering.UFontCalculator;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.world.UBlockState;
import net.kyori.adventure.nbt.CompoundBinaryTag;

public class MazeLeftProcessor extends GeneralDefuseChamberProcessor {
    public MazeLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
    }

    @Override
    public String getName() {
        return "mazeLeft";
    }


    @Override
    public void drawScreen(float partialTicks, RenderingContext context) {
        RaycastResult result = ModAPI.getAPI().getObjectMouseOver();
        if (result.getType() != RaycastResult.HitType.BLOCK) return;

        UBlockState b = getSolver().getDungeonRoom().getRoomWorld().getBlockStateAt(result.getBlockHit());

//        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
//        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        String str = "Press "+ ModAPI.getAPI().getKeyDisplayString(FeatureRegistry.SOLVER_BOMBDEFUSE.<Integer>getParameter("key").getValue()) + " to request open "+b.getBlock().getLocalizedName();
//        GlStateManager.enableBlend();
//        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        int width = (int) (ModAPI.getAPI().getDisplayWidth() / ModAPI.getAPI().getScaleFactor());
        int height = (int) (ModAPI.getAPI().getDisplayHeight() / ModAPI.getAPI().getScaleFactor());
        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        context.drawString(str, (width - fr.getStringWidth(str)) / 2, (height - fr.getFontHeight()) / 2, 0xFFFFFFFF);
    }

    @Override
    public void onSendData() {
        RaycastResult result = ModAPI.getAPI().getObjectMouseOver();
        if (result.getType() != RaycastResult.HitType.BLOCK) return;
        VectorI3D block = result.getBlockHit();
        UBlockState b = getChamber().getRoom().getContext().getWorld().getBlockStateAt(block);

        CompoundBinaryTag nbt = CompoundBinaryTag.builder()
                .putByte("a", (byte)5)
                .putString("b", b.serialize()).build();
        getSolver().communicate(nbt);
    }
}
