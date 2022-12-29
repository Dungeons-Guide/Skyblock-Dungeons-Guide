/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.Map;

public class DomElementRegistry {
    public interface DomElementCreator {
        public Layouter createLayout(DomElement domElement);
        public Renderer createRenderer(DomElement domElement);
        public Controller createController(DomElement domElement);
    }

    private static Map<String, DomElementCreator> creatorMap = new HashMap<>();

    static {

    }

    public static DomElement createTree(Element element) {
        DomElementCreator creator = creatorMap.get(element.getTagName());

        DomElement domElement = new DomElement();
        domElement.setRepresenting(element);
        domElement.setController(creator.createController(domElement));
        domElement.setLayouter(creator.createLayout(domElement));
        domElement.setRenderer(creator.createRenderer(domElement));

        return domElement;
    }


    public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

}
