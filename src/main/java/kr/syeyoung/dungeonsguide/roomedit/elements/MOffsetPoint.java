package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonOffsetPointEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.roomedit.panes.ProcessorParameterEditPane;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditOffsetPointSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class MOffsetPoint extends MPanel {
    @Getter
    private OffsetPoint data;
    private ValueEditOffsetPointSet valueEditOffsetPointSet;
    private MLabel dataLab;

    @Getter @Setter
    private Color hover = Color.gray;


    public MOffsetPoint(ValueEditOffsetPointSet valueEditOffsetPointSet, final OffsetPoint parameter) {
        this.valueEditOffsetPointSet = valueEditOffsetPointSet;
        this.add(this.dataLab = new MLabel() {
            @Override
            public String getText() {
                return data != null ?data.toString() :"-empty-";
            }
        });
        this.dataLab.setAlignment(MLabel.Alignment.RIGHT);

        this.data = parameter;
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (hover != null && new Rectangle(new Point(0,0),bounds.getSize()).contains(relMousex0, relMousey0)) {
            Gui.drawRect(0,0,bounds.width, bounds.height, hover.getRGB());
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (this.bounds.x > -20 && new Rectangle(new Point(0,0),bounds.getSize()).contains(relMouseX, relMouseY)) {
            EditingContext.getEditingContext().openGui(new GuiDungeonOffsetPointEdit(valueEditOffsetPointSet, data));
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, bounds.height));
        dataLab.setBounds(new Rectangle(0,0,parentWidth, bounds.height));
    }

    @Override
    public void onBoundsUpdate() {
        dataLab.setBounds(new Rectangle(0,0,bounds.width, bounds.height));
    }
}
