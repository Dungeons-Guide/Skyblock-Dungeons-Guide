/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.secret;

import com.google.common.base.Supplier;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MFeatureEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.MParameterEdit;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.RootConfigPanel;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.text.*;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MPassiveLabelAndElement;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MStringSelectionButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MToggleButton;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public class FeatureSoulRoomWarning extends TextHUDFeature implements TickListener {

    public FeatureSoulRoomWarning() {
        super("Dungeon.HUDs","Secret Soul Alert", "Alert if there is an fairy soul in the room", "secret.fairysoulwarn", true, getFontRenderer().getStringWidth("There is a fairy soul in this room!"), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("warning", new AColor(0xFF, 0x69,0x17,255), new AColor(0, 0,0,0), false));

        addParameter("roomuids", new FeatureParameter("roomuids", "Disabled room Names", "Disable for these rooms", new ArrayList<>(), "stringlist"));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        return warning > System.currentTimeMillis();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Collections.singletonList("warning");
    }

    private UUID lastRoomUID = UUID.randomUUID();
    private long warning = 0;

    private static final List<StyledText> text = new ArrayList<StyledText>();
    static {
        text.add(new StyledText("There is a fairy soul in this room!", "warning"));
    }

    @Override
    public List<StyledText> getText() {
        return text;
    }


    @Override
    public void onTick() {
        if (!skyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || !DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) return;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        if (!dungeonRoom.getDungeonRoomInfo().getUuid().equals(lastRoomUID)) {
            for (DungeonMechanic value : dungeonRoom.getMechanics().values()) {
                if (value instanceof DungeonFairySoul)
                    warning = System.currentTimeMillis() + 2500;
            }
            lastRoomUID = dungeonRoom.getDungeonRoomInfo().getUuid();
        }
    }

    @Override
    public String getEditRoute(RootConfigPanel rootConfigPanel) {
        ConfigPanelCreator.map.put("base." + getKey() , new Supplier<MPanel>() {
            @Override
            public MPanel get() {

                MFeatureEdit featureEdit = new MFeatureEdit(FeatureSoulRoomWarning.this, rootConfigPanel);
                featureEdit.addParameterEdit("textStyleNEW", new PanelTextParameterConfig(FeatureSoulRoomWarning.this));

                StyledTextRenderer.Alignment alignment = StyledTextRenderer.Alignment.valueOf(FeatureSoulRoomWarning.this.<String>getParameter("alignment").getValue());
                MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(Arrays.asList("LEFT", "CENTER", "RIGHT"), alignment.name());
                mStringSelectionButton.setOnUpdate(() -> {
                    FeatureSoulRoomWarning.this.<String>getParameter("alignment").setValue(mStringSelectionButton.getSelected());
                });
                featureEdit.addParameterEdit("alignment", new MParameterEdit(FeatureSoulRoomWarning.this, FeatureSoulRoomWarning.this.<String>getParameter("alignment"), rootConfigPanel, mStringSelectionButton, (a) -> false));

                for (FeatureParameter parameter: getParameters()) {
                    if (parameter.getKey().equals("textStylesNEW")) continue;
                    if (parameter.getKey().equals("alignment")) continue;
                    if (parameter.getKey().equals("roomuids")) continue;
                    featureEdit.addParameterEdit(parameter.getKey(), new MParameterEdit(FeatureSoulRoomWarning.this, parameter, rootConfigPanel));
                }
                featureEdit.addParameterEdit("roomuids", new RoomSelectionPanel(FeatureSoulRoomWarning.this.<List<String>>getParameter("roomuids"), (a) -> {
                    for (DungeonMechanic value : a.getMechanics().values()) {
                        if (value instanceof DungeonFairySoul) return true;
                    }
                    return false;
                }) );
                return featureEdit;
            }
        });
        return "base." + getKey() ;
    }

    public static class RoomSelectionPanel extends MPanel {
        FeatureParameter<List<String>> uids;
        private List<MPassiveLabelAndElement> passiveLabelAndElements = new ArrayList<>();
        private List<MToggleButton> toggleButtons = new ArrayList<>();
        private MButton addAll, removeAll;
        public RoomSelectionPanel(FeatureParameter<List<String>> roomuids, Predicate<DungeonRoomInfo> selectableRooms) {
            this.uids = roomuids;
            for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                if (!selectableRooms.test(dungeonRoomInfo)) continue;
                MToggleButton mToggleButton = new MToggleButton();
                mToggleButton.setEnabled(!roomuids.getValue().contains(dungeonRoomInfo.getUuid().toString()));
                mToggleButton.setOnToggle(() -> {
                    if (mToggleButton.isEnabled())
                        roomuids.getValue().remove(dungeonRoomInfo.getUuid().toString());
                    else
                        roomuids.getValue().add(dungeonRoomInfo.getUuid().toString());
                });
                toggleButtons.add(mToggleButton);
                MPassiveLabelAndElement passiveLabelAndElement = new MPassiveLabelAndElement(dungeonRoomInfo.getName(), mToggleButton);
                passiveLabelAndElement.setDivideRatio(0.7);
                passiveLabelAndElements.add(passiveLabelAndElement);
            }
            for (MPassiveLabelAndElement passiveLabelAndElement : passiveLabelAndElements) {
                add(passiveLabelAndElement);
            }
            {
                addAll = new MButton(); addAll.setText("Enable All");
                addAll.setOnActionPerformed(() -> {
                    roomuids.getValue().clear();
                    for (MToggleButton toggleButton : toggleButtons) {
                        toggleButton.setEnabled(true);
                    }
                });
                removeAll = new MButton(); removeAll.setText("Disable All");
                removeAll.setOnActionPerformed(() -> {
                    for (MToggleButton toggleButton : toggleButtons) {
                        toggleButton.setEnabled(false);
                        toggleButton.getOnToggle().run();
                    }
                });
                add(addAll); add(removeAll);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(-1, (int) (20 * Math.ceil(passiveLabelAndElements.size() / 3) + 27));
        }

        @Override
        public void onBoundsUpdate() {
            int xI = 0;
            int y = 22;
            int w3 = (getBounds().width-20) / 3;
            for (MPassiveLabelAndElement passiveLabelAndElement : passiveLabelAndElements) {
                passiveLabelAndElement.setBounds(new Rectangle(5 + xI * (w3+5), y, w3, 20));
                xI ++;
                if (xI == 3) {
                    xI = 0;
                    y += 20;
                }
            }

            addAll.setBounds(new Rectangle(getBounds().width-150, 2, 70, 14));
            removeAll.setBounds(new Rectangle(getBounds().width-75, 2, 70, 14));
        }

        @Override
        public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
            Gui.drawRect(0,0,getBounds().width, getBounds().height,RenderUtils.blendAlpha(0x141414, 0.12f));
            Gui.drawRect(1,18,getBounds().width -1, getBounds().height-1, RenderUtils.blendAlpha(0x141414, 0.15f));
            Gui.drawRect(1,1,getBounds().width-1, 18, RenderUtils.blendAlpha(0x141414, 0.12f));

            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            GlStateManager.pushMatrix();
            GlStateManager.translate(5,5,0);
            GlStateManager.scale(1.0,1.0,0);
            GlStateManager.enableBlend();
            GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            fr.drawString("Enable for these rooms", 0,0, 0xFFFFFFFF);
            GlStateManager.popMatrix();
        }
    }
}
