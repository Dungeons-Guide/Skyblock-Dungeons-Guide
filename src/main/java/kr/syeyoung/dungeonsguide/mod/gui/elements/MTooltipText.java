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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MTooltipText extends MTooltip {
    @Getter @Setter
    private List<String> tooltipText = new ArrayList<>();
    @Override
    public Rectangle getBounds() {
        return new Rectangle(0,0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        GuiUtils.drawHoveringText(tooltipText, relMousex0, relMousey0, (int) (Minecraft.getMinecraft().displayWidth/getRelativeScale()), (int) (Minecraft.getMinecraft().displayHeight/getRelativeScale()), -1, Minecraft.getMinecraft().fontRendererObj);
    }
}
