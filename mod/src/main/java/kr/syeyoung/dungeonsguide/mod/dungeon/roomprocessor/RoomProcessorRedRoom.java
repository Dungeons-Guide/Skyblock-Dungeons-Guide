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

import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonDoor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureWarningOnPortal;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.BreakWord;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.RichText;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.gui.elements.richtext.styles.ParentDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.render.gui.StencilImage;
import kr.syeyoung.dungeonsguide.mod.render.gui.StencilImageTexture;
import kr.syeyoung.dungeonsguide.mod.render.world.DGWorldRenderer;
import kr.syeyoung.dungeonsguide.mod.render.world.state.WorldImageState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RoomProcessorRedRoom extends GeneralRoomProcessor {
    public RoomProcessorRedRoom(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        BlockPos basePt = dungeonRoom.getRoomBounds().getMin().offset(dungeonRoom.getRoomBounds().getMax());
        this.basePt = new Vec3(basePt.getX() / 2.0f, basePt.getY() / 2.0f, basePt.getZ() / 2.0f);
    }

    Vec3 basePt;
    int dir = 0;

    private final RichText richText = new RichText(new TextSpan(
            ParentDelegatingTextStyle.ofDefault(),
            ""
    ), BreakWord.WORD, false, RichText.TextAlign.LEFT);


    @Override
    public void tick() {
        BlockPos basePt = getDungeonRoom().getRoomBounds().getMin().offset(getDungeonRoom().getRoomBounds().getMax());
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
            BlockPos dir = real.getPosition().subtract(opposite);
            dir = new BlockPos(Mth.clamp(dir.getX() / 10, -1, 1), 0, Mth.clamp(dir.getZ() / 10, -1, 1));

            this.basePt = Vec3.atLowerCornerOf(opposite.offset(dir.getX() * 6 + dir.getZ(), 3, dir.getZ() * 6 - dir.getX()));

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
            Vec3 pos = Vec3.atLowerCornerOf(offsetPoint.getBlockPos(getDungeonRoom()));
            pos = pos.add(0.5, 0, 0.5);
            String dirStr = (String) getDungeonRoom().getDungeonRoomInfo().getProperties().get("warning-dir");
            Direction dirFacing = Direction.byName(dirStr);
            if (dirFacing != null) {
                int rot = getDungeonRoom().getRoomMatcher().getRotation();
                for (int i = 0; i < 4-rot; i++)
                    dirFacing = dirFacing.getClockWise();
                pos = pos.add(dirFacing.getStepX() * 0.5, dirFacing.getStepY() * 0.5, dirFacing.getStepZ() * 0.5);
                dir = (2-dirFacing.get2DDataValue())* 90; // don't ask me why
                dirFacing = dirFacing.getClockWise();
                pos = pos.add(dirFacing.getStepX() * 0.5, dirFacing.getStepY() * 0.5, dirFacing.getStepZ() * 0.5);

            }

            this.basePt = pos;
        }
    }

    // the warning is gui rich text, drawn into an image that's put on a quad in the world
    private static final StencilImage warningImage = new StencilImage("Dungeons Guide red room warning");
    private static final Identifier WARNING_TEXTURE = Identifier.fromNamespaceAndPath("dungeonsguide", "dynamic/red_room_warning");
    private static boolean warningTextureRegistered = false;
    // image pixels per rich text unit
    private static final int WARNING_RESOLUTION = 4;

    @Override
    public void drawWorld(RenderWorldEvent event) {
        super.drawWorld(event);
        if (!FeatureRegistry.BOSSFIGHT_WARNING_ON_PORTAL.isEnabled()) return;


        FeatureWarningOnPortal featureWarningOnPortal = FeatureRegistry.BOSSFIGHT_WARNING_ON_PORTAL;
        {
            if (!warningTextureRegistered) {
                Minecraft.getInstance().getTextureManager().register(WARNING_TEXTURE, new StencilImageTexture(warningImage));
                warningTextureRegistered = true;
            }

            richText.setRootSpan(featureWarningOnPortal.getText());
            Size size = richText.layout(null, new ConstraintBox(0, Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY));
            int width = Math.max(1, (int) Math.ceil(size.getWidth() * WARNING_RESOLUTION));
            int height = Math.max(1, (int) Math.ceil(size.getHeight() * WARNING_RESOLUTION));

            RenderingContext context = new RenderingContext(height);
            context.pose().scale(WARNING_RESOLUTION, WARNING_RESOLUTION);
            richText.doRender(event.partialTicks, context, new DomElement() {
                @Override
                public Size getSize() {
                    return new Size(999,999);
                }

                @Override
                public Rect getAbsBounds() {
                    return new Rect(0,0,999,999);
                }
            });

            // 1.8.9: translate to basePt, rotate by dir (or against the view yaw), scale (-0.05, -0.05, 0.05), depth off
            double angle = Math.toRadians(dir == Integer.MIN_VALUE ? -Minecraft.getInstance().gameRenderer.getMainCamera().yRot() : dir);
            double textWidth = width / (double) WARNING_RESOLUTION, textHeight = height / (double) WARNING_RESOLUTION;
            Vec3[] corners = new Vec3[] {
                    warningCorner(angle, 0, 0),
                    warningCorner(angle, 0, textHeight),
                    warningCorner(angle, textWidth, textHeight),
                    warningCorner(angle, textWidth, 0)
            };
            DGWorldRenderer.record(new WorldImageState(warningImage, WARNING_TEXTURE, context.getStates(), width, height, corners, false));
        }
    }

    // a rich text point (u, v) after 1.8.9's rotate(angle around y) and scale(-0.05, -0.05, 0.05), from basePt
    private Vec3 warningCorner(double angle, double u, double v) {
        double x = -0.05 * u;
        return basePt.add(x * Math.cos(angle), -0.05 * v, -x * Math.sin(angle));
    }


    public static class Generator implements RoomProcessorGenerator<RoomProcessorRedRoom> {
        @Override
        public RoomProcessorRedRoom createNew(DungeonRoom dungeonRoom) {
            RoomProcessorRedRoom defaultRoomProcessor = new RoomProcessorRedRoom(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
