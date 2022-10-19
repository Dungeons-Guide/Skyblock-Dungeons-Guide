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

package kr.syeyoung.dungeonsguide.features.impl.secret.mechanicbrowser;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MList;
import kr.syeyoung.dungeonsguide.gui.elements.MPanelScaledGUI;
import kr.syeyoung.dungeonsguide.gui.elements.MScrollablePanel;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PanelMechanicBrowser extends MPanelScaledGUI {
    private FeatureMechanicBrowse feature;
    private MScrollablePanel scrollablePanel;
    private MList mList;
    private MechanicBrowserTooltip mechanicBrowserTooltip;

    public PanelMechanicBrowser(FeatureMechanicBrowse mechanicBrowse) {
        this.feature = mechanicBrowse;
        this.scrollablePanel = new MScrollablePanel(1);
        add(this.scrollablePanel);
        scrollablePanel.getScrollBarY().setWidth(0);
        mList = new MList() {
            @Override
            public void resize(int parentWidth, int parentHeight) {
                setBounds(new Rectangle(0,0,parentWidth,parentHeight));
                Dimension prefSize = getPreferredSize();
                int hei = prefSize.height;
                setBounds(new Rectangle(0,0,parentWidth,hei));
                realignChildren();
            }
        };
        mList.setDrawLine(false); mList.setGap(0);
        scrollablePanel.add(mList);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        toggleTooltip(openGUI());

        Optional<DungeonRoom> dungeonRoomOpt = Optional.ofNullable(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext())
                .map(DungeonContext::getMapProcessor).map(a->a.worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition()))
                .map(a -> DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext().getRoomMapper().get(a));

        DungeonRoom dungeonRoom = dungeonRoomOpt.orElse(null);
        renderTick(dungeonRoom);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();


        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        Dimension effectiveDim = getEffectiveDimension();

        Gui.drawRect(0, 0, effectiveDim.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, effectiveDim.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        if (grp.getPath("MECH-BROWSER") == null)
            fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        else {
            ActionRoute route = grp.getPath("MECH-BROWSER");
            fr.drawString(route.getMechanic()+" -> "+route.getState(), fr.getStringWidth("Selected: ") + 2,2, 0xFFFFFF00);
        }
        fr.drawString("Open Chat to Select Secrets", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);

        if (!openGUI()) return;

        Gui.drawRect(0, fr.FONT_HEIGHT + 4, effectiveDim.width, effectiveDim.height, 0xFF444444);
        Gui.drawRect(1, fr.FONT_HEIGHT + 5, effectiveDim.width - 1,effectiveDim.height - 1, 0xFF262626);
    }

    private UUID lastRoomUID = null;
    public void renderTick(DungeonRoom dungeonRoom) {
        if (dungeonRoom == null && lastRoomUID != null) {
            lastRoomUID = null;
            for (MPanel childComponent : mList.getChildComponents()) {
                mList.remove(childComponent);
            }
            if (mechanicBrowserTooltip != null) {
                mechanicBrowserTooltip.close();
                mechanicBrowserTooltip = null;
            }
            selectedID = null;
        } else if (dungeonRoom != null && lastRoomUID != dungeonRoom.getDungeonRoomInfo().getUuid()) {
            lastRoomUID = dungeonRoom.getDungeonRoomInfo().getUuid();
            // SETUP THINGS.
            for (MPanel childComponent : mList.getChildComponents()) {
                mList.remove(childComponent);
            }
            if (mechanicBrowserTooltip != null) {
                mechanicBrowserTooltip.close();
                mechanicBrowserTooltip = null;
            }
            selectedID = null;
            mList.add(new MechanicBrowserElement(() -> "§bCancel Current", false, (pt, me) -> cancel(pt)));

            boolean found = false;
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonFairySoul) {
                    if (!found) {
                        mList.add(new MechanicBrowserElement(() -> "Fairy Soul", true, null));
                        found = true;
                    }
                    mList.add(new MechanicBrowserElement(() -> value.getKey()+" §7("+ value.getValue().getCurrentState(dungeonRoom) +", "+
                            (value.getValue().getRepresentingPoint(dungeonRoom) != null ?
                                    String.format("%.1f", MathHelper.sqrt_double(value.getValue().getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                            +"m)", false, (me, pt) -> onElementClick(value.getKey(), value.getValue(), pt, me)));
                }
            }
            found = false;
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonSecret) {
                    if (!found) {
                        mList.add(new MechanicBrowserElement(() -> "Secrets", true, null));
                        found = true;
                    }
                    mList.add(new MechanicBrowserElement(() -> value.getKey()+" §7("+ value.getValue().getCurrentState(dungeonRoom) +", "+
                            (value.getValue().getRepresentingPoint(dungeonRoom) != null ?
                                    String.format("%.1f", MathHelper.sqrt_double(value.getValue().getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                            +"m)", false, (me, pt) -> onElementClick(value.getKey(), value.getValue(), pt, me)));
                }
            }
            found = false;
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonTomb) {
                    if (!found) {
                        mList.add(new MechanicBrowserElement(() -> "Crypts", true, null));
                        found = true;
                    }
                    mList.add(new MechanicBrowserElement(() -> value.getKey()+" §7("+ value.getValue().getCurrentState(dungeonRoom) +", "+
                            (value.getValue().getRepresentingPoint(dungeonRoom) != null ?
                                    String.format("%.1f", MathHelper.sqrt_double(value.getValue().getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                            +"m)", false, (me, pt) -> onElementClick(value.getKey(), value.getValue(), pt, me)));
                }
            }
            found = false;
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonNPC) {
                    if (!found) {
                        mList.add(new MechanicBrowserElement(() -> "NPC", true, null));
                        found = true;
                    }
                    mList.add(new MechanicBrowserElement(() -> value.getKey()+" §7("+ value.getValue().getCurrentState(dungeonRoom) +", "+
                            (value.getValue().getRepresentingPoint(dungeonRoom) != null ?
                                    String.format("%.1f", MathHelper.sqrt_double(value.getValue().getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                            +"m)", false, (me, pt) -> onElementClick(value.getKey(), value.getValue(), pt, me)));
                }
            }
            found = false;
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonJournal) {
                    if (!found) {
                        mList.add(new MechanicBrowserElement(() -> "Journals", true, null));
                        found = true;
                    }
                    mList.add(new MechanicBrowserElement(() -> value.getKey()+" §7("+ value.getValue().getCurrentState(dungeonRoom) +", "+
                            (value.getValue().getRepresentingPoint(dungeonRoom) != null ?
                                    String.format("%.1f", MathHelper.sqrt_double(value.getValue().getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                            +"m)", false, (me, pt) -> onElementClick(value.getKey(), value.getValue(), pt, me)));
                }
            }
            found = false;
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonRoomDoor){
                    if (!found) {
                        mList.add(new MechanicBrowserElement(() -> "Gates", true, null));
                        found = true;
                    }
                    mList.add(new MechanicBrowserElement(() -> value.getKey()+" §7("+ value.getValue().getCurrentState(dungeonRoom) +", "+
                            (value.getValue().getRepresentingPoint(dungeonRoom) != null ?
                                    String.format("%.1f", MathHelper.sqrt_double(value.getValue().getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                            +"m)", false, (me, pt) -> onElementClick(value.getKey(), value.getValue(), pt, me)));
                }
            }
            found = false;
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonDoor || value.getValue() instanceof DungeonBreakableWall || value.getValue() instanceof DungeonLever
                || value.getValue() instanceof DungeonOnewayDoor || value.getValue() instanceof DungeonOnewayLever || value.getValue() instanceof DungeonPressurePlate) {
                    if (!found) {
                        mList.add(new MechanicBrowserElement(() -> "ETC", true, null));
                        found = true;
                    }
                    mList.add(new MechanicBrowserElement(() -> value.getKey()+" §7("+ value.getValue().getCurrentState(dungeonRoom) +", "+
                            (value.getValue().getRepresentingPoint(dungeonRoom) != null ?
                                    String.format("%.1f", MathHelper.sqrt_double(value.getValue().getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                            +"m)", false, (me, pt) -> onElementClick(value.getKey(), value.getValue(), pt, me)));
                }
            }

            scrollablePanel.evalulateContentArea();

        }
    }

    private int latestTooltipDY;
    @Getter
    private String selectedID = null;
    public void onElementClick(String id, DungeonMechanic dungeonMechanic, Point pt, MechanicBrowserElement mechanicBrowserElement) {
        Optional<DungeonRoom> dungeonRoomOpt = Optional.ofNullable(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext())
                .map(DungeonContext::getMapProcessor).map(a->a.worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition()))
                .map(a -> DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext().getRoomMapper().get(a));
        selectedID = id;

        DungeonRoom dungeonRoom = dungeonRoomOpt.orElse(null);
        if (dungeonRoom == null) return;
        DungeonMechanic dungeonMechanic1 = dungeonRoom.getMechanics().get(id);
        if (dungeonMechanic1 != dungeonMechanic) return;
        Set<String> states = dungeonMechanic1.getPossibleStates(dungeonRoom);


        if (mechanicBrowserTooltip != null) {
            mechanicBrowserTooltip.close();
        }

        latestTooltipDY = (int) (pt.y * getScale() - bounds.y - 1);

        mechanicBrowserTooltip = new MechanicBrowserTooltip();
        for (String state : states) {
            mechanicBrowserTooltip.getMList().add(new MechanicBrowserElement(() -> state, false, (m2, pt2) -> {
                if (dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)
                    ((GeneralRoomProcessor)dungeonRoom.getRoomProcessor()).pathfind("MECH-BROWSER", id, state, FeatureRegistry.SECRET_LINE_PROPERTIES_SECRET_BROWSER.getRouteProperties());
//                mechanicBrowserTooltip.close();
//                mechanicBrowserTooltip = null;
            }));
        }
        mechanicBrowserTooltip.setScale(getScale());
        Dimension prefSize = mechanicBrowserTooltip.getPreferredSize();
        mechanicBrowserTooltip.setBounds(new Rectangle(bounds.x +
                (bounds.x > Minecraft.getMinecraft().displayWidth/2 ? -prefSize.width : bounds.width), latestTooltipDY + bounds.y, prefSize.width, prefSize.height));
        mechanicBrowserTooltip.open(this);
    }

    public void cancel(MechanicBrowserElement mechanicBrowserElement) {
        Optional<DungeonRoom> dungeonRoomOpt = Optional.ofNullable(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext())
                .map(DungeonContext::getMapProcessor).map(a->a.worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition()))
                .map(a -> DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext().getRoomMapper().get(a));
        mechanicBrowserElement.setFocused(false);
        if (!dungeonRoomOpt.isPresent()) return;
        DungeonRoom dungeonRoom = dungeonRoomOpt.get();
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        ((GeneralRoomProcessor) dungeonRoom.getRoomProcessor()).cancel("MECH-BROWSER");
    }

    public void toggleTooltip(boolean open) {
        if (mechanicBrowserTooltip != null) {
            if (open) {
                mechanicBrowserTooltip.open(this);
            } else {
                mechanicBrowserTooltip.close();
            }
        }
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        Dimension dimension = getEffectiveDimension();
        int y = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 4;
        scrollablePanel.setBounds(new Rectangle(1,y + 1, dimension.width - 2, dimension.height - y - 2));
        scrollablePanel.evalulateContentArea();
        if (mechanicBrowserTooltip != null) {
            Dimension prefSize = mechanicBrowserTooltip.getPreferredSize();
            mechanicBrowserTooltip.setScale(getScale());
            mechanicBrowserTooltip.setBounds(new Rectangle(bounds.x + (bounds.x > Minecraft.getMinecraft().displayWidth/2 ? -prefSize.width: bounds.width), latestTooltipDY + bounds.y, prefSize.width, prefSize.height));
        }
    }

    public boolean openGUI() {
        return Minecraft.getMinecraft().currentScreen != null
                && Minecraft.getMinecraft().currentScreen instanceof GuiChat && lastRoomUID != null;
    }

    @Override
    public List<MPanel> getChildComponents() {
        return openGUI() ? super.getChildComponents() : Collections.emptyList();
    }

    @Override
    public boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
        selectedID = null;
        if (mechanicBrowserTooltip != null) {
            mechanicBrowserTooltip.close();
            mechanicBrowserTooltip = null;
        }

        return super.mouseClicked0(absMouseX, absMouseY, relMouseX0, relMouseY0, mouseButton);
    }
}
