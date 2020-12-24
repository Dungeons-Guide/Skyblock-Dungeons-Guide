package kr.syeyoung.dungeonsguide.config;

import kr.syeyoung.dungeonsguide.Config;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.MButton;
import kr.syeyoung.dungeonsguide.roomedit.elements.MParameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.MValue;
import kr.syeyoung.dungeonsguide.roomedit.panes.DynamicEditor;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditRegistry;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.*;
import java.util.List;

public class CategoryEditPane extends MPanel implements DynamicEditor {
    private ConfigCategory category;

    private MButton save;
    private Map<String, MValue> parameters = new HashMap<String, MValue>();

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
                    for (Map.Entry<String,MValue> parameter:parameters.entrySet()) {
                        if (parameter.getValue().getData() != null)
                        category.get(parameter.getKey()).setValue(parameter.getValue().getData().toString());
                    }
                    Config.syncConfig(false);
                }
            });
        }
        {
            for (Map.Entry<String, Property> en : category.entrySet()) {
                ValueEditCreator vec = ValueEditRegistry.getValueEditMap(typeToclass(en.getValue().getType()));

                MValue mParameter = new MValue(getValue(en.getValue()), Collections.emptyList());
                mParameter.setBounds(new Rectangle(0,0,bounds.width,20));
                parameters.put(en.getKey(), mParameter);
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
        ArrayList<MPanel> panels = new ArrayList<MPanel>(parameters.values());
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
