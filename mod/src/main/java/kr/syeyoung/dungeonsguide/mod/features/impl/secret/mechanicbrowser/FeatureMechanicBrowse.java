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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.GuiFeature;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MFloatSelectionButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MPassiveLabelAndElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FeatureMechanicBrowse extends GuiFeature {

    private OverlayWidget lastOpen;
    private OverlayWidget widget;
    private WidgetMechanicBrowser mechanicBrowser;

    public FeatureMechanicBrowse() {
        super("Dungeon.Secrets.Secret Browser","Secret Browser", "Browse and Pathfind secrets and mechanics in the current room", "secret.mechanicbrowse", false, 100, 300);
        addParameter("scale", new FeatureParameter<Float>("scale", "Scale", "Scale", 1.0f, "float"));
    }

    public double getScale() {
        return this.<Float>getParameter("scale").getValue();
    }



    @Override
    public void drawDemo(float partialTicks) {
        double scale = FeatureMechanicBrowse.this.<Float>getParameter("scale").getValue();
        GlStateManager.scale(scale, scale, 1.0);

        Dimension bigDim = getFeatureRect().getRectangleNoScale().getSize();
        Dimension effectiveDim = new Dimension((int) (bigDim.width / scale),(int)( bigDim.height / scale));

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        Gui.drawRect(0, 0, effectiveDim.width, fr.FONT_HEIGHT + 4, 0xFF444444);
        Gui.drawRect(1, 1, effectiveDim.width - 1, fr.FONT_HEIGHT + 3, 0xFF262626);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Selected: ", 2,2, 0xFFAAAAAA);
        fr.drawString("Nothing", fr.getStringWidth("Selected: ") + 2,2, 0xFFAA0000);
        fr.drawString("Open Chat to Select Secrets", 2, fr.FONT_HEIGHT + 5, 0xFFAAAAAA);
    }

    @Override
    public void drawHUD(float partialTicks) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || !DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();

        double scale = FeatureMechanicBrowse.this.<Float>getParameter("scale").getValue();
        GlStateManager.scale(scale, scale, 1.0);

        Dimension bigDim = getFeatureRect().getRectangleNoScale().getSize();
        Dimension effectiveDim = new Dimension((int) (bigDim.width / scale),(int)( bigDim.height / scale));

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
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
    }


    @DGEventHandler
    public void drawWorld(RenderWorldLastEvent event) {
        float partialTicks = event.partialTicks;
        
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || !DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        String id = mechanicBrowser.getSelectedId();
        if (id != null) {
            Optional.ofNullable(dungeonRoom.getMechanics().get(mechanicBrowser.getSelectedId()))
                    .ifPresent(a -> {
                        a.highlight(new Color(0,255,255,50), id +" ("+(
                                dungeonRoom.getMechanics().get(id).getRepresentingPoint(dungeonRoom) != null ?
                                String.format("%.1f", MathHelper.sqrt_double((dungeonRoom.getMechanics().get(id)).getRepresentingPoint(dungeonRoom).getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                                +"m)", dungeonRoom, partialTicks);
                    });
        }
    }
    @Override
    public List<MPanel> getTooltipForEditor(GuiGuiLocationConfig guiGuiLocationConfig) {
        List<MPanel> mPanels = super.getTooltipForEditor(guiGuiLocationConfig);

            mPanels.add(new MPassiveLabelAndElement("Scale", new MFloatSelectionButton(FeatureMechanicBrowse.this.<Float>getParameter("scale").getValue()) {{
                setOnUpdate(() ->{
                    FeatureMechanicBrowse.this.<Float>getParameter("scale").setValue(this.getData());
                }); }
            }));

        return mPanels;
    }

    @DGEventHandler
    public void onOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiChat) {
            if (widget != null)
                OverlayManager.getInstance().addOverlay(lastOpen = widget);
        } else {
            if (lastOpen != null)
                OverlayManager.getInstance().removeOverlay(lastOpen);
            lastOpen = null;
        }
    }


    private UUID lastRoomUid = null;
    @DGEventHandler
    public void onTick(DGTickEvent event) {
        Optional<DungeonRoom> dungeonRoomOpt = Optional.ofNullable(DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext())
                .map(DungeonContext::getMapProcessor).map(a->a.worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition()))
                .map(a -> {
                    return DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getRoomMapper().get(a);
                });
        UUID currentUID = dungeonRoomOpt.map(a -> a.getDungeonRoomInfo().getUuid()).orElse(null);
        // Event-ify above this.

        if (!Objects.equals(lastRoomUid, currentUID)) {
            if (currentUID == null)  {
                mechanicBrowser = null;
                widget = null;
            } else {
                if (lastOpen != null)
                    OverlayManager.getInstance().removeOverlay(lastOpen);
                widget = new OverlayWidget(
                    mechanicBrowser = new WidgetMechanicBrowser(dungeonRoomOpt.get()),
                    OverlayType.OVER_CHAT,
                    () -> {
                        Rectangle loc = getFeatureRect().getRectangleNoScale();
                        return new Rect(loc.x, loc.y, loc.width, loc.height);
                    });
            }
        }
        if (mechanicBrowser != null)
            mechanicBrowser.update();
        lastRoomUid = currentUID;
    }
}
