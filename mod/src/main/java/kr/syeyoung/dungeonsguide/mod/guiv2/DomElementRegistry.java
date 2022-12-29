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
