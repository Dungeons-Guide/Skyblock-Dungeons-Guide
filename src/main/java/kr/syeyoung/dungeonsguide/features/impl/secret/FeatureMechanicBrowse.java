package kr.syeyoung.dungeonsguide.features.impl.secret;

import com.google.common.collect.Lists;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.GuiClickListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPreRenderListener;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class FeatureMechanicBrowse extends GuiFeature implements GuiPreRenderListener, GuiClickListener, WorldRenderListener {

    public FeatureMechanicBrowse() {
        super("Secret","Mechanic(Secret) Browser", "Browse and Pathfind secrets and mechanics in the current room", "secret.mechanicbrowse", false, 100, 300);
        parameters.put("linecolor", new FeatureParameter<Color>("linecolor", "Color", "Color of Pathfind line", Color.green, "color"));
    }

    public Color getColor() {
        return this.<Color>getParameter("linecolor").getValue();
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawHUD(float partialTicks) {
        if (Minecraft.getMinecraft().currentScreen != null && !(Minecraft.getMinecraft().currentScreen instanceof GuiGuiLocationConfig
                || Minecraft.getMinecraft().currentScreen instanceof GuiConfig
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonRoomEdit
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonAddSet
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonParameterEdit
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonValueEdit
                || Minecraft.getMinecraft().currentScreen instanceof GuiContainer
                || Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu)) return;
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        Rectangle feature = getFeatureRect().getRectangle();
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
        fr.drawString("Open any gui to browse", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    @Override
    public void drawDemo(float partialTicks) {
        Rectangle feature = getFeatureRect().getRectangle();
        FontRenderer fr = getFontRenderer();

        Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, feature.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        fr.drawString("Open any gui to browse", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    @Override
    public void onGuiPreRender(GuiScreenEvent.DrawScreenEvent.Pre rendered) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiGuiLocationConfig
        || Minecraft.getMinecraft().currentScreen instanceof GuiConfig
        || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonRoomEdit
        || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonAddSet
        || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonParameterEdit
        || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonValueEdit
        || Minecraft.getMinecraft().currentScreen instanceof GuiContainer
                || Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu) return;

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
        GlStateManager.pushMatrix();

        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        Rectangle feature = getFeatureRect().getRectangle();
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
        GlStateManager.translate(0, fr.FONT_HEIGHT + 4, 0);
        Gui.drawRect(0, 0, feature.width, feature.height - fr.FONT_HEIGHT - 4, 0xFF444444);
        Gui.drawRect(1, 1, feature.width - 1,feature.height - fr.FONT_HEIGHT - 5, 0xFF262626);
        clip(new ScaledResolution(Minecraft.getMinecraft()), feature.x, feature.y + fr.FONT_HEIGHT + 5, feature.width , feature.height - fr.FONT_HEIGHT - 6);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GlStateManager.translate(0, -dy, 0);

        GlStateManager.pushMatrix();
        GlStateManager.translate(2,2, 0);
        setupMechanics();
        for (int i = 0; i < sortedMechanics.size(); i++) {
            Object obj = sortedMechanics.get(i);
            if (selected == i) {
                Gui.drawRect(-1, i * fr.FONT_HEIGHT, feature.width - 3, i * fr.FONT_HEIGHT + fr.FONT_HEIGHT - 1, 0xFF444444);
            } else if (new Rectangle(feature.x, feature.y + fr.FONT_HEIGHT + 6 - dy + i * fr.FONT_HEIGHT, feature.width, fr.FONT_HEIGHT).contains(mouseX, mouseY)) {
                Gui.drawRect(-1, i * fr.FONT_HEIGHT, feature.width - 3, i * fr.FONT_HEIGHT + fr.FONT_HEIGHT - 1, 0xFF555555);
            }
            if (obj instanceof DungeonMechanic) {
                String name = sortedMechanicsName.get(i);
                fr.drawString(name, 3, i * fr.FONT_HEIGHT, 0xFFFFFF00);
                fr.drawString(" ("+ ((DungeonMechanic) obj).getCurrentState(dungeonRoom) +", "+
                        (((DungeonMechanic) obj).getRepresentingPoint(dungeonRoom) != null ?
                                String.format("%.1f", MathHelper.sqrt_double(((DungeonMechanic) obj).getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                        +"m)",fr.getStringWidth(name) + 3, i * fr.FONT_HEIGHT, 0xFFAAAAAA);
            } else if ("$SPECIAL-CANCEL".equals(obj)) {
                fr.drawString("Cancel Current", 3, i * fr.FONT_HEIGHT, 0xFF00FFFF);
            } else {
                Gui.drawRect(-1, i * fr.FONT_HEIGHT, feature.width - 3, i * fr.FONT_HEIGHT + fr.FONT_HEIGHT - 1, 0xFF444444);
                fr.drawString((String)obj, 3, i * fr.FONT_HEIGHT, 0xFFEEEEEE);
            }
        }
        GlStateManager.popMatrix();;

        if (selected != -1) {
            clip(new ScaledResolution(Minecraft.getMinecraft()), feature.x + feature.width, feature.y + fr.FONT_HEIGHT + 5, feature.width , feature.height - fr.FONT_HEIGHT - 6);
            GlStateManager.translate(feature.width, selected * fr.FONT_HEIGHT, 0);
            Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT * possibleStates.size() + 4, 0xFF444444);
            Gui.drawRect(-1, 1, feature.width - 1, fr.FONT_HEIGHT  * possibleStates.size() + 3, 0xFF262626);
            GlStateManager.translate(2,2, 0);

            Point popupStart = new Point(feature.x + feature.width, (selected + 1) * fr.FONT_HEIGHT  +6 + feature.y - dy + 2);
            for (int i = 0; i < possibleStates.size(); i++) {
                if (new Rectangle(feature.x + feature.width, popupStart.y + i * fr.FONT_HEIGHT, feature.width, fr.FONT_HEIGHT).contains(mouseX, mouseY)) {
                    Gui.drawRect(-2, i * fr.FONT_HEIGHT, feature.width - 3, i * fr.FONT_HEIGHT + fr.FONT_HEIGHT - 1, 0xFF555555);
                }
                fr.drawString(possibleStates.get(i), 0, i * fr.FONT_HEIGHT, 0xFFFFFFFF);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();
    }

    private void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    private int dy = 0;
    private int selected = -1;
    private int selectedState = -1;
    private List<String> possibleStates = new ArrayList<String>();
    private List<Object> sortedMechanics = new ArrayList<Object>();
    private List<String> sortedMechanicsName = new ArrayList<String>();
    private void setupMechanics() {
        sortedMechanics.clear();
        sortedMechanicsName.clear();

        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        sortedMechanics.add("$SPECIAL-CANCEL");
        sortedMechanicsName.add("");

        boolean found = false;
        for (Map.Entry<String, DungeonMechanic> value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonFairySoul) {
                if (!found) {
                    sortedMechanics.add("Fairy Souls");
                    sortedMechanicsName.add("");
                    found = true;
                }
                sortedMechanics.add(value.getValue());
                sortedMechanicsName.add(value.getKey());
            }
        }
        found = false;
        for (Map.Entry<String, DungeonMechanic> value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonSecret) {
                if (!found) {
                    sortedMechanics.add("Secrets");
                    sortedMechanicsName.add("");
                    found = true;
                }
                sortedMechanics.add(value.getValue());
                sortedMechanicsName.add(value.getKey());
            }
        }
        found = false;
        for (Map.Entry<String, DungeonMechanic> value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonTomb) {
                if (!found) {
                    sortedMechanics.add("Crypts");
                    sortedMechanicsName.add("");
                    found = true;
                }
                sortedMechanics.add(value.getValue());
                sortedMechanicsName.add(value.getKey());
            }
        }
        found = false;
        for (Map.Entry<String, DungeonMechanic> value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonNPC) {
                if (!found) {
                    sortedMechanics.add("NPC");
                    sortedMechanicsName.add("");
                    found = true;
                }
                sortedMechanics.add(value.getValue());
                sortedMechanicsName.add(value.getKey());
            }
        }
        found = false;
        for (Map.Entry<String, DungeonMechanic> value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonJournal) {
                if (!found) {
                    sortedMechanics.add("Journals");
                    sortedMechanicsName.add("");
                    found = true;
                }
                sortedMechanics.add(value.getValue());
                sortedMechanicsName.add(value.getKey());
            }
        }
        found = false;
        for (Map.Entry<String, DungeonMechanic> value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonRoomDoor){
                if (!found) {
                    sortedMechanics.add("Gates");
                    sortedMechanicsName.add("");
                    found = true;
                }
                sortedMechanics.add(value.getValue());
                sortedMechanicsName.add(value.getKey());
            }
        }
        found = false;
        for (Map.Entry<String, DungeonMechanic> value : ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).getDungeonRoom().getMechanics().entrySet()) {
            if (value.getValue() instanceof DungeonDoor || value.getValue() instanceof DungeonBreakableWall || value.getValue() instanceof DungeonLever
            || value.getValue() instanceof DungeonOnewayDoor || value.getValue() instanceof DungeonOnewayLever || value.getValue() instanceof DungeonPressurePlate) {
                if (!found) {
                    sortedMechanics.add("ETC");
                    sortedMechanicsName.add("");
                    found = true;
                }
                sortedMechanics.add(value.getValue());
                sortedMechanicsName.add(value.getKey());
            }
        }
    }

    @Override
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!isEnabled()) return;
        if (Minecraft.getMinecraft().currentScreen instanceof GuiGuiLocationConfig
                || Minecraft.getMinecraft().currentScreen instanceof GuiConfig
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonRoomEdit
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonAddSet
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonParameterEdit
                || Minecraft.getMinecraft().currentScreen instanceof GuiDungeonValueEdit
                || Minecraft.getMinecraft().currentScreen instanceof GuiContainer
                || Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu) return;


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

        Rectangle feature = getFeatureRect().getRectangle();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        Point popupStart = new Point(feature.x + feature.width, (selected + 1) * fr.FONT_HEIGHT  +6 + feature.y - dy);
        if (feature.contains(mouseX, mouseY)) {
            mouseInputEvent.setCanceled(true);

            int wheel = Mouse.getDWheel();
            if (wheel > 0) dy  -= fr.FONT_HEIGHT;
            else if (wheel < 0) dy += fr.FONT_HEIGHT;

            if (-dy + sortedMechanics.size() * fr.FONT_HEIGHT < feature.height - fr.FONT_HEIGHT - 6) dy = -(feature.height - fr.FONT_HEIGHT - 6) + sortedMechanics.size() * fr.FONT_HEIGHT;
            if (dy < 0) dy = 0;


            if (Mouse.getEventButton() != -1) {
                int yDiff = mouseY + dy - feature.y - fr.FONT_HEIGHT - 6;
                selected = yDiff / fr.FONT_HEIGHT;

                if (selected < 0) selected = -1;
                if (selected >= sortedMechanics.size()) selected = -1;
                if (selected == -1) {
                    possibleStates.clear();
                } else if (sortedMechanics.get(selected) instanceof DungeonMechanic){
                    possibleStates = Lists.newArrayList(((DungeonMechanic) sortedMechanics.get(selected)).getPossibleStates(dungeonRoom));
                } else if ("$SPECIAL-CANCEL".equals(sortedMechanics.get(selected))) {
                    ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).cancel();
                    possibleStates.clear();
                    selected = -1;
                    selectedState = -1;
                } else {
                    possibleStates.clear();
                    selected = -1;
                    selectedState = -1;
                }
            }
        } else if (selected != -1 && sortedMechanics.get(selected) instanceof DungeonMechanic &&
                new Rectangle(popupStart, new Dimension(feature.width, ((DungeonMechanic) sortedMechanics.get(selected)).getPossibleStates(dungeonRoom).size() * fr.FONT_HEIGHT)).contains(mouseX, mouseY)) {
            mouseInputEvent.setCanceled(true);
            if (Mouse.getEventButton() != -1) {
                int yDiff = mouseY - popupStart.y - 2;
                int preSelectedState = yDiff / fr.FONT_HEIGHT ;
                if (preSelectedState < 0) preSelectedState = -1;
                if (preSelectedState >= possibleStates.size()) preSelectedState =
                        possibleStates.size() - 1;

                if (preSelectedState == selectedState && preSelectedState != -1) {
                    ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).pathfind(sortedMechanicsName.get(selected),
                            possibleStates.get(selectedState));
                    selected = -1;
                    selectedState = -1;
                    possibleStates.clear();
                }
                selectedState = preSelectedState;
            }
        } else if (Mouse.getEventButton() != -1){
            possibleStates.clear();
            selectedState = -1;
            selected = -1;
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        if (selected != -1) {
            if (sortedMechanics.size() <= selected) return;;
            ((DungeonMechanic)sortedMechanics.get(selected)).highlight(new Color(0,255,255,50), sortedMechanicsName.get(selected) +" ("+(((DungeonMechanic)
            sortedMechanics.get(selected)).getRepresentingPoint(dungeonRoom) != null ?
                    String.format("%.1f", MathHelper.sqrt_double(((DungeonMechanic) sortedMechanics.get(selected)).getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                    +"m)", dungeonRoom, partialTicks);
        }
    }
}
