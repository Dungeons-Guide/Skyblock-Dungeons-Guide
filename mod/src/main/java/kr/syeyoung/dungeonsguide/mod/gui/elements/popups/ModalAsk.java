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

package kr.syeyoung.dungeonsguide.mod.gui.elements.popups;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class ModalAsk extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "text")
    public final BindableAttribute<String> text = new BindableAttribute<>(String.class);
    @Bind(variableName = "placeholder")
    public final BindableAttribute<String> placeholder = new BindableAttribute<>(String.class);
    @Bind(variableName = "input")
    public final BindableAttribute<String> input = new BindableAttribute<>(String.class, "");

    public ModalAsk(String text, String placeholder, String current) {
        super(Identifier.parse("dungeonsguide:gui/elements/modal_ask.gui"));
        this.text.setValue(text);
        this.placeholder.setValue(placeholder);
        this.input.setValue(current);
    }

    @On(functionName = "confirm")
    public void ok() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        if (popupMgr != null) popupMgr.closePopup(this.input.getValue());
    }

    @On(functionName = "cancel")
    public void no() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        if (popupMgr != null) popupMgr.closePopup(null);
    }
}
