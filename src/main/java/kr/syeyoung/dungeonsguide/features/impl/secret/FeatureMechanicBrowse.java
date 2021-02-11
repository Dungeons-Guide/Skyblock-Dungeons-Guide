package kr.syeyoung.dungeonsguide.features.impl.secret;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.GuiClickListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPostRenderListener;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class FeatureMechanicBrowse extends GuiFeature implements GuiPostRenderListener, GuiClickListener {

    protected FeatureMechanicBrowse(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super("secret","Mechanic(Secret) Browser", "Browse and Pathfind secrets and mechanics in the current room", "secret.mechanicbrowse", false, 100, 300);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        Rectangle feature = getFeatureRect();
        FontRenderer fr = getFontRenderer();

        Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, feature.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        if (grp.getPath() == null)
            fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        else {
            ActionRoute route = grp.getPath();
            fr.drawString(route.getMechanic()+" -> "+route.getState(), fr.getStringWidth("Selected: ") + 2,2, 0xFFFFFF00);
        }
        fr.drawString("Press T to browse mechanic", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    @Override
    public void drawDemo(float partialTicks) {
        Rectangle feature = getFeatureRect();
        FontRenderer fr = getFontRenderer();

        Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, feature.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        fr.drawString("Press T to browse mechanic", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    @Override
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;


        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        Rectangle feature = getFeatureRect();
        FontRenderer fr = getFontRenderer();
        GlStateManager.translate(feature.x, feature.y, 0);
        Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, feature.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        if (grp.getPath() == null)
            fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        else {
            ActionRoute route = grp.getPath();
            fr.drawString(route.getMechanic()+" -> "+route.getState(), fr.getStringWidth("Selected: ") + 2,2, 0xFFFFFF00);
        }
        clip(new ScaledResolution(Minecraft.getMinecraft()), 0, fr.FONT_HEIGHT + 4, feature.width, fr.FONT_HEIGHT * 10 + 4);

        Gui.drawRect(0, fr.FONT_HEIGHT + 4, feature.width, fr.FONT_HEIGHT * 10 + 4, 0xFF444444);
        Gui.drawRect(1, fr.FONT_HEIGHT + 5, feature.width - 1, fr.FONT_HEIGHT * 10 + 3, 0xFF262626);

    }

    private void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    private int dy = 0;
    private int selected = -1;
    private int selectedState = -1;
    private List<Object> sortedMechanics = new ArrayList<Object>();
    private void setupMechanics() {
        sortedMechanics.clear();

        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        boolean found = false;
        for (DungeonMechanic value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getDungeonRoomInfo().getMechanics().values()) {
            if (value instanceof DungeonDoor) {
                if (!found) {
                    sortedMechanics.add("Fairy Souls");
                    found = true;
                }
                sortedMechanics.add(value);
            }
        }
        found = false;
        for (DungeonMechanic value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getDungeonRoomInfo().getMechanics().values()) {
            if (value instanceof DungeonSecret) {
                if (!found) {
                    sortedMechanics.add("Secrets");
                    found = true;
                }
                sortedMechanics.add(value);
            }
        }
        found = false;
        for (DungeonMechanic value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getDungeonRoomInfo().getMechanics().values()) {
            if (value instanceof DungeonTomb) {
                if (!found) {
                    sortedMechanics.add("Crypts");
                    found = true;
                }
                sortedMechanics.add(value);
            }
        }
        found = false;
        for (DungeonMechanic value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getDungeonRoomInfo().getMechanics().values()) {
            if (value instanceof DungeonDoor || value instanceof DungeonBreakableWall || value instanceof DungeonLever
            || value instanceof DungeonOnewayDoor || value instanceof DungeonOnewayLever || value instanceof DungeonPressurePlate) {
                if (!found) {
                    sortedMechanics.add("Walls & Doors & Levers & Pressure Plates");
                    found = true;
                }
                sortedMechanics.add(value);
            }
        }
    }

    @Override
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseY = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

        Rectangle feature = getFeatureRect();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        Point popupStart = new Point(feature.x + feature.width, (selected - dy) * fr.FONT_HEIGHT + 7 + feature.y);
        if (feature.contains(mouseX, mouseY)) {
            mouseInputEvent.setCanceled(true);

            int wheel = Mouse.getDWheel();
            if (wheel > 0) dy ++;
            else if (wheel < 0) dy --;

            if (Mouse.getEventButton() != -1) {
                int yDiff = mouseY - feature.y - fr.FONT_HEIGHT - 7;
                selected = yDiff / fr.FONT_HEIGHT + dy;
            }
        } else if (sortedMechanics.get(selected) instanceof DungeonMechanic &&
                new Rectangle(popupStart, new Dimension(feature.width, ((DungeonMechanic) sortedMechanics.get(selected)).getPossibleStates(dungeonRoom).size() * fr.FONT_HEIGHT + 6)).contains(mouseX, mouseY)) {
            mouseInputEvent.setCanceled(true);
            if (Mouse.getEventButton() != -1) {
                int yDiff = mouseY - popupStart.y - 2;
                selected = yDiff / fr.FONT_HEIGHT + dy;

            }
        }
    }
}
