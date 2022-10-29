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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.boss.FeatureWarningOnPortal;
import kr.syeyoung.dungeonsguide.features.text.StyledTextRenderer;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;

public class RoomProcessorRedRoom extends GeneralRoomProcessor {
    public RoomProcessorRedRoom(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        BlockPos basePt = dungeonRoom.getMin().add(dungeonRoom.getMax());
        this.basePt = new Vec3(basePt.getX() / 2.0f, basePt.getY() / 2.0f, basePt.getZ() / 2.0f);
    }

    Vec3 basePt;
    int dir = 0;

    @Override
    public void tick() {
        BlockPos basePt = getDungeonRoom().getMin().add(getDungeonRoom().getMax());
        this.basePt = new Vec3(basePt.getX() / 2.0f, basePt.getY() / 2.0f + 4, basePt.getZ() / 2.0f);
        DungeonDoor real = null;
        for (DungeonDoor door : getDungeonRoom().getDoors()) {
            if (door.getType().isExist()) {
                real = door;break;
            }
        }
        if (real != null) {
            OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), real.getPosition());
            offsetPoint = new OffsetPoint(33- offsetPoint.getX(), offsetPoint.getY(), 33 - offsetPoint.getZ());
            BlockPos opposite =offsetPoint.getBlockPos(getDungeonRoom());
            BlockPos dir = new BlockPos(real.getPosition().subtract(opposite));
            dir = new BlockPos(MathHelper.clamp_int(dir.getX() / 10, -1, 1), 0, MathHelper.clamp_int(dir.getZ() / 10, -1, 1));

            this.basePt = new Vec3(opposite.add(dir.getX() * 6 + dir.getZ(), 3, dir.getZ() * 6 - dir.getX()));

            if (dir.getX() > 0) this.dir = 270;
            else if (dir.getX() < 0) this.dir = 90;
            else if (dir.getZ() < 0) this.dir = 0;
            else if (dir.getZ() > 0) this.dir = 180;
            else this.dir = Integer.MIN_VALUE;
        } else {
            dir = Integer.MIN_VALUE;
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.getInstance().BOSSFIGHT_WARNING_ON_PORTAL.isEnabled()) return;


        FeatureWarningOnPortal featureWarningOnPortal = FeatureRegistry.getInstance().BOSSFIGHT_WARNING_ON_PORTAL;
        {
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

            Vector3f renderPos = RenderUtils.getRenderPos((float)basePt.xCoord,(float) basePt.yCoord, (float)basePt.zCoord, partialTicks);

            GlStateManager.color(1f, 1f, 1f, 0.5f);
            GlStateManager.pushMatrix();
            GlStateManager.translate(renderPos.x, renderPos.y, renderPos.z);
            if (dir == Integer.MIN_VALUE)
                GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
            else
                GlStateManager.rotate(dir, 0.0f, 1.0f, 0.0f);
            GlStateManager.scale(-0.05f, -0.05f, 0.05f);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false); GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


            StyledTextRenderer.drawTextWithStylesAssociated(featureWarningOnPortal.getText(), 0, 0,0, featureWarningOnPortal.getStylesMap(), StyledTextRenderer.Alignment.LEFT);

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }


    public static class Generator implements RoomProcessorGenerator<RoomProcessorRedRoom> {
        @Override
        public RoomProcessorRedRoom createNew(DungeonRoom dungeonRoom) {
            RoomProcessorRedRoom defaultRoomProcessor = new RoomProcessorRedRoom(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
