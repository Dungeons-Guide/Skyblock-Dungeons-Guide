package kr.syeyoung.dungeonsguide.roomedit.gui;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.panes.DynamicEditor;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
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

public class GuiDungeonParameterEdit extends GuiScreen {

    private MPanel mainPanel = new MPanel();

    private Parameter parameter;
    private DungeonRoom dungeonRoom;

    private String classSelection;

    private MPanel currentValueEdit;

    private MButton save;
    private MButton delete;

    @Getter
    private ValueEdit valueEdit;

    public GuiDungeonParameterEdit(final MParameter parameter2, final DynamicEditor processorParameterEditPane) {
        dungeonRoom = EditingContext.getEditingContext().getRoom();
        mainPanel.setBackgroundColor(new Color(17, 17, 17, 179));
        this.parameter = parameter2.getParameter();
        {
            MTextField mTextField = new MTextField() {
                @Override
                public void edit(String str) {
                    parameter.setName(str);
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("Name", mTextField);

            mTextField.setText(parameter.getName());
            mLabelAndElement.setBounds(new Rectangle(0,0,200, 20));
            mainPanel.add(mLabelAndElement);
        }
        {
            classSelection = parameter.getNewData() == null ?"null" : parameter.getNewData().getClass().getName();
            final MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(processorParameterEditPane.allowedClass(), classSelection) {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0, 20, parentWidth,20));
                }

                @Override
                public String selectionToDisplay(String selection) {
                    String[] split = selection.split("\\.");
                    return super.selectionToDisplay(split[split.length - 1]);
                }
            };

            mStringSelectionButton.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    classSelection = mStringSelectionButton.getSelected();
                    updateClassSelection();
                }
            });
            mStringSelectionButton.setBounds(new Rectangle(0,20,150,20));
            mainPanel.add(mStringSelectionButton);
        }
        {
            currentValueEdit = new MPanel(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0, 40, parentWidth,parentHeight - 60));
                }
            };
            mainPanel.add(currentValueEdit);
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
                    processorParameterEditPane.delete(parameter2);
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
        updateClassSelection();
    }

    public void updateClassSelection() {
        currentValueEdit.getChildComponents().clear();

        ValueEditCreator valueEditCreator = ValueEditRegistry.getValueEditMap(classSelection);

        if (!classSelection.equals(parameter.getNewData() == null ?"null" :parameter.getNewData().getClass().getName())) {
            parameter.setNewData(valueEditCreator.createDefaultValue(parameter));
            parameter.setPreviousData(valueEditCreator.cloneObj(parameter.getNewData()));
        }

        MPanel valueEdit = (MPanel) valueEditCreator.createValueEdit(parameter);
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
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();GL11.glDisable(GL11.GL_FOG);
        GlStateManager.color(1,1,1,1);
        GlStateManager.pushAttrib();
        mainPanel.render0(scaledResolution, new Point(0,0), new Rectangle(0,0,scaledResolution.getScaledWidth(),scaledResolution.getScaledHeight()), mouseX, mouseY, mouseX, mouseY, partialTicks);
        GlStateManager.popAttrib();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
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
