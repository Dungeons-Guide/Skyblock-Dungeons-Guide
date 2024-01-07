/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class Modal extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "title")
    public final BindableAttribute<String> title = new BindableAttribute<>(String.class);
    @Bind(variableName = "width")
    public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class);
    @Bind(variableName = "height")
    public final BindableAttribute<Double> height = new BindableAttribute<>(Double.class);;
    @Bind(variableName = "child")
    public final BindableAttribute<Widget> child = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "closeVisible")
    public final BindableAttribute<String> closevisible = new BindableAttribute<>(String.class, "false");
    public Modal(double width, double height, String title, Widget child, boolean close) {
        super(new ResourceLocation("dungeonsguide:gui/elements/modal.gui"));
        this.width.setValue(width);
        this.height.setValue(height);
        this.title.setValue(title);
        this.child.setValue(child);
        this.closevisible.setValue(close ? "true" : "false");
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        return true;
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
        return true;
    }

    @Override
    public boolean mouseScrolled(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int scrollAmount) {
        return true;
    }

    @On(functionName = "close")
    public void close() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        if (popupMgr != null) popupMgr.closePopup(null);
    }
}
