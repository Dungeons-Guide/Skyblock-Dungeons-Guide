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

package kr.syeyoung.dungeonsguide.launcher.guiv2.xml;

import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.data.ParserElement;

/**
 * This class is for converting xml elements to widgets, to be used by XMLWidget
 *
 * type parameter R should be kept.
 */
public interface ParsedWidgetConverter<W extends Widget, R extends Widget & ImportingWidget> {
    /**
     * Converts xml element into a Widget
     * xmlWidget is the widget that triggered reading the xml file
     * Element ofc has all the props
     * It's the converter's job to instantiate the widget and bind to appropriate variables in XMLWidget
     * ^^ yes, I love dependency injection-like stuff
     *
     *
     * @param rootWidget
     * @param element
     * @return
     */
    W convert(R rootWidget, ParserElement element);
}
