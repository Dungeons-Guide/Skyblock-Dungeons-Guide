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
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WidgetStyleGroup extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "styles")
    public final BindableAttribute widgets = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "name")
    public final BindableAttribute<String> stuff = new BindableAttribute<>(String.class);
    @Bind(variableName = "inherit")
    public final BindableAttribute<String> inherit = new BindableAttribute<>(String.class, "hide");


    private List<Widget> widgetList = new ArrayList<>();

    public boolean anythingUseful() {
        return !widgetList.isEmpty();
    }

    public WidgetStyleGroup(WidgetStyleEdit edit, DefaultingDelegatingTextStyle style, DefaultingDelegatingTextStyle effective, boolean editable) {
        super(new ResourceLocation("dungeonsguide:gui/config/text/stylegroup.gui"));
        stuff.setValue(style.name);
        inherit.setValue(style == effective ? "hide" : "show");

        if (!editable) {
            if (style.size != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Size", () -> style.size != effective.getSize(), style.size, "Overridden in child"));
            if (style.topAscent != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Top Padding", () -> style.topAscent != effective.getTopAscent(), style.topAscent, "Overridden in child"));
            if (style.bottomAscent != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Bottom Padding", () -> style.bottomAscent != effective.getBottomAscent(), style.bottomAscent, "Overridden in child"));
            if (style.bold != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Bold", () -> style.bold != effective.isBold(), style.bold, "Overridden in child"));
            if (style.italics != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Italic", () -> style.italics != effective.isItalics(), style.italics, "Overridden in child"));
            if (style.strikeThrough != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Strike Through",() ->  style.strikeThrough != effective.isStrikeThrough(), style.strikeThrough, "Overridden in child"));
            if (style.underline != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Underline", () -> style.underline != effective.isUnderline(), style.underline, "Overridden in child"));
            if (style.outline != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Outline", () -> style.outline != effective.isOutline(), style.outline, "Overridden in child"));
            if (style.shadow != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Shadow", () -> style.shadow != effective.isShadow(), style.shadow, "Overridden in child"));
            if (style.background != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Background", () -> style.background != effective.hasBackground(), style.background, "Overridden in child"));
            if (style.backgroundShader != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Background Color", () -> style.backgroundShader != effective.getBackgroundShaderColor(), style.backgroundShader, "Overridden in child"));
            if (style.textShader != null)
                widgetList.add(new WidgetConstStyleGroupStyleLine("Text Color", () -> style.textShader != effective.getTextShaderColor(), style.textShader, "Overridden in child"));
            if (style.strikeThroughShader != null)
                widgetList.add(new WidgetConstStyleGroupStyleLineDerivedColor("Strike Through Color", () -> style.strikeThroughShader != effective.getStrikeThroughShaderColor() || !effective.isStrikeThrough(),
                        style.strikeThroughShader.isPresent(), style.strikeThroughShader.orElse(style.getTextShaderColor()),
                        style.strikeThroughShader != effective.getStrikeThroughShaderColor() ? "Overridden in child" : "Strikethrough effectively disabled"));
            if (style.underlineShader != null)
                widgetList.add(new WidgetConstStyleGroupStyleLineDerivedColor("Underline Color", () -> style.underlineShader != effective.getUnderlineShaderColor() || !effective.isUnderline(),
                        style.underlineShader.isPresent(), style.underlineShader.orElse(style.getTextShaderColor()),
                        style.underlineShader != effective.getUnderlineShaderColor() ? "Overridden in child" : "Underline effectively disabled"));
            if (style.outlineShader != null)
                widgetList.add(new WidgetConstStyleGroupStyleLineDerivedColor("Outline Color", () -> style.outlineShader != effective.getOutlineShaderColor() || !effective.isOutline(),
                        style.outlineShader.isPresent(), style.outlineShader.orElse(style.getTextShaderColor()),
                        style.outlineShader != effective.getOutlineShaderColor() ? "Overridden in child" : "Outline effectively disabled"));
            if (style.shadowShader != null)
                widgetList.add(new WidgetConstStyleGroupStyleLineDerivedColor("Shadow Color", () -> style.shadowShader != effective.getShadowShaderColor() || !effective.isShadow(),
                        style.shadowShader.isPresent(), style.shadowShader.orElseGet(() -> {
                            AColor textShader = style.getTextShaderColor();
                            AColor aColor = new AColor(textShader.getRed()/4, textShader.getGreen()/4, textShader.getBlue()/4, textShader.getAlpha());
                            aColor.setChroma(textShader.isChroma());
                            aColor.setChromaSpeed(textShader.getChromaSpeed());
                            return aColor;
                }),
                        style.shadowShader != effective.getShadowShaderColor() ? "Overridden in child" : "Shadow effectively disabled"));
        } else {
            boolean disableable = style.parent != null;
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Size", style.size != null, () -> effective.getSize().doubleValue(), (val) -> {style.size= (Double) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Top Padding", style.topAscent != null, () -> effective.getTopAscent().doubleValue(), (val) -> {style.topAscent= (Double) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Bottom Padding", style.bottomAscent != null,() ->  style.getBottomAscent().doubleValue(), (val) -> {style.bottomAscent= (Double) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Bold", style.bold != null, () -> style.isBold().booleanValue(), (val) -> {style.bold = (Boolean)val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Italic", style.italics != null, () -> style.isItalics().booleanValue(), (val) -> {style.italics = (Boolean)val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Strike Through", style.strikeThrough != null, () -> style.isStrikeThrough().booleanValue(), (val) -> {style.strikeThrough = (Boolean)val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Underline", style.underline != null,() ->  style.isUnderline().booleanValue(), (val) -> {style.underline = (Boolean)val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Outline", style.outline != null, () -> style.isOutline().booleanValue(), (val) -> {style.outline = (Boolean)val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Shadow", style.shadow != null, () -> style.isShadow().booleanValue(), (val) -> {style.shadow = (Boolean)val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Background", style.background != null, () -> style.hasBackground().booleanValue(), (val) -> {style.background = (Boolean)val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(true, "Background Color", style.backgroundShader != null,
                    () -> new AColor(style.getBackgroundShaderColor()), (val) -> {style.backgroundShader = (AColor) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLine(disableable,"Text Color", style.textShader != null,
                    () -> new AColor(style.getTextShaderColor()), (val) -> {style.textShader = (AColor) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLineDerivedColor(true, "Strike Through Color", style.strikeThroughShader != null,
                    () -> style.getStrikeThroughShaderColor(), () -> new AColor(style.getTextShaderColor()), (val) -> {style.strikeThroughShader = (Optional<AColor>) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLineDerivedColor(true, "Underline Color", style.underlineShader != null,
                    () -> style.getUnderlineShaderColor(), () -> new AColor(style.getTextShaderColor()), (val) -> {style.underlineShader = (Optional<AColor>) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLineDerivedColor(true, "Outline Color", style.outlineShader != null,
                    () -> style.getOutlineShaderColor(), () -> new AColor(style.getTextShaderColor()), (val) -> {style.outlineShader = (Optional<AColor>) val; edit.update();}));
            widgetList.add(new WidgetEditableStyleGroupStyleLineDerivedColor(true, "Shadow Color", style.shadowShader != null,
                    () -> style.getShadowShaderColor(), () -> {
                AColor textShader = style.getTextShaderColor();
                AColor aColor = new AColor(textShader.getRed()/4, textShader.getGreen()/4, textShader.getBlue()/4, textShader.getAlpha());
                aColor.setChroma(textShader.isChroma());
                aColor.setChromaSpeed(textShader.getChromaSpeed());
                return aColor;
            },  (val) -> {style.shadowShader = (Optional<AColor>) val; edit.update();}));
        }
        widgets.setValue(widgetList);
    }
    
    public void refresh() {
        for (Widget widget : widgetList) {
            if (widget instanceof WidgetConstStyleGroupStyleLine)
                ((WidgetConstStyleGroupStyleLine) widget).refresh();
        }
    }
}
