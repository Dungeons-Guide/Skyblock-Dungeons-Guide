package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.MButton;
import kr.syeyoung.dungeonsguide.roomedit.elements.MOffsetPoint;
import kr.syeyoung.dungeonsguide.roomedit.elements.MParameter;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ValueEditOffsetPointSet extends MPanel implements ValueEdit<OffsetPointSet> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set

    private MPanel scroll;
    @Getter
    private List<MPanel> MParameters = new ArrayList<MPanel>();

    private MButton add, addSet;

    public void delete(OffsetPoint offsetPoint) {
        ((OffsetPointSet)parameter.getNewData()).getOffsetPointList().remove(offsetPoint);
        Iterator<MPanel> iterator = MParameters.iterator();
        while (iterator.hasNext()) {
            MOffsetPoint panel = (MOffsetPoint) iterator.next();
            if (panel.getData() == offsetPoint) {
                iterator.remove();
                break;
            }
        }

    }

    public ValueEditOffsetPointSet(final Parameter parameter2) {
        this.parameter = parameter2;
        {
            scroll = new MPanel() {
                private int offsetY = 0;

                @Override
                public List<MPanel> getChildComponents() {
                    return MParameters;
                }

                @Override
                public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
                    int heights = 0;
                    for (MPanel panel:getChildComponents()) {
                        panel.setPosition(new Point(0, -offsetY + heights));
                        heights += panel.getBounds().height;
                    }
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

                @Override
                public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
                    if (new Rectangle(new Point(0,0), getSize()).contains(relMouseX0, relMouseY0)) {
                        if (scrollAmount >0) offsetY += 20;
                        else if (scrollAmount < 0) offsetY -= 20;
                        if (offsetY <0) offsetY = 0;
                    }
                }
            };
            scroll.setBounds(new Rectangle(0,0,bounds.width, bounds.height-20));
            add(scroll);
        }

        {
            add = new MButton() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            add.setText("Add");
            add.setBackgroundColor(Color.green);
            add.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    OffsetPoint offsetPoint = new OffsetPoint(EditingContext.getEditingContext().getRoom(), Minecraft.getMinecraft().thePlayer.getPosition());
                    MOffsetPoint mOffsetPoint;
                    MParameters.add(mOffsetPoint = new MOffsetPoint(ValueEditOffsetPointSet.this, offsetPoint));
                    ((OffsetPointSet)parameter.getNewData()).getOffsetPointList().add(offsetPoint);
                    mOffsetPoint.setSize(new Dimension(bounds.width, 20));
                }
            });

            addSet = new MButton(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(parentWidth / 2,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            addSet.setText("Add Set");
            addSet.setBackgroundColor(Color.cyan);
            addSet.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    //open gui
                }
            });
            add(add);
            add(addSet);
        }
        for (OffsetPoint offsetPoint : ((OffsetPointSet)parameter.getNewData()).getOffsetPointList()) {
            MParameters.add(new MOffsetPoint(this, offsetPoint));
        }
    }

    @Override
    public void onBoundsUpdate() {
        scroll.setBounds(new Rectangle(0,0,bounds.width, bounds.height-20));
        add.setBounds(new Rectangle(0,bounds.height-20,bounds.width / 2, 20));
        addSet.setBounds(new Rectangle(bounds.width / 2,bounds.height-20,bounds.width / 2, 20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        for (OffsetPoint offsetPoint :((OffsetPointSet)parameter.getNewData()).getOffsetPointList()) {
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(0,255,255,50), partialTicks);
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditOffsetPointSet> {

        @Override
        public ValueEditOffsetPointSet createValueEdit(Parameter parameter) {
            return new ValueEditOffsetPointSet(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new OffsetPointSet();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((OffsetPointSet)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
