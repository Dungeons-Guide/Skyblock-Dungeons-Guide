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

import com.sun.org.apache.xerces.internal.impl.xs.opti.AttrImpl;
import com.sun.org.apache.xerces.internal.impl.xs.opti.NamedNodeMapImpl;
import com.sun.org.apache.xml.internal.utils.UnImplNode;
import com.sun.org.apache.xpath.internal.NodeSet;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.view.TestView;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DomElementRegistry {
    public interface DomElementCreator {
        public Layouter createLayout(DomElement domElement);
        public Renderer createRenderer(DomElement domElement);
        public Controller createController(DomElement domElement);
    }

    public static class GeneralDomElementCreator implements DomElementCreator {
        private final Function<DomElement, Layouter> layouterFunction;
        private final Function<DomElement, Renderer> rendererFunction;
        private final Function<DomElement, Controller> controllerFunction;

        public GeneralDomElementCreator(Function<DomElement, Layouter> layouterFunction, Function<DomElement, Renderer> rendererFunction, Function<DomElement, Controller> controllerFunction) {
            this.layouterFunction = layouterFunction;
            this.rendererFunction = rendererFunction;
            this.controllerFunction = controllerFunction;
        }

        @Override
        public Layouter createLayout(DomElement domElement) {
            return layouterFunction.apply(domElement);
        }

        @Override
        public Renderer createRenderer(DomElement domElement) {
            return rendererFunction.apply(domElement);
        }

        @Override
        public Controller createController(DomElement domElement) {
            return controllerFunction.apply(domElement);
        }
    }

    public static class ComponentCreator implements DomElementCreator {
        private final Function<DomElement, Controller> controllerFunction;

        public ComponentCreator(Function<DomElement, Controller> controllerFunction) {
            this.controllerFunction = controllerFunction;
        }

        @Override
        public Layouter createLayout(DomElement domElement) {
            return new SingleChildPassingLayouter(domElement);
        }

        @Override
        public Renderer createRenderer(DomElement domElement) {
            return new SingleChildRenderer(domElement);
        }

        @Override
        public Controller createController(DomElement domElement) {
            return controllerFunction.apply(domElement);
        }
    }
    private static Map<String, DomElementCreator> creatorMap = new HashMap<>();

    static {
        creatorMap.put("stack", Stack.CREATOR);
        creatorMap.put("size", SizedBox.CREATOR);
        creatorMap.put("scaler", Scaler.CREATOR);
        creatorMap.put("row", Row.CREATOR);
        creatorMap.put("padding", Padding.CREATOR);
        creatorMap.put("col", Column.CREATOR);
        creatorMap.put("bgcolor", Background.CREATOR);
        creatorMap.put("flexible", Flexible.CREATOR);
        creatorMap.put("line", Line.CREATOR);
        creatorMap.put("border", Border.CREATOR);

        creatorMap.put("Text", Text.CREATOR);
        creatorMap.put("Placeholder", Placeholder.CREATOR);

        creatorMap.put("PopupManager", PopupMgr.CREATOR);


        creatorMap.put("TestView", TestView.CREATOR); // not needed but ... idk
    }

    private static final class AttributePassingElement extends UnImplNode {
        private Map<String, String> attributes;

        public AttributePassingElement(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        @Override
        public String getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public Attr getAttributeNode(String name) {
            if (!attributes.containsKey(name)) return null;
            return new AttrImpl(null, null, null, name, null, attributes.get(name));
        }

        @Override
        public NamedNodeMap getAttributes() {
            return new NamedNodeMapImpl(
                    attributes.keySet().stream()
                            .map(this::getAttributeNode)
                            .collect(Collectors.toList())
                            .toArray(new Attr[]{})
            );
        }

        @Override
        public NodeList getChildNodes() {
            return new NodeSet();
        }

        @Override
        public String getTagName() {
            return "";
        }
    }

    public static RootDom createView(DomElementCreator creator, Map<String, String> attributes) {
        RootDom domElement = new RootDom();
        domElement.setRepresenting(new AttributePassingElement(attributes));
        domElement.setController(creator.createController(domElement));
        domElement.setLayouter(creator.createLayout(domElement));
        domElement.setRenderer(creator.createRenderer(domElement));

        return domElement;
    }
    public static DomElement createTree(Element element) {
        return createTree(element, null);
    }
    public static DomElement createTree(Element element, DomElement parent) {
        DomElementCreator creator = creatorMap.get(element.getTagName());

        DomElement domElement = new DomElement(parent);
        domElement.setRepresenting(element);
        domElement.setController(creator.createController(domElement));
        domElement.setLayouter(creator.createLayout(domElement));
        domElement.setRenderer(creator.createRenderer(domElement));

        return domElement;
    }


    public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static {
        factory.setIgnoringComments(true);
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        factory.setExpandEntityReferences(false);
        factory.setCoalescing(false);
    }
}
