package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MEditableAColor;
import kr.syeyoung.dungeonsguide.gui.elements.MToggleButton;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MParameter extends MPanel {
    private final MLabel label;

    @Getter
    private final AbstractFeature feature;
    @Getter
    private final FeatureParameter parameter;

    private final List<MPanel> addons =  new ArrayList<MPanel>();

    @Getter @Setter
    private Color hover;

    private PanelDefaultParameterConfig config;

    private final MLabel label2;
    public MParameter(AbstractFeature abstractFeature, final FeatureParameter parameter, final GuiConfig config2) {
        this.config = config;
        this.parameter = parameter;
        this.feature = abstractFeature;

        this.add(this.label = new MLabel());
        this.label.setText(parameter.getName());

        {
            if (parameter.getValue_type().equalsIgnoreCase("boolean")) {
                final MToggleButton button = new MToggleButton();
                button.setOnToggle(new Runnable() {
                    @Override
                    public void run() {
                        parameter.setValue(button.isEnabled());
                        label2.setText(parameter.getValue().toString());
                    }
                });
                button.setEnabled((Boolean) parameter.getValue());
                addons.add(button);
                add(button);
            } else if (parameter.getValue_type().equalsIgnoreCase("acolor")) {
                final MEditableAColor button = new MEditableAColor();
                button.setEnableEdit(true);
                button.setOnUpdate(new Runnable() {
                    @Override
                    public void run() {
                        parameter.setValue(button.getColor());
                        label2.setText(parameter.getValue().toString());
                    }
                });
                button.setColor((AColor) parameter.getValue());
                addons.add(button);
                add(button);
            } else {
                MButton button = new MButton();
                button.setText("Edit");
                button.setOnActionPerformed(new Runnable() {
                    @Override
                    public void run() {
                        final GuiParameterValueEdit guiParameterValueEdit = new GuiParameterValueEdit(parameter.getValue(), config2);
                        guiParameterValueEdit.setOnUpdate(new Runnable() {
                            @Override
                            public void run() {
                                Parameter parameter1 = guiParameterValueEdit.getParameter();
                                parameter.setValue(parameter1.getNewData());
                                label2.setText(parameter.getValue().toString());
                            }
                        });
                        Minecraft.getMinecraft().displayGuiScreen(guiParameterValueEdit);
                    }
                });
                addons.add(button);
                add(button);
            }
        }
        {
            MLabel button = new MLabel();
            button.setText(parameter.getValue().toString());
            addons.add(button);
            add(button);
            label2 = button;
        }
        setSize(new Dimension(100,20));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (hover != null && new Rectangle(new Point(0,0),getBounds().getSize()).contains(relMousex0, relMousey0)) {
            Gui.drawRect(0,0,getBounds().width, getBounds().height, hover.getRGB());
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, getBounds().height));
    }

    @Override
    public void onBoundsUpdate() {
        int x = getBounds().width - 50;
        for (MPanel panel : addons) {
            panel.setBounds(new Rectangle(x, 3, 50, getBounds().height - 6));
        }
        label2.setBounds(new Rectangle(x/2,0,x/2,getBounds().height));
        label.setBounds(new Rectangle(0,0,x/2, getBounds().height));
    }
}
