package kr.syeyoung.dungeonsguide.roomedit;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.elements.MButton;
import kr.syeyoung.dungeonsguide.roomedit.elements.MLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiDungeonRoomEdit extends GuiScreen {

    private MPanel mainPanel = new MPanel();

    private DungeonRoom room;

    public GuiDungeonRoomEdit(DungeonRoom room) {
        this.room = room;

        mainPanel.setBackgroundColor(Color.green);
        MLabel label = new MLabel();
        label.setText("blah blah is great!");
        label.setBackgroundColor(Color.BLACK);
        label.setBounds(new Rectangle(0,0,50,10));
        mainPanel.add(label);

        MButton mButton = new MButton();
        mButton.setText("this is awesome");
        label.setBounds(new Rectangle(30,20,50,10));
        mainPanel.add(label);
    }

    @Override
    public void initGui() {
        // update bounds
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        mainPanel.setBounds(new Rectangle((scaledResolution.getScaledWidth() - 500) / 2, (scaledResolution.getScaledHeight() - 300) / 2,500,300));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        mainPanel.render0(new Rectangle(0,0,scaledResolution.getScaledWidth(),scaledResolution.getScaledHeight()), mouseX, mouseY, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        mainPanel.keyTyped0(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        mainPanel.mouseClicked0(mouseX, mouseY,mouseX,mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        mainPanel.mouseReleased0(mouseX, mouseY,mouseX,mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
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
