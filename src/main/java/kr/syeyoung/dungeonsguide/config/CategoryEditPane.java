package kr.syeyoung.dungeonsguide.config;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.panes.DynamicEditor;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditRegistry;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CategoryEditPane extends MPanel implements DynamicEditor {
    private ConfigCategory category;

    private MButton save;
    private Map<String, Parameter> parameters = new HashMap<String, Parameter>();
    private List<MLabelAndElement> le = new ArrayList<MLabelAndElement>();

    public CategoryEditPane(ConfigCategory category) {
        this.category = category;
        buildElements();
    }

    public String typeToclass(Property.Type type) {
        switch (type) {
            case STRING:
                return String.class.getName();
            case INTEGER:
                return Integer.class.getName();
            case BOOLEAN:
                return Boolean.class.getName();
            case COLOR:
                return Color.class.getName();
        }
        return "null";
    }

    public void buildElements() {
        {
            save = new MButton();
            save.setText("Save");
            save.setBackgroundColor(Color.green);
            save.setBounds(new Rectangle(0,0,100,20));
            save.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String,Parameter> parameter:parameters.entrySet()) {
                        if (parameter.getValue() != null)
                        category.get(parameter.getKey()).setValue(String.valueOf(parameter.getValue().getNewData()));
                    }
                    Config.syncConfig(false);
                }
            });
        }
        {
            for (Map.Entry<String, Property> en : category.entrySet()) {
                ValueEditCreator vec = ValueEditRegistry.getValueEditMap(typeToclass(en.getValue().getType()));
                final Parameter parameter;
                vec.createDefaultValue(parameter = new Parameter(en.getValue().comment, getValue(en.getValue()), getValue(en.getValue())));

                MPanel element = null;
                if (en.getValue().getType() == Property.Type.STRING) {
                    element = new MTextField() {
                        @Override
                        public void edit(String str) {
                            parameter.setNewData(str);
                        }
                    };
                    ((MTextField)element).setText(en.getValue().getString());
                } else if (en.getValue().getType() == Property.Type.INTEGER) {
                    element = new MIntegerSelectionButton(en.getValue().getInt());
                    final MPanel finalElement = element;
                    ((MIntegerSelectionButton)element).setOnUpdate(new Runnable() {
                        @Override
                        public void run() {
                            parameter.setNewData(((MIntegerSelectionButton) finalElement).getData());
                        }
                    });
                }else if (en.getValue().getType() == Property.Type.BOOLEAN) {
                    element = new MStringSelectionButton(Arrays.asList(new String[] {"on", "off"}), en.getValue().getBoolean() ? "on":"off");
                    final MPanel finalElement1 = element;
                    ((MStringSelectionButton)element).setOnUpdate(new Runnable() {
                        @Override
                        public void run() {
                            parameter.setNewData(((MStringSelectionButton) finalElement1).getSelected().equals("on") ? true : false);
                        }
                    });
                }

                MLabelAndElement labelAndElement = new MLabelAndElement(en.getValue().comment, element);
                labelAndElement.setBounds(new Rectangle(0,0,bounds.width,20));
                parameters.put(en.getKey(), parameter);
                le.add(labelAndElement);
            }
        }
    }

    private Object getValue(Property property) {
        switch(property.getType()) {
            case STRING:
                return property.getString();
            case INTEGER:
                return property.getInt();
            case BOOLEAN:
                return property.getBoolean();
        }
        return null;
    }

    @Override
    public void onBoundsUpdate() {
        for (MPanel panel :getChildComponents()){
            panel.setSize(new Dimension(bounds.width, 20));
        }
    }
    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10,parentHeight-10));
    }


    public void delete(MParameter parameter) {
        parameters.remove(parameter);
    }

    @Override
    public List<String> allowedClass() {
        return ValueEditRegistry.getClassesSupported();
    }


    @Override
    public List<MPanel> getChildComponents() {
        ArrayList<MPanel> panels = new ArrayList<MPanel>(le);
        panels.add(save);
        return panels;
    }

    private int offsetY = 0;
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        int heights = 0;
        for (MPanel panel:getChildComponents()) {
            panel.setPosition(new Point(0, -offsetY + heights));
            heights += panel.getBounds().height;
        }
    }

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (scrollAmount > 0) offsetY -= 20;
        else if (scrollAmount < 0) offsetY += 20;
        if (offsetY < 0) offsetY = 0;
    }
}
