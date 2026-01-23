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

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.gui.UContainerChest;
import lombok.Getter;
import org.lwjgl.input.Mouse;

public class CustomGuiScreenAdapterChestOverride extends CustomGuiScreenAdapter {

    @Getter
    protected UContainerChest guiChest;

    private boolean repositionCursor = false;
    private int cursorX;
    private int cursorY;

    public CustomGuiScreenAdapterChestOverride(Widget widget) {
        super(widget);
    }
    public CustomGuiScreenAdapterChestOverride(Widget widget, int cursorX, int cursorY) {
        super(widget);
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.repositionCursor = true;
    }

    @Override
    public void init() {
        ModAPI.getAPI().getPlayer().setOpenContainer(guiChest);
        if (repositionCursor) {
            Mouse.setCursorPosition(cursorX, cursorY);
        }
    }

    public void setGuiChest(UContainerChest guiChest) {
        this.guiChest = guiChest;
        this.view.getContext().CONTEXT.put("chest", guiChest);
    }

    public static CustomGuiScreenAdapterChestOverride getAdapter(DomElement domElement) {
        return domElement.getContext().getValue(CustomGuiScreenAdapterChestOverride.class, "screenAdapter");
    }


    public void emulateClick(int slotId, int mouseButtonClicked, int mode) {
        guiChest.clickSlot(slotId, UContainer.EnumClickType.CHOOSE);
    }

    private boolean flag = false;

    public void setCanExitWithoutClosing(boolean flag) {
        this.flag =flag;
    }

    @Override
    public void closeScreenRequested() {
        if (guiChest != null)
            guiChest.closeContainer();
    }


    @Override
    public void onRemoved() {
        super.onRemoved();
        guiChest = null;
    }
}
