package kr.syeyoung.dungeonsguide.roomedit.gui;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.MButton;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditOffsetPointSet;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiDungeonOffsetPointEdit extends GuiScreen {
    private MPanel mainPanel = new MPanel();
    @Getter
    private OffsetPoint offsetPoint;
    private MButton save;
    private MButton delete;

    @Getter
    private ValueEdit valueEdit;

    public GuiDungeonOffsetPointEdit(final ValueEditOffsetPointSet valueEditOffsetPointSet, final OffsetPoint offsetPoint) {
        this.offsetPoint = offsetPoint;
        {
            MPanel thing = (MPanel) ValueEditRegistry.getValueEditMap(OffsetPoint.class.getName()).createValueEdit(new Parameter(null, null, null) {
                @Override
                public Object getPreviousData() {
                    return offsetPoint;
                }

                @Override
                public Object getNewData() {
                    return offsetPoint;
                }

                @Override
                public String getName() {
                    return "";
                }

                @Override
                public void setName(String name) {
                    return;
                }

                @Override
                public void setPreviousData(Object previousData) {
                    return;
                }

                @Override
                public void setNewData(Object newData) {
                    return;
                }
            });
            valueEdit = (ValueEdit) thing;
            MPanel wrapper = new MPanel() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0, 0, parentWidth,parentHeight - 20));
                }
            };
            wrapper.add(thing);
            mainPanel.add(wrapper);
        }
        {
            delete = new MButton() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            delete.setText("Delete");
            delete.setBackgroundColor(Color.red);
            delete.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    valueEditOffsetPointSet.delete(offsetPoint);
                    EditingContext.getEditingContext().goBack();
                }
            });

            save = new MButton(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(parentWidth / 2,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            save.setText("Go back");
            save.setBackgroundColor(Color.green);
            save.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    EditingContext.getEditingContext().goBack();
                }
            });
            mainPanel.add(delete);
            mainPanel.add(save);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        // update bounds
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        mainPanel.setBounds(new Rectangle(10, Math.min((scaledResolution.getScaledHeight() - 300) / 2, scaledResolution.getScaledHeight()),200,300));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glPushMatrix();
        GlStateManager.pushAttrib();
        mainPanel.render0(scaledResolution, new Point(0,0), new Rectangle(0,0,scaledResolution.getScaledWidth(),scaledResolution.getScaledHeight()), mouseX, mouseY, mouseX, mouseY, partialTicks);
        GlStateManager.popAttrib();
        GL11.glPopMatrix();
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        mainPanel.keyTyped0(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        mainPanel.mouseClicked0(mouseX, mouseY,mouseX,mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        mainPanel.mouseReleased0(mouseX, mouseY,mouseX,mouseY, state);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        mainPanel.mouseClickMove0(mouseX,mouseY,mouseX,mouseY,clickedMouseButton,timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            mainPanel.mouseScrolled0(i, j,i,j, wheel);
        }
    }
}
