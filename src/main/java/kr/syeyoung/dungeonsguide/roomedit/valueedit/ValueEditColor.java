package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.MIntegerSelectionButton;
import kr.syeyoung.dungeonsguide.roomedit.elements.MLabel;
import kr.syeyoung.dungeonsguide.roomedit.elements.MLabelAndElement;

import java.awt.*;

public class ValueEditColor extends MPanel implements ValueEdit<Integer> {
    private Parameter parameter;


    @Override
    public void renderWorld(float partialTicks) {

    }
    public ValueEditColor(final Parameter parameter2) {
        this.parameter = parameter2;
        {
            MLabel label = new MLabel() {
                @Override
                public String getText() {
                    return parameter.getPreviousData().toString();
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("Prev",label);
            mLabelAndElement.setBounds(new Rectangle(0,0,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            int newData = (Integer) parameter.getNewData();
            final MIntegerSelectionButton textField = new MIntegerSelectionButton(newData);
            textField.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    parameter.setNewData(textField.getData());
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("New",textField);
            mLabelAndElement.setBounds(new Rectangle(0,20,getBounds().width,20));
            add(mLabelAndElement);
        }
    }

    @Override
    public void onBoundsUpdate() {
        for (MPanel panel :getChildComponents()){
            panel.setSize(new Dimension(getBounds().width, 20));
        }
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditColor> {

        @Override
        public ValueEditColor createValueEdit(Parameter parameter) {
            return new ValueEditColor(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return 0;
        }

        @Override
        public Object cloneObj(Object object) {
            return object;
        }
    }
}
