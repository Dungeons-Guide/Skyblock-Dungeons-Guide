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

import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.BreakWord;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.RichText;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ITextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class WidgetTextStyleConfig extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "visibility")
    public final BindableAttribute<String> visibleWidget = new BindableAttribute<>(String.class, "group");

    @Bind(variableName = "text")
    public final BindableAttribute<Widget> text = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "styleEdit")
    public final BindableAttribute<Widget> styleEdit = new BindableAttribute<>(Widget.class);

    private RichText richText;

    @Bind(variableName = "groups")
    public final BindableAttribute classes = new BindableAttribute<>(WidgetList.class);


    private final TextSpan span;
    private final Map<String, DefaultingDelegatingTextStyle> styleMap;
    private final Map<String, WidgetGroupButton> buttons = new HashMap<>();
    public WidgetTextStyleConfig(TextSpan span, Map<String, DefaultingDelegatingTextStyle> styles) {
        super(new ResourceLocation("dungeonsguide:gui/config/text/textconfig.gui"));
        this.span = span;
        this.styleMap = styles;

        Map<ITextStyle, DefaultingDelegatingTextStyle> wrapped = new HashMap<>();

        Queue<TextSpan> toVisit = new LinkedList<>();
        toVisit.add(span);
        while (!toVisit.isEmpty()) {
            TextSpan span1 = toVisit.poll();
            ITextStyle style = span1.getTextStyle();
            if (!wrapped.containsKey(span1.getTextStyle()))
                wrapped.put(span1.getTextStyle(), DefaultingDelegatingTextStyle.derive("Config Hack", () -> style));
            span1.setTextStyle(wrapped.get(span1.getTextStyle()));
            toVisit.addAll(span1.getChildren());
        }

        text.setValue(richText = new RichText(span, BreakWord.WORD, false, RichText.TextAlign.LEFT));

        List<Widget> list = new ArrayList<>();
        for (Map.Entry<String, DefaultingDelegatingTextStyle> stringDefaultingDelegatingTextStyleEntry : styles.entrySet()) {
            WidgetGroupButton widgetGroupButton = new WidgetGroupButton(this, stringDefaultingDelegatingTextStyleEntry.getKey(),
                    stringDefaultingDelegatingTextStyleEntry.getValue(), wrapped.get(stringDefaultingDelegatingTextStyleEntry.getValue()));
            buttons.put(stringDefaultingDelegatingTextStyleEntry.getKey(), widgetGroupButton);
            list.add(widgetGroupButton);
        }
        classes.setValue(list);
    }

    public void enterEdit(DefaultingDelegatingTextStyle style) {
        // add smth
        styleEdit.setValue(new WidgetStyleEdit(this, style));
        visibleWidget.setValue("style");
    }

    public void exitEdit() {
        for (Map.Entry<String, WidgetGroupButton> stringWidgetGroupButtonEntry : buttons.entrySet()) {
            stringWidgetGroupButtonEntry.getValue().setNewBG(
                    styleMap.get(stringWidgetGroupButtonEntry.getKey()).backgroundShader
            );
        }
        visibleWidget.setValue("group");
    }
    public void refreshText() {
        richText.setRootSpan(span);
    }
}
