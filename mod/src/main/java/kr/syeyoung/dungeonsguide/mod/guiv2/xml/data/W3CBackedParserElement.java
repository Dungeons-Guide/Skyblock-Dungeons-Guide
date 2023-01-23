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

package kr.syeyoung.dungeonsguide.mod.guiv2.xml.data;

import kr.syeyoung.dungeonsguide.mod.guiv2.xml.StringConversions;
import org.w3c.dom.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class W3CBackedParserElement implements ParserElement {
    private final Element backingElement;

    public W3CBackedParserElement(Element element) {
        this.backingElement = element;
    }

    @Override
    public String getNodeName() {
        return backingElement.getTagName();
    }

    @Override
    public String getAttributeValue(String attribute) {
        return backingElement.getAttribute(attribute);
    }

    @Override
    public Set<String> getAttributes() {
        NamedNodeMap nodeList = backingElement.getAttributes();
        Set<String> list = new HashSet<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (!(n instanceof Attr)) continue;
            list.add(((Attr) n).getName());
        }
        return list;
    }

    @Override
    public <T> T getConvertedAttributeValue(Class<T> clazz, String attribute) {
        return StringConversions.convert(clazz, getAttributeValue(attribute));
    }

    @Override
    public List<ParserElement> getChildren() {
        NodeList nodeList = backingElement.getChildNodes();
        List<ParserElement> list = new LinkedList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (!(n instanceof Element)) continue;
            list.add(new W3CBackedParserElement((Element) n));
        }
        return list;
    }
}
