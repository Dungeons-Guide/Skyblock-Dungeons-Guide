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

import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetStyleEdit extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "styles")
    public final BindableAttribute widgets = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute(Column.class);
    @Bind(variableName = "help")
    public final BindableAttribute<Widget> help = new BindableAttribute<>(Widget.class, new WidgetHelp());
    private final WidgetTextStyleConfig config;
    private DefaultingDelegatingTextStyle style;
    public WidgetStyleEdit(WidgetTextStyleConfig config, DefaultingDelegatingTextStyle style) {
        super(new ResourceLocation("dungeonsguide:gui/config/text/styleedit.gui"));
        this.config = config;
        this.style =style;
        DefaultingDelegatingTextStyle curr = style;
        List<Widget> widgetList = new ArrayList<>();
        while (curr != null) {
            WidgetStyleGroup group = new WidgetStyleGroup(this, curr, style, curr == style);
            if (group.anythingUseful() || style == curr)
                widgetList.add(group);
            if (curr.parent == null || !(curr.getParent() instanceof DefaultingDelegatingTextStyle)) break;
            curr = (DefaultingDelegatingTextStyle) curr.getParent();
        }

        widgets.setValue(widgetList);
    }

    public void update() {
        config.refreshText();
        List<Widget> widgets = (List<Widget>) this.widgets.getValue();
        for (Widget widget : widgets) {
            ((WidgetStyleGroup)widget).refresh();
        }
    }

    @On(functionName = "back")
    public void back() {
        config.exitEdit();
    }
}
