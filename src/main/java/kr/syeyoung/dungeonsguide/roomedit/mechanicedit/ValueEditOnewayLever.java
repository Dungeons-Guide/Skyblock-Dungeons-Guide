package kr.syeyoung.dungeonsguide.roomedit.mechanicedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonOnewayLever;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.gui.elements.MLabelAndElement;
import kr.syeyoung.dungeonsguide.gui.elements.MTextField;
import kr.syeyoung.dungeonsguide.gui.elements.MValue;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.utils.TextUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class ValueEditOnewayLever extends MPanel implements ValueEdit<DungeonOnewayLever> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set
    private DungeonOnewayLever dungeonLever;

    private MLabel label;
    private MValue<OffsetPoint> value;
    private MTextField preRequisite;
    private MLabelAndElement preRequisite2;
    private MTextField target;
    private MLabelAndElement target2;


    public ValueEditOnewayLever(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonLever = (DungeonOnewayLever) parameter2.getNewData();


        label = new MLabel();
        label.setText("Secret Point");
        label.setAlignment(MLabel.Alignment.LEFT);
        add(label);

        value = new MValue(dungeonLever.getLeverPoint(), Collections.emptyList());
        add(value);

        preRequisite = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonLever.setPreRequisite(Arrays.asList(str.split(",")));
            }
        };
        preRequisite.setText(TextUtils.join(dungeonLever.getPreRequisite(), ","));
        preRequisite2 = new MLabelAndElement("Req.",preRequisite);
        preRequisite2.setBounds(new Rectangle(0,40,getBounds().width,20));
        add(preRequisite2);


        target = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonLever.setTriggering(str);
            }
        };
        target.setText(dungeonLever.getTriggering());
        target2 = new MLabelAndElement("Target",target);
        target2.setBounds(new Rectangle(0,60,getBounds().width,20));
        add(target2);
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,getBounds().width, 20));
        value.setBounds(new Rectangle(0,20,getBounds().width, 20));
        preRequisite2.setBounds(new Rectangle(0,40,getBounds().width,20));
        target2.setBounds(new Rectangle(0,60,getBounds().width,20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        dungeonLever.highlight(new Color(0,255,0,50), parameter.getName(), EditingContext.getEditingContext().getRoom(), partialTicks);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditOnewayLever> {

        @Override
        public ValueEditOnewayLever createValueEdit(Parameter parameter) {
            return new ValueEditOnewayLever(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonOnewayLever();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((DungeonOnewayLever)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
