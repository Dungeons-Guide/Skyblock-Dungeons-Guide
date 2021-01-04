package kr.syeyoung.dungeonsguide.roomedit.elements;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditOffsetPointSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
public class MValue<T> extends MPanel {
    @Getter
    private T data;
    private ValueEditOffsetPointSet valueEditOffsetPointSet;
    private MLabel dataLab;

    @Getter @Setter
    private Color hover = Color.gray;

    private List<MPanel> addons;

    public MValue(final T parameter, List<MPanel> addons) {
        this.addons = addons;
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
        if (hover != null && new Rectangle(new Point(0,0),getBounds().getSize()).contains(relMousex0, relMousey0)) {
            Gui.drawRect(0,0,getBounds().width, getBounds().height, hover.getRGB());
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (this.getBounds().x > -20 && lastAbsClip.contains(absMouseX, absMouseY)) {
            EditingContext.getEditingContext().openGui(new GuiDungeonValueEdit(data, addons));
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, getBounds().height));
        dataLab.setBounds(new Rectangle(0,0,parentWidth, getBounds().height));
    }

    @Override
    public void onBoundsUpdate() {
        dataLab.setBounds(new Rectangle(0,0,getBounds().width, getBounds().height));
    }
}
