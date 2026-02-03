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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.world.UBlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class FeatureCompareRoom extends SimpleFeature {

    public FeatureCompareRoom() {
        super("Debug", "Compare", "Toggles compare mode", "debug.compare", false);

        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to toggle", Keyboard.KEY_R, TCKeybind.INSTANCE));
    }
    public boolean toggleCompareStatus = false;

    @DGEventHandler
    public void onKeybindPress(KeyBindPressedEvent keyBindPressedEvent) {
        if (keyBindPressedEvent.getKey() == this.<Integer>getParameter("key").getValue() && isEnabled()) {
            toggleCompareStatus = !toggleCompareStatus;
            try {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §fToggled Compare to §e"+(toggleCompareStatus ? "on":"off"));
            } catch (Exception ignored) {}
        }
    }


    @DGEventHandler
    public void onWorldRenderLast(RenderWorldLastEvent event) {
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
        if (toggleCompareStatus) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return;
        DungeonRoomScaffoldParser scaffoldParser = context.getScaffoldParser();
        if (scaffoldParser == null) return;
        UPlayerSelf thePlayer = ModAPI.getAPI().getPlayer();
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());

        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return;
        if (dungeonRoom.getDungeonRoomInfo() == null) return;

        if ( dungeonRoom.getDungeonRoomInfo().hasSchematic()) {
            OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new VectorI3D(0,0,0));
            for (VectorI3D allInBox : VectorI3D.getAllInBox(dungeonRoom.getRoomBounds().getMin().add(0, -60, 0), dungeonRoom.getRoomBounds().getMax().add(0, 180, 0))) {
                offsetPoint.setPosInWorld(dungeonRoom, allInBox);
                UBlockState blockState = dungeonRoom.getDungeonRoomInfo().getBlock(offsetPoint, dungeonRoom.getRoomMatcher().getRotation());
                if (blockState != dungeonRoom.getRoomWorld().getBlockStateAt(allInBox)) {
                    RenderUtils.highlightBlock(allInBox, new Color(0x70FF0000,true), event.partialTicks, false);
                    Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                    float partialTicks = event.partialTicks;
                    RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);
                    GlStateManager.translate(allInBox.getX(), allInBox.getY(), allInBox.getZ());
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                    GlStateManager.translate(0.5f,0.5f,0.5f);
                    GlStateManager.disableLighting();
                    GlStateManager.enableAlpha();
                    GlStateManager.enableDepth();
                    GlStateManager.depthMask(true);
//                        GlStateManager.disableDepth();
//                        GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();

                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
                    vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);
                    BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
//                        GlStateManager.color(1.0f,1.0f,1.0f,0.1f);
                    blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().theWorld,
                            blockrendererdispatcher.getBlockModelShapes().getModelForState((IBlockState) blockState.getIBlockState()),
                            (IBlockState) blockState.getIBlockState(), new BlockPos(0,0,0), vertexBuffer, false);
                    tessellator.draw();

                    GlStateManager.enableLighting();
                    GlStateManager.popMatrix();
                }
            }
        }
    }

}
