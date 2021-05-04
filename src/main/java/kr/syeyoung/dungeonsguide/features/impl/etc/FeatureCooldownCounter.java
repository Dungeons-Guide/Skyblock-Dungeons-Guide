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

package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.DungeonQuitListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiOpenListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FeatureCooldownCounter extends GuiFeature implements DungeonQuitListener, GuiOpenListener {
    public FeatureCooldownCounter() {
        super("ETC", "Dungeon Cooldown Counter", "Counts 10 seconds after leaving dungeon", "qol.cooldown", true, getFontRenderer().getStringWidth("Cooldown: 10s "), getFontRenderer().FONT_HEIGHT);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.orange, "color"));
    }

    private long leftDungeonTime = 0L;
    private final boolean wasInDungeon = false;
    @Override
    public void drawHUD(float partialTicks) {
        if (System.currentTimeMillis() - leftDungeonTime > 20000) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getRectangle().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        fr.drawString("Cooldown: "+(20 - (System.currentTimeMillis() - leftDungeonTime) / 1000)+"s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getRectangle().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        fr.drawString("Cooldown: 20s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void onDungeonQuit() {
        leftDungeonTime = System.currentTimeMillis();
    }

    @Override
    public void onGuiOpen(GuiOpenEvent rendered) {
        if (!(rendered.gui instanceof GuiChest)) return;
        ContainerChest chest = (ContainerChest) ((GuiChest) rendered.gui).inventorySlots;
        if (chest.getLowerChestInventory().getName().contains("On cooldown!")) {
            leftDungeonTime = System.currentTimeMillis();
        } else if (chest.getLowerChestInventory().getName().contains("Error")) {
            leftDungeonTime = System.currentTimeMillis();
        }
    }
}
