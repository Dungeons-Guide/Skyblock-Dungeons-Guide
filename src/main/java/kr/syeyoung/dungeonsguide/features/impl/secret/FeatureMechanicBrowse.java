/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.secret;

import com.google.common.collect.Lists;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.old.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
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
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class FeatureMechanicBrowse extends GuiFeature implements GuiPreRenderListener, GuiClickListener, WorldRenderListener {
    public FeatureMechanicBrowse() {
        super("Dungeon Secret.Secret Pathfind","Mechanic(Secret) Browser", "Browse and Pathfind secrets and mechanics in the current room", "secret.mechanicbrowse", false, 100, 300);
        parameters.put("linecolor2", new FeatureParameter<AColor>("linecolor2", "Color", "Color of Pathfind line", new AColor(0xFF00FF00, true), "acolor"));
        parameters.put("linethickness", new FeatureParameter<Float>("linethickness", "Thickness", "Thickness of Pathfind line", 1.0f, "float"));
        parameters.put("refreshrate", new FeatureParameter<Integer>("refreshrate", "Line Refreshrate", "How many ticks per line refresh?", 10, "integer"));
    }

    public AColor getColor() {
        return this.<AColor>getParameter("linecolor2").getValue();
    }
    public float getThickness() {
        return this.<Float>getParameter("linethickness").getValue();
    }
    public int getRefreshRate() {
        return this.<Integer>getParameter("refreshrate").getValue();
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    private UUID lastRoomUID = null;
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
        if (!dungeonRoom.getDungeonRoomInfo().getUuid().equals(lastRoomUID)) {
            selected = -1;
            selectedState = -1;
            dy = 0;
        }
        lastRoomUID = dungeonRoom.getDungeonRoomInfo().getUuid();

        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        Rectangle feature = getFeatureRect().getRectangle();
        FontRenderer fr = getFontRenderer();

        Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, feature.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        fr.drawString("Open any gui to browse", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    @Override
    public void onGuiPreRender(GuiScreenEvent.DrawScreenEvent.Pre rendered) {
        if (!isEnabled()) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) return;

        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        int width = Minecraft.getMinecraft().displayWidth;
        int height = Minecraft.getMinecraft().displayHeight;
        int mouseX = Mouse.getX() ;
        int mouseY = height - Mouse.getY() - 1;
        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);

        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        Rectangle feature = getFeatureRect().getRectangle();
        FontRenderer fr = getFontRenderer();
        GlStateManager.translate(feature.x, feature.y, 0);
        Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, feature.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
            GlStateManager.enableBlend();
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                fr.drawString((String)obj, 3, i * fr.FONT_HEIGHT, 0xFFEEEEEE);
            }
        }
        GlStateManager.popMatrix();

        if (selected != -1) {

            boolean overFlows = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() < feature.x + 2*feature.width;

            clip(new ScaledResolution(Minecraft.getMinecraft()), overFlows ? feature.x - feature.width : feature.x + feature.width, feature.y + fr.FONT_HEIGHT + 5, feature.width , feature.height - fr.FONT_HEIGHT - 6);
            GlStateManager.translate(overFlows ? - feature.width : feature.width, selected * fr.FONT_HEIGHT, 0);
            Gui.drawRect(0, 0, feature.width, fr.FONT_HEIGHT * possibleStates.size() + 4, 0xFF444444);
            Gui.drawRect(-1, 1, feature.width - 1, fr.FONT_HEIGHT  * possibleStates.size() + 3, 0xFF262626);
            GlStateManager.translate(2,2, 0);

            Point popupStart = new Point(overFlows ? feature.x - feature.width : feature.x + feature.width, (selected + 1) * fr.FONT_HEIGHT  +6 + feature.y - dy + 2);
            for (int i = 0; i < possibleStates.size(); i++) {
                if (new Rectangle(overFlows ? feature.x - feature.width : feature.x + feature.width, popupStart.y + i * fr.FONT_HEIGHT, feature.width, fr.FONT_HEIGHT).contains(mouseX, mouseY)) {
                    Gui.drawRect(-2, i * fr.FONT_HEIGHT, feature.width - 3, i * fr.FONT_HEIGHT + fr.FONT_HEIGHT - 1, 0xFF555555);
                }
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                fr.drawString(possibleStates.get(i), 0, i * fr.FONT_HEIGHT, 0xFFFFFFFF);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    private int dy = 0;
    private int selected = -1;
    private int selectedState = -1;
    private List<String> possibleStates = new ArrayList<String>();
    private final List<Object> sortedMechanics = new ArrayList<Object>();
    private final List<String> sortedMechanicsName = new ArrayList<String>();
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
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) return;


        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        int width = Minecraft.getMinecraft().displayWidth;
        int height = Minecraft.getMinecraft().displayHeight;
        int mouseX = Mouse.getX();
        int mouseY = height - Mouse.getY() - 1;

        Rectangle feature = getFeatureRect().getRectangle();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        boolean overFlows = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth() < feature.x + 2*feature.width;

        Point popupStart = new Point(overFlows ? feature.x - feature.width : feature.x + feature.width, (selected + 1) * fr.FONT_HEIGHT  +6 + feature.y - dy);
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
            if (sortedMechanics.size() <= selected) return;
            ((DungeonMechanic)sortedMechanics.get(selected)).highlight(new Color(0,255,255,50), sortedMechanicsName.get(selected) +" ("+(((DungeonMechanic)
            sortedMechanics.get(selected)).getRepresentingPoint(dungeonRoom) != null ?
                    String.format("%.1f", MathHelper.sqrt_double(((DungeonMechanic) sortedMechanics.get(selected)).getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                    +"m)", dungeonRoom, partialTicks);
        }
    }
}
