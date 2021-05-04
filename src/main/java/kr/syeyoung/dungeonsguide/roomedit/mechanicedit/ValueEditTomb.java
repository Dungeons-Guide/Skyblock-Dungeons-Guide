package kr.syeyoung.dungeonsguide.roomedit.mechanicedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonTomb;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.utils.TextUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class ValueEditTomb extends MPanel implements ValueEdit<DungeonTomb> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set
    private final DungeonTomb dungeonTomb;

    private final MLabel label;
    private final MValue<OffsetPointSet> value;
    private final MTextField preRequisite;
    private final MLabelAndElement preRequisite2;

    public ValueEditTomb(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonTomb = (DungeonTomb) parameter2.getNewData();


        label = new MLabel();
        label.setText("Tomb Points");
        label.setAlignment(MLabel.Alignment.LEFT);
        add(label);

        value = new MValue(dungeonTomb.getSecretPoint(), Collections.emptyList());
        add(value);

        preRequisite = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonTomb.setPreRequisite(Arrays.asList(str.split(",")));
            }
        };
        preRequisite.setText(TextUtils.join(dungeonTomb.getPreRequisite(), ","));
        preRequisite2 = new MLabelAndElement("Req.",preRequisite);
        preRequisite2.setBounds(new Rectangle(0,40,getBounds().width,20));
        add(preRequisite2);
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,getBounds().width, 20));
        value.setBounds(new Rectangle(0,20,getBounds().width, 20));
        preRequisite2.setBounds(new Rectangle(0,40,getBounds().width,20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        dungeonTomb.highlight(new Color(0,255,255,50), parameter.getName(), EditingContext.getEditingContext().getRoom(), partialTicks);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditTomb> {

        @Override
        public ValueEditTomb createValueEdit(Parameter parameter) {
            return new ValueEditTomb(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonTomb();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((DungeonTomb)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
