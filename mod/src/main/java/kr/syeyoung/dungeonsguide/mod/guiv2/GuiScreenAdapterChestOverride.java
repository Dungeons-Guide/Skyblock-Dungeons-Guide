/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.GL_GREATER;

public class GuiScreenAdapterChestOverride extends GuiScreenAdapter {

    @Getter
    protected GuiChest guiChest;

    private boolean repositionCursor = false;
    private int cursorX;
    private int cursorY;

    public GuiScreenAdapterChestOverride(Widget widget) {
        super(widget);
    }
    public GuiScreenAdapterChestOverride(Widget widget, int cursorX, int cursorY) {
        super(widget);
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.repositionCursor = true;
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        if (repositionCursor) {
            Mouse.setCursorPosition(cursorX, cursorY);
        }
    }

    public void setGuiChest(GuiChest guiChest) {
        this.guiChest = guiChest;
        this.view.getContext().CONTEXT.put("chest", guiChest);

        guiChest.setWorldAndResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        guiChest.initGui();
    }

    public static GuiScreenAdapterChestOverride getAdapter(DomElement domElement) {
        return domElement.getContext().getValue(GuiScreenAdapterChestOverride.class, "screenAdapter");
    }


    public void emulateClick(int slotId, int mouseButtonClicked, int mode) {
        Minecraft.getMinecraft().playerController.windowClick(guiChest.inventorySlots.windowId, slotId, mouseButtonClicked, mode, Minecraft.getMinecraft().thePlayer);
    }

    private boolean flag = false;

    @Override
    public void initGui() {
        super.initGui();
        setCanExitWithoutClosing(false);
    }

    public void setCanExitWithoutClosing(boolean flag) {
        this.flag =flag;
    }
    @Override
    public void onGuiClosed() {
        if (!flag && Minecraft.getMinecraft().thePlayer.openContainer.windowId != Minecraft.getMinecraft().thePlayer.inventoryContainer.windowId) {
            Minecraft.getMinecraft().getNetHandler().addToSendQueue(new C0DPacketCloseWindow(this.guiChest.inventorySlots.windowId));
            Minecraft.getMinecraft().thePlayer.inventory.setItemStack(null);
            Minecraft.getMinecraft().thePlayer.openContainer = Minecraft.getMinecraft().thePlayer.inventoryContainer;
        }
        if (guiChest != null)
            guiChest.onGuiClosed();
        guiChest = null;
        super.onGuiClosed();
    }
}
