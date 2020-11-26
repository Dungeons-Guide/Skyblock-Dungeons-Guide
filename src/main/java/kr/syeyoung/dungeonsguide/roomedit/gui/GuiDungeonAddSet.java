package kr.syeyoung.dungeonsguide.roomedit.gui;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditOffsetPointSet;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiDungeonAddSet extends GuiScreen {

    private MPanel mainPanel = new MPanel();

    private ValueEditOffsetPointSet valueEditOffsetPointSet;

    private MButton add;
    private MButton back;


    private OffsetPoint start;
    private OffsetPoint end;

    public void onWorldRender(float partialTicks) {
        for (OffsetPoint pos:getBlockPoses()) {
            RenderUtils.highlightBlock(pos.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(0,255,255,50), partialTicks);
        }
        RenderUtils.highlightBlock(start.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(255,0,0,50), partialTicks);
        RenderUtils.highlightBlock(end.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(0,255,0,50), partialTicks);
    }

    public List<OffsetPoint> getBlockPoses() {
        int minX = Math.min(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxX = Math.max(start.getX(), end.getX());
        int maxY = Math.max(start.getY(), end.getY());
        int maxZ = Math.max(start.getZ(), end.getZ());

        List<OffsetPoint> offsetPoints = new ArrayList<OffsetPoint>();
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <=maxX; x++) {
                for (int y = maxY; y >= minY; y --) {
                    offsetPoints.add(new OffsetPoint(x,y,z));
                }
            }
        }
        return offsetPoints;
    }

    public void add() {
        valueEditOffsetPointSet.addAll(getBlockPoses());
    }

    public GuiDungeonAddSet(final ValueEditOffsetPointSet processorParameterEditPane) {
        this.valueEditOffsetPointSet = processorParameterEditPane;
        mainPanel.setBackgroundColor(new Color(17, 17, 17, 179));
        {
            start = new OffsetPoint(EditingContext.getEditingContext().getRoom(), Minecraft.getMinecraft().thePlayer.getPosition());
            end = new OffsetPoint(EditingContext.getEditingContext().getRoom(), Minecraft.getMinecraft().thePlayer.getPosition());
        }
        {
            MOffsetPoint mOffsetPoint = new MOffsetPoint(null,start);
            mOffsetPoint.setBounds(new Rectangle(0,0,150,20));
            mainPanel.add(mOffsetPoint);
            MOffsetPoint mOffsetPoint2 = new MOffsetPoint(null,end);
            mOffsetPoint2.setBounds(new Rectangle(0,20,150,20));
            mainPanel.add(mOffsetPoint2);
        }
        {
            add = new MButton() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            add.setText("Add");
            add.setBackgroundColor(Color.red);
            add.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    add();
                    EditingContext.getEditingContext().goBack();
                }
            });

            back = new MButton(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(parentWidth / 2,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            back.setText("Go back");
            back.setBackgroundColor(Color.green);
            back.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    EditingContext.getEditingContext().goBack();
                }
            });
            mainPanel.add(add);
            mainPanel.add(back);
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
