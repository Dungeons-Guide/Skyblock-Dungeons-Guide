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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.BreakWord;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.RichText;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.styles.ParentDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.utils.MathUtils;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;

public class RoomProcessorRedRoom extends GeneralRoomProcessor {
    public RoomProcessorRedRoom(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        VectorI3D basePt = dungeonRoom.getRoomBounds().getMin().add(dungeonRoom.getRoomBounds().getMax());
        this.basePt = new Vector3D(basePt.getX() / 2.0f, basePt.getY() / 2.0f, basePt.getZ() / 2.0f);
    }

    Vector3D basePt;
    int dir = 0;

    private final RichText richText = new RichText(new TextSpan(
            ParentDelegatingTextStyle.ofDefault(),
            ""
    ), BreakWord.WORD, false, RichText.TextAlign.LEFT);


    @Override
    public void tick() {
        VectorI3D basePt = getDungeonRoom().getRoomBounds().getMin().add(getDungeonRoom().getRoomBounds().getMax());
        this.basePt = new Vector3D(basePt.getX() / 2.0f, basePt.getY() / 2.0f + 4, basePt.getZ() / 2.0f);
        DungeonDoor real = null;
        for (DungeonDoor door : getDungeonRoom().getDoors()) {
            if (door.getType().isExist()) {
                real = door;break;
            }
        }
        if (real != null) {
            OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), real.getPosition());
            offsetPoint = new OffsetPoint(33- offsetPoint.getX(), offsetPoint.getY(), 33 - offsetPoint.getZ());
            VectorI3D opposite =offsetPoint.getBlockPos(getDungeonRoom());
            VectorI3D dir = real.getPosition().subtract(opposite);
            dir = new VectorI3D(MathUtils.clamp_int(dir.getX() / 10, -1, 1), 0, MathUtils.clamp_int(dir.getZ() / 10, -1, 1));

            this.basePt = new Vector3D(opposite.add(dir.getX() * 6 + dir.getZ(), 3, dir.getZ() * 6 - dir.getX()));

            if (dir.getX() > 0) this.dir = 270;
            else if (dir.getX() < 0) this.dir = 90;
            else if (dir.getZ() < 0) this.dir = 0;
            else if (dir.getZ() > 0) this.dir = 180;
            else this.dir = Integer.MIN_VALUE;
        } else {
            dir = Integer.MIN_VALUE;
        }

        if (getDungeonRoom().getDungeonRoomInfo().getProperties().containsKey("warning-pos")) {
            OffsetPoint offsetPoint = (OffsetPoint) getDungeonRoom().getDungeonRoomInfo().getProperties().get("warning-pos");
            Vector3D pos = new Vector3D(offsetPoint.getBlockPos(getDungeonRoom()));
            pos = pos.add(0.5, 0, 0.5);
            String dirStr = (String) getDungeonRoom().getDungeonRoomInfo().getProperties().get("warning-dir");
            EnumFacing dirFacing = EnumFacing.byName(dirStr);
            if (dirFacing != null) {
                int rot = getDungeonRoom().getRoomMatcher().getRotation();
                for (int i = 0; i < 4-rot; i++)
                    dirFacing = dirFacing.rotateY();
                pos = pos.add(dirFacing.getFrontOffsetX() * 0.5, dirFacing.getFrontOffsetY() * 0.5, dirFacing.getFrontOffsetZ() * 0.5);
                dir = (2-dirFacing.getHorizontalIndex())* 90; // don't ask me why
                dirFacing = dirFacing.rotateY();
                pos = pos.add(dirFacing.getFrontOffsetX() * 0.5, dirFacing.getFrontOffsetY() * 0.5, dirFacing.getFrontOffsetZ() * 0.5);

            }

            this.basePt = pos;
        }
    }

    @Override
    public void drawWorld(UWorldRenderContext context, float partialTicks) {
        super.drawWorld(context, partialTicks);
        if (!FeatureRegistry.BOSSFIGHT_WARNING_ON_PORTAL.isEnabled()) return;


//        FeatureWarningOnPortal featureWarningOnPortal = FeatureRegistry.BOSSFIGHT_WARNING_ON_PORTAL; $$ TODO REDROOM
//        {
//            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
//
//            RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);
//            context.translate(basePt.x, basePt.y, basePt.z);
//
//
//            GlStateManager.color(1f, 1f, 1f, 0.5f);
//            if (dir == Integer.MIN_VALUE)
//                context.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
//            else
//                context.rotate(dir, 0.0f, 1.0f, 0.0f);
//            context.scale(-0.05f, -0.05f, 0.05f);
//            GlStateManager.disableLighting();
//            GlStateManager.depthMask(false); GL11.glDisable(GL11.GL_DEPTH_TEST);
//            GlStateManager.disableDepth();
//            GlStateManager.enableBlend();
//            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//
//
//            richText.setRootSpan(featureWarningOnPortal.getText());
//            richText.layout(null, new ConstraintBox(0, Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY));
//            richText.doRender(partialTicks, null, new DomElement() {
//                @Override
//                public Size getSize() {
//                    return new Size(999,999);
//                }
//
//                @Override
//                public Rect getAbsBounds() {
//                    return new Rect(0,0,999,999);
//                }
//            });
//
//            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
//            GlStateManager.depthMask(true);
//            GlStateManager.enableDepth();
//            GlStateManager.popMatrix();
//        }
    }


    public static class Generator implements RoomProcessorGenerator<RoomProcessorRedRoom> {
        @Override
        public RoomProcessorRedRoom createNew(DungeonRoom dungeonRoom) {
            RoomProcessorRedRoom defaultRoomProcessor = new RoomProcessorRedRoom(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
