package kr.syeyoung.dungeonsguide.mod.guiv2;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.management.AttributeChangeNotification;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class Controller {

    private DomElement element;

    private Map<String, Element> slots = new HashMap<>();

    private final Map<String, BindableAttribute> attributeMap = new HashMap<>();
    private final Map<String, BindableAttribute> attributeMap2 = new HashMap<>();
    private final Map<BindableAttribute, String> binds = new HashMap<>();

    private BindableAttribute<DomElement> ref = new BindableAttribute<>(DomElement.class);

    public Controller(DomElement element) {
        this.element = element;
        ref.setValue(element);

        NodeList list = element.getRepresenting().getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            String value = list.item(i).getAttributes().getNamedItem("slot").getNodeValue();
            slots.put(value, (Element) list.item(i));
        }


        for (Field declaredField : getClass().getDeclaredFields()) {
            if (declaredField.getAnnotation(Export.class) != null) {
                Export export = declaredField.getAnnotation(Export.class);

                if (declaredField.getType() != BindableAttribute.class) throw new IllegalStateException("Export Annotation must be applied on BindableAttribute field.");
                if (!Modifier.isFinal(declaredField.getModifiers())) throw new IllegalStateException("Exported Bindable Attribute must be final ");

                try {
                    attributeMap.put(export.attributeName(), (BindableAttribute) declaredField.get(this));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            if (declaredField.getAnnotation(Bind.class) != null) {
                Bind bind = declaredField.getAnnotation(Bind.class);

                if (declaredField.getType() != BindableAttribute.class) throw new IllegalStateException("Bind Annotation must be applied on BindableAttribute field.");
                if (!Modifier.isFinal(declaredField.getModifiers())) throw new IllegalStateException("Bound Bindable Attribute must be final ");


                try {
                    attributeMap2.put(bind.attributeName(), (BindableAttribute) declaredField.get(this));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        NamedNodeMap attributes = element.getRepresenting().getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            if (attr.getName().startsWith("bind:")) {
                String bindingAttribute = attr.getName().substring(5);

                if (attributeMap.containsKey(bindingAttribute)) {
                    throw new IllegalStateException("Can not bind "+bindingAttribute+" because it is not exported");
                }

                binds.put(attributeMap.get(bindingAttribute), attr.getValue());
            } else if (attributeMap.containsKey(attr.getName())) {
                attributeMap.get(attr.getName()).setValue(somehow(attributeMap.get(attr.getName()).getType(), attr.getValue()));
            }
        }
    }
    
    private static <T> T somehow(Class<T> clazz, String val) {
        if (clazz== float.class) {
            return (T) Float.valueOf(val);
        } else if (clazz== double.class) {
            return (T) Double.valueOf(val);
        } else if (clazz== int.class) {
            return (T) Integer.valueOf(val);
        } else if (clazz== String.class) {
            return (T) val;
        } else if (clazz.isEnum()) {
            for (Object enumConstant : clazz.getEnumConstants()) {
                if (val.equalsIgnoreCase(enumConstant.toString()))
                    return (T) enumConstant;
            }
        } else if (clazz== boolean.class) {
            return (T) Boolean.valueOf(val);
        }
        throw new UnsupportedOperationException("cant convert to "+clazz.getName());
    }

    public final Map<String, BindableAttribute> getExportedAttributes() {
        return attributeMap;
    }

    public final Map<String, BindableAttribute> getBindingAttributes() {
        return attributeMap2;
    }

    public final void loadDom() {
        NodeList nodeList = element.getRepresenting().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            DomElement domElement = DomElementRegistry.createTree((Element)nodeList.item(i));
            element.addElement(domElement);
        }
    }

    public final void loadFile(String location) throws ParserConfigurationException, IOException, SAXException {
        Document document = DomElementRegistry.factory.newDocumentBuilder().parse(location);
        NodeList nodeList = document.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            DomElement domElement = DomElementRegistry.createTree((Element)nodeList.item(i));
            domElement.setComponentParent(element);
            element.addElement(domElement);
        }
    }

    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {}
    public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {}
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {}
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {}
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {}
    public void keyReleased(char typedChar, int keyCode) {}
    public void keyHeld(char typedChar, int keyCode) {}
    public void keyPressed(char typedChar, int keyCode) {}
    public void mouseExited(int absMouseX, int absMouseY, int relMouseX, int relMouseY) {}
    public void mouseEntered(int absMouseX, int absMouseY, int relMouseX, int relMouseY) {}

    public void onMount() {
        for (Map.Entry<BindableAttribute, String> bindableAttributeStringEntry : binds.entrySet()) {
            bindableAttributeStringEntry.getKey().linkTo(
                element.getComponentParent().getController().getBindingAttributes().get(bindableAttributeStringEntry.getValue())
            );
        }
    }
    public void onUnmount() {
        for (Map.Entry<BindableAttribute, String> bindableAttributeStringEntry : binds.entrySet()) {
            bindableAttributeStringEntry.getKey().unlinkAll();
        }
    }
}
