package kr.syeyoung.dungeonsguide.roomedit.panes;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.ArrayUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.UUID;

public class RoomMatchDisplayPane extends MPanel {

    private int offsetX = 0;
    private int offsetY = 0;

    private DungeonRoom dungeonRoom;

    private int[][] currentBlocks, targetBlocks;
    public RoomMatchDisplayPane(DungeonRoom dungeonRoom, UUID uid, int rotation) {
        this.dungeonRoom = dungeonRoom;

        currentBlocks = dungeonRoom.getDungeonRoomInfo().getBlocks();
        targetBlocks = DungeonRoomInfoRegistry.getByUUID(uid).getBlocks();
        for (int i = 0; i < rotation; i++)
            targetBlocks = ArrayUtils.rotateCounterClockwise(targetBlocks);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        int height = Math.max(currentBlocks.length, targetBlocks.length);
        int width = Math.max(currentBlocks[0].length, targetBlocks[0].length);

        // draw Axis;
        Gui.drawRect(0,0,10,10,0x77777777);
        clip(sr, clip.x + 10, clip.y, clip.width - 10, 10);
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0x77777777);
        for (int x = 0; x < width; x++) {
            fr.drawString(x+"", x * 16 +10 + offsetX, 0, 0xFFFFFFFF);
        }
        clip(sr, clip.x, clip.y +10, 10, clip.height-10);
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0x77777777);
        for (int z = 0; z < height; z++) {
            fr.drawString(z+"", 2, z * 16 + 10 + offsetY, 0xFFFFFFFF);
        }

        int hoverX = (relMousex0 - offsetX - 10) / 16;
        int hoverY = (relMousey0 - offsetY - 10) / 16;
        // draw Content
        clip(sr, clip.x + 10, clip.y +10, clip.width - 10, clip.height - 10);
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int data1;
                try { data1 = currentBlocks[z][x]; } catch (Exception e) {
                    data1 = -2;
                }
                int data2;
                try { data2 = targetBlocks[z][x]; } catch (Exception e) {
                    data2 = -2;
                }

                if (z == hoverY && x == hoverX) {
                    Gui.drawRect(x *16 +10+offsetX, z *16 +10 + offsetY, x *16 +26 +offsetX, z *16 +26 + offsetY, 0xAA505050);
                }

                if (data1 == data2) drawItemStack(new ItemStack(Item.getItemFromBlock(Block.getBlockById(data1)), 1), x * 16 +10 + offsetX, z *16 +10 + offsetY);
                else if (data2 == -1 || data1 == -1) {
                    drawItemStack(new ItemStack(Item.getItemFromBlock(Block.getBlockById(data1 == -1 ? data2 : data1)), 1), x * 16 +10 + offsetX, z *16 +10 + offsetY);
                    fr.drawString("S", x *16 +10 + offsetX, z *16 +10 + offsetY,0xFFFFFF00);
                } else {
                    fr.drawString("N", x *16 +10 + offsetX, z *16 +10 + offsetY,0xFFFF0000);
                }
                if (z == hoverY && x == hoverX) {
                    FeatureEditPane.drawHoveringText(Arrays.asList(new String[] {"Expected "+data2 +" But found "+data1}), relMousex0, relMousey0, fr);
                }
            }
        }

    }
    private void drawItemStack(ItemStack stack, int x, int y)
    {
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10,parentHeight-10));
    }

    private int lastX;
    private int lastY;
    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        lastX = absMouseX;
        lastY = absMouseY;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        int dX = absMouseX - lastX;
        int dY = absMouseY - lastY;
        offsetX += dX;
        offsetY += dY;
        lastX = absMouseX;
        lastY = absMouseY;
    }
}
