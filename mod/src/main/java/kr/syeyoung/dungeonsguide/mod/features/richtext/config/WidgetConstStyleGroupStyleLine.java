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

package kr.syeyoung.dungeonsguide.mod.features.richtext.config;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MouseTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import scala.actors.threadpool.Arrays;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class WidgetConstStyleGroupStyleLine extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "erase")
    public final BindableAttribute<String> erase = new BindableAttribute<>(String.class, "hide");
    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "value")
    public final BindableAttribute<Widget> value = new BindableAttribute<>(Widget.class);

    private List<String> hover;
    private Supplier<Boolean> eraseEvaluator;
    public WidgetConstStyleGroupStyleLine(String name, Supplier<Boolean> erase, Object value, String eraseReason) {
        super(new ResourceLocation("dungeonsguide:gui/config/text/styleline.gui"));
        this.name.setValue(name);
        this.eraseEvaluator = erase;
        refresh();

        if (erase.get())
            hover = Arrays.asList(eraseReason.split("\n"));

        if (value instanceof AColor) {
            this.value.setValue(new WidgetConstColor(true, (AColor) value));
        } else if (value instanceof Number) {
            this.value.setValue(new Text(value.toString(), 0xFFAAAAAA, Text.TextAlign.LEFT, Text.WordBreak.WORD, 1.0, 8.0));
        } else if (value instanceof Boolean) {
            this.value.setValue(new WidgetConstCheckmark((Boolean) value));
        }
    }

    public void refresh() {
        this.erase.setValue(eraseEvaluator.get() ? "show" : "hide");
    }

    private MinecraftTooltip actualTooltip = new MinecraftTooltip();
    private MouseTooltip tooltip = null;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (hover == null) return false;
        getDomElement().setCursor(EnumCursor.NOT_ALLOWED);
        List<String> toHover = hover;

        if (toHover != null)
            actualTooltip.setTooltip(toHover);

        if (toHover == null && this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip);
            this.tooltip = null;
        } else if (toHover != null && this.tooltip == null)
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = new MouseTooltip(actualTooltip), (a) -> {
                        this.tooltip = null;
                    });
        return false;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
    }

    @Override
    public void onUnmount() {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
        super.onUnmount();
    }
}
