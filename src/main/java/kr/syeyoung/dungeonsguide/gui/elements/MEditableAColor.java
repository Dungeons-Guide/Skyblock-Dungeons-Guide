package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import java.awt.*;

@AllArgsConstructor
@NoArgsConstructor
public class MEditableAColor extends MPanel {
    @Getter
    @Setter
    private AColor color = new AColor(0xffffffff, true);
    @Getter
    @Setter
    private Dimension size = new Dimension(20,15);

    @Getter
    @Setter
    private boolean enableEdit = false;

    @Getter
    @Setter
    private Runnable onUpdate;

    public void setEnableEdit(boolean enableEdit) {
        this.enableEdit = enableEdit;
        if (portable != null && !enableEdit) {
            remove(portable);
            portable = null;
        }
    }

    private MPortableColorEdit portable = null;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Rectangle rectangle = getBounds();

        int x = (rectangle.width - getSize().width) / 2;
        int y = (rectangle.height - getSize().height) / 2;
        Gui.drawRect(x,y,x+getSize().width,y+getSize().height, RenderUtils.getColorAt(absMousex - relMousex0, absMousey -  relMousey0, color));

        Gui.drawRect(x,y,x+getSize().width,y+1, 0xff333333);
        Gui.drawRect(x,y,x+1,y+getSize().height, 0xff333333);
        Gui.drawRect(x+getSize().width-1,y,x+getSize().width,y+getSize().height, 0xff333333);
        Gui.drawRect(x,y+getSize().height-1,x+getSize().width,y+getSize().height, 0xff333333);
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!enableEdit) return;
        if (lastAbsClip.contains(absMouseX, absMouseY) && portable == null) {
            portable = new MPortableColorEdit() {
                @Override
                public void update2() {
                    MEditableAColor.this.color = portable.getColor();
                    if (onUpdate != null)
                        onUpdate.run();
                }
            };
            portable.setColor(color);
            System.out.println(relMouseX+ "  " +relMouseY);
            portable.setBounds(new Rectangle(relMouseX, relMouseY, 100, 60));
            add(portable);
        } else if (portable != null && !portable.getBounds().contains(relMouseX, relMouseY)) {
            remove(portable);
            portable = null;
        }
    }
}
