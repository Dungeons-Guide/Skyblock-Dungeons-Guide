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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext;

import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.CompiledTextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ITextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ParentDelegatingTextStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TextSpan {
    private ITextStyle textStyle;
    private String text;
    private List<TextSpan> children = new ArrayList<>();
    public TextSpan(ITextStyle textStyle, String text) {
        this.textStyle = textStyle;
        this.text = text;
    }

    public void addChild(TextSpan textSpan) {
        if (textSpan.textStyle instanceof ParentDelegatingTextStyle)
            ((ParentDelegatingTextStyle) textSpan.textStyle).setParent(textStyle);
        children.add(textSpan);
    }

    public void flattenTextSpan(Consumer<FlatTextSpan> appender) {
        appender.accept(new FlatTextSpan(new CompiledTextStyle(textStyle), text.toCharArray()));
        children.forEach(a -> a.flattenTextSpan(appender));
    }
}
