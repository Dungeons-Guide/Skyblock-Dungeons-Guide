package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.MButton;
import kr.syeyoung.dungeonsguide.roomedit.elements.MLabel;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditRegistry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GuiParameterValueEdit extends GuiScreen {

    private MPanel mainPanel = new MPanel() {
        @Override
        public void onBoundsUpdate() {
            save.setBounds(new Rectangle(0 ,getBounds().height - 20, getBounds().width, 20));
        }
    };


    private MPanel currentValueEdit;

    private MButton save;

    @Getter
    private ValueEdit valueEdit;

    private Object editingObj;

    @Getter
    @Setter
    private Runnable onUpdate;
    @Getter
    private Parameter parameter;

    public GuiParameterValueEdit(final Object object, final GuiParameterConfig prev) {
        try {
            this.editingObj = object;
            mainPanel.setBackgroundColor(new Color(17, 17, 17, 179));
            {
                currentValueEdit = new MPanel() {
                    @Override
                    public void resize(int parentWidth, int parentHeight) {
                        setBounds(new Rectangle(5, 5, parentWidth-10, parentHeight - 25));
                    }
                };
                mainPanel.add(currentValueEdit);
            }
            {
                save = new MButton() {
                    @Override
                    public void resize(int parentWidth, int parentHeight) {
                        setBounds(new Rectangle(0, parentHeight - 20, parentWidth, 20));
                    }
                };
                save.setText("Go back");
                save.setBackgroundColor(Color.green);
                save.setOnActionPerformed(new Runnable() {
                    @Override
                    public void run() {
                        onUpdate.run();
                        Minecraft.getMinecraft().displayGuiScreen(prev);
                    }
                });
                mainPanel.add(save);
            }
            updateClassSelection();
        } catch (Exception e){}
    }

    public void updateClassSelection() {
        currentValueEdit.getChildComponents().clear();

        ValueEditCreator valueEditCreator = ValueEditRegistry.getValueEditMap(editingObj == null ?"null":editingObj.getClass().getName());
        System.out.println(valueEditCreator);
        MPanel valueEdit = (MPanel) valueEditCreator.createValueEdit(parameter= new Parameter("", editingObj, editingObj));
        System.out.println(valueEdit);
        if (valueEdit == null) {
            MLabel valueEdit2 = new MLabel() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0, 0, parentWidth,20));
                }
            };
            valueEdit2.setText("No Value Edit");
            valueEdit2.setBounds(new Rectangle(0,0,150,20));
            valueEdit = valueEdit2;
            this.valueEdit = null;
        } else{
            this.valueEdit = (ValueEdit) valueEdit;
        }
        valueEdit.resize0(currentValueEdit.getBounds().width, currentValueEdit.getBounds().height);
        currentValueEdit.add(valueEdit);
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
        try {
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            GL11.glPushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.color(1,1,1,1);
            mainPanel.render0(scaledResolution, new Point(0, 0), new Rectangle(0, 0, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight()), mouseX, mouseY, mouseX, mouseY, partialTicks);
            GL11.glPopMatrix();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {

        try {
            super.keyTyped(typedChar, keyCode);
            mainPanel.keyTyped0(typedChar, keyCode);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        mainPanel.mouseClicked0(mouseX, mouseY,mouseX,mouseY, mouseButton);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        try {
        mainPanel.mouseReleased0(mouseX, mouseY,mouseX,mouseY, state);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        try {
            mainPanel.mouseClickMove0(mouseX,mouseY,mouseX,mouseY,clickedMouseButton,timeSinceLastClick);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            try {
                mainPanel.mouseScrolled0(i, j,i,j, wheel);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
