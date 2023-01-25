/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.gui.tooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationManager {
    public static final NotificationManager INSTANCE = new NotificationManager();
    private NotificationManager() {

    }

    private final Map<UUID, Notification> tooltipList = new HashMap<>();

    public void updateNotification(UUID uid, Notification tooltip) {
        tooltipList.put(uid, tooltip);
    }
    public void removeNotification(UUID uid) {
        tooltipList.remove(uid);
    }


    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || postRender.type == RenderGameOverlayEvent.ElementType.JUMPBAR))
            return;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int widthX = fr.getStringWidth("X");

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.translate(sr.getScaledWidth() - 5, sr.getScaledHeight() -5, 0);

        int currY = sr.getScaledHeight() - 5;

        for (Notification tooltip : tooltipList.values()) {
            int width, height;
            String[] description = tooltip.getDescription().split("\n");
            width =
                    Math.max(
                            fr.getStringWidth(tooltip.getTitle()),
                            Arrays.stream(description).map(fr::getStringWidth).max(Integer::compareTo).orElse(300)
                    ) + 10;
            height = description.length * fr.FONT_HEIGHT + 15 + fr.FONT_HEIGHT;

            GlStateManager.translate(0, -height, 0);
            currY -= height;

            GlStateManager.pushMatrix();
            GlStateManager.translate(-width, 0, 0);
            Gui.drawRect(0, 0,width,height, 0xFF23272a);
            Gui.drawRect(1, 1, width-1, height-1, 0XFF2c2f33);

            if (!tooltip.isUnremovable()) {
                fr.drawString("X", width - widthX - 2, 2, 0xFFFF0000);
            }

            GlStateManager.translate(5,5,0);
            fr.drawString(tooltip.getTitle(), 0,0, tooltip.getTitleColor());
            GlStateManager.translate(0, fr.FONT_HEIGHT + 5, 0);
            int y = 0;
            for (String line : description) {
                fr.drawString(line, 0, y, 0xFFAAAAAA);
                y += fr.FONT_HEIGHT;
            }
            GlStateManager.popMatrix();

            tooltip.setBoundRect(new Rectangle(
                    sr.getScaledWidth() - width - 5,
                    currY,
                    width,
                    height
            ));

            currY -= 5;
            GlStateManager.translate(0, -5, 0);
        }

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
    @SubscribeEvent
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int widthX = fr.getStringWidth("X");

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.translate(sr.getScaledWidth() - 5, sr.getScaledHeight() -5, 0);

        int currY = sr.getScaledHeight() - 5;

        for (Notification tooltip : tooltipList.values()) {
            int width, height;
            String[] description = tooltip.getDescription().split("\n");
            width =
                    Math.max(
                            fr.getStringWidth(tooltip.getTitle()),
                            Arrays.stream(description).map(fr::getStringWidth).max(Integer::compareTo).orElse(300)
                    ) + 10;
            height = description.length * fr.FONT_HEIGHT + 15 + fr.FONT_HEIGHT;

            GlStateManager.translate(0, -height, 0);
            currY -= height;

            GlStateManager.pushMatrix();
            GlStateManager.translate(-width, 0, 0);
            Gui.drawRect(0, 0,width,height, 0xFF23272a);
            Gui.drawRect(1, 1, width-1, height-1, 0XFF2c2f33);

            if (!tooltip.isUnremovable()) {
                if (rendered.mouseX >= sr.getScaledWidth() - 5 - widthX - 2 && rendered.mouseX <= sr.getScaledWidth() - 2
                        && rendered.mouseY >= currY + 2 && rendered.mouseY <= currY + 2 + fr.FONT_HEIGHT) {
                    fr.drawString("X", width - widthX - 2, 2, 0xFFFFAAAA);
                } else {
                    fr.drawString("X", width - widthX - 2, 2, 0xFFFF0000);
                }
            }

            GlStateManager.translate(5,5,0);
            fr.drawString(tooltip.getTitle(), 0,0, tooltip.getTitleColor());
            GlStateManager.translate(0, fr.FONT_HEIGHT + 5, 0);
            int y = 0;
            for (String line : description) {
                fr.drawString(line, 0, y, 0xFFAAAAAA);
                y += fr.FONT_HEIGHT;
            }
            GlStateManager.popMatrix();

            tooltip.setBoundRect(new Rectangle(
                    sr.getScaledWidth() - width - 5,
                    currY,
                    width,
                    height
            ));

            currY -= 5;
            GlStateManager.translate(0, -5, 0);
        }

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int mouseX = Mouse.getX() / sr.getScaleFactor();
        int mouseY = (Minecraft.getMinecraft().displayHeight - Mouse.getY() +3)/ sr.getScaleFactor();
        for (Map.Entry<UUID, Notification> tooltip_ : tooltipList.entrySet()) {
            Notification tooltip = tooltip_.getValue();
            if (tooltip.getBoundRect()  == null) continue;;
            if (tooltip.getBoundRect().contains(mouseX, mouseY)) {

                mouseInputEvent.setCanceled(true);

                if (Mouse.getEventButton() == -1) return;
                if (!Mouse.getEventButtonState()) return;

                int dx = mouseX - tooltip.getBoundRect().x;
                int dy = mouseY - tooltip.getBoundRect().y;

                if (!tooltip.isUnremovable()) {
                    tooltipList.remove(tooltip_.getKey());
                }
                if (dx >= tooltip.getBoundRect().width - 2 - fr.getStringWidth("X") && dx <= tooltip.getBoundRect().width - 2
                && dy >= 2 && dy <= 2 + fr.FONT_HEIGHT) {
                } else {
                    if (tooltip.getOnClick() != null) tooltip.getOnClick().run();
                }

                return;
            }
        }
    }

}
