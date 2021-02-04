package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.gui.elements.MLabelAndElement;
import kr.syeyoung.dungeonsguide.gui.elements.MStringSelectionButton;

import java.awt.*;
import java.util.Arrays;

public class ValueEditBoolean extends MPanel implements ValueEdit<Boolean> {
    private Parameter parameter;


    public ValueEditBoolean(Parameter parameter2) {
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
            boolean newData = (Boolean) parameter.getNewData();
            final MStringSelectionButton textField = new MStringSelectionButton(Arrays.asList(new String[] {"true", "false"}), Boolean.toString(newData));
            textField.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    parameter.setNewData(Boolean.valueOf(textField.getSelected()));
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
    public void renderWorld(float partialTicks) {

    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditBoolean> {

        @Override
        public ValueEditBoolean createValueEdit(Parameter parameter) {
            return new ValueEditBoolean(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return true;
        }

        @Override
        public Object cloneObj(Object object) {
            return object;
        }
    }
}
