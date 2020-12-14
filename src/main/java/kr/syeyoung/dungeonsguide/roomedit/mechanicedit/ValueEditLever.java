package kr.syeyoung.dungeonsguide.roomedit.mechanicedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonLever;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.Collections;

public class ValueEditLever extends MPanel implements ValueEdit<DungeonLever> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set
    private DungeonLever dungeonLever;

    private MLabel label;
    private MValue<OffsetPoint> value;
    private MTextField preRequisite;
    private MLabelAndElement preRequisite2;

    public ValueEditLever(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonLever = (DungeonLever) parameter2.getNewData();


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
        preRequisite2.setBounds(new Rectangle(0,40,bounds.width,20));
        add(preRequisite2);
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,bounds.width, 20));
        value.setBounds(new Rectangle(0,20,bounds.width, 20));
        preRequisite2.setBounds(new Rectangle(0,40,bounds.width,20));
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

    public static class Generator implements ValueEditCreator<ValueEditLever> {

        @Override
        public ValueEditLever createValueEdit(Parameter parameter) {
            return new ValueEditLever(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonLever();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((DungeonLever)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
