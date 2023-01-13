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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class W3CBackedParser implements Parser {
    public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static {
        factory.setIgnoringComments(true);
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        factory.setExpandEntityReferences(false);
        factory.setCoalescing(false);
    }


    private final InputStream inputStream;
    private final Element rootElement;

    public W3CBackedParser(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        this.inputStream = inputStream;

        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        NodeList nodeList = document.getChildNodes();
        Element semiRoot = null;
        for (int i = 0; i < nodeList.getLength(); i++)
            if (nodeList.item(i) instanceof Element) {
                semiRoot = (Element) nodeList.item(i);
                break;
            }
        rootElement = semiRoot;
    }

    @Override
    public ParserElement getRootNode() {
        return new W3CBackedParserElement(rootElement);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
