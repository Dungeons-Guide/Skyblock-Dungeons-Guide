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

package kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.widget;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class WidgetColorButton extends AnnotatedImportOnlyWidget implements Renderer {
    @Bind(variableName = "text")
    public final BindableAttribute<String> text = new BindableAttribute<>(String.class);
    @Bind(variableName = "disabled")
    public final BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "normal")
    public final BindableAttribute<Integer> normal = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "hover")
    public final BindableAttribute<Integer> hover = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "press")
    public final BindableAttribute<Integer> press = new BindableAttribute<>(Integer.class);

    @Bind(variableName = "click")
    public final BindableAttribute<Runnable> onClick = new BindableAttribute<>(Runnable.class);

    private static final int[] colorCode = new int[32];
    static {
        for(int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;
            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    private boolean chroma = false;
    public WidgetColorButton(boolean enabled, String colorcode, Runnable onClick) {
        super(new ResourceLocation("dungeonsguide:gui/config/cosmetics/button.gui"));
        text.setValue(enabled ? "Available" : "Locked");
        disabled.setValue(false);
        this.onClick.setValue(onClick);
        int i1 = "0123456789abcdefz".indexOf(colorcode.toLowerCase(Locale.ENGLISH).charAt(0));

        if (i1 == 16) {
            chroma = true;

            int color =RenderUtils.getChromaColorAt(0, 0, 0.5f, 1, 1, 1.0f);
            normal.setValue(color);
            hover.setValue(RenderUtils.blendAlpha(color, 0.2f));
            press.setValue(RenderUtils.blendAlpha(color, 0.4f));
        } else {
            int color = colorCode[i1] | 0xFF000000;
            normal.setValue(color);
            hover.setValue(RenderUtils.blendAlpha(color, 0.2f));
            press.setValue(RenderUtils.blendAlpha(color, 0.4f));
        }
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        if (chroma) {
            int color =RenderUtils.getChromaColorAt(0, 0, 0.3f, 1, 1, 1.0f);
            normal.setValue(color);
            hover.setValue(RenderUtils.blendAlpha(color, 0.2f));
            press.setValue(RenderUtils.blendAlpha(color, 0.4f));
        }

        SingleChildRenderer.INSTANCE.doRender(absMouseX, absMouseY, relMouseX, relMouseY, partialTicks, context, buildContext);
    }
}
