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

package kr.syeyoung.dungeonsguide.mod.gui;

import com.mojang.blaze3d.platform.Window;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class GuiScreenAdapterChestOverride extends GuiScreenAdapter {

    @Getter
    protected ContainerScreen guiChest;

    private boolean repositionCursor = false;
    private int cursorX;
    private int cursorY;

    public GuiScreenAdapterChestOverride(Widget widget) {
        super(widget);
    }
    // framebuffer pixels from the bottom left, like 1.8.9's Mouse.setCursorPosition
    public GuiScreenAdapterChestOverride(Widget widget, int cursorX, int cursorY) {
        super(widget);
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.repositionCursor = true;
    }

    public void setGuiChest(ContainerScreen guiChest) {
        this.guiChest = guiChest;
        this.view.getContext().CONTEXT.put("chest", guiChest);

        guiChest.init(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
    }

    public static GuiScreenAdapterChestOverride getAdapter(DomElement domElement) {
        return domElement.getContext().getValue(GuiScreenAdapterChestOverride.class, "screenAdapter");
    }


    // mode is 1.8.9's click mode, the same ids as ContainerInput
    public void emulateClick(int slotId, int mouseButtonClicked, int mode) {
        Minecraft.getInstance().gameMode.handleContainerInput(guiChest.getMenu().containerId, slotId, mouseButtonClicked, ContainerInput.values()[mode], Minecraft.getInstance().player);
    }

    private boolean flag = false;

    // 1.8.9 did the cursor in setWorldAndResolution and the rest in initGui, both run on open and resize
    @Override
    protected void init() {
        super.init();
        if (repositionCursor) {
            Window window = Minecraft.getInstance().getWindow();
            GLFW.glfwSetCursorPos(window.handle(),
                    cursorX * (double) window.getScreenWidth() / window.getWidth(),
                    (window.getHeight() - cursorY) * (double) window.getScreenHeight() / window.getHeight());
        }
        setCanExitWithoutClosing(false);
    }

    public void setCanExitWithoutClosing(boolean flag) {
        this.flag =flag;
    }
    @Override
    public void removed() {
        Minecraft mc = Minecraft.getInstance();
        if (!flag && mc.player.containerMenu.containerId != mc.player.inventoryMenu.containerId) {
            mc.getConnection().send(new ServerboundContainerClosePacket(this.guiChest.getMenu().containerId));
            mc.player.containerMenu.setCarried(ItemStack.EMPTY);
            mc.player.containerMenu = mc.player.inventoryMenu;
        }
        if (guiChest != null)
            guiChest.removed();
        guiChest = null;
        super.removed();
    }
}
