package kr.syeyoung.dungeonsguide.mod.dungeon.world;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.RoomBounds;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.EnumHalf;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockSkull;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollisionStateCalculatingCoordinateMap implements ICoordinateMap<CollisionStateCalculatingCoordinateMap.CollisionState> {
    private ICoordinateMap<UBlockState> map;
    private InstaBreakFactorCalculatingCoordinateMap instaBreakCalc;

    @Getter
    private int minX, minY, minZ, maxX, maxY, maxZ, lenX, lenY, lenZ;

    private CoordinateMapBlockAccessible world;

    private Set<VectorI3D> poses;
    private RoomBounds roomBounds;
    public CollisionStateCalculatingCoordinateMap(ICoordinateMap<UBlockState> map, Set<VectorI3D> poses, InstaBreakFactorCalculatingCoordinateMap instaBreakCalc, RoomBounds roomBounds) {
        this.map = map;
        this.world = new CoordinateMapBlockAccessible(map);

        this.minX = roomBounds.getMinX() * 2 + 2;
        this.minY = 0;
        this.minZ = roomBounds.getMinZ() * 2 + 2;
        this.maxX = roomBounds.getMaxX() * 2 + 2;
        this.maxY = 256 * 2;
        this.maxZ = roomBounds.getMaxZ() * 2 + 2;

        this.lenX = maxX - minX;
        this.lenY = maxY - minY;
        this.lenZ = maxZ - minZ;

        this.poses = poses;
        this.instaBreakCalc = instaBreakCalc;
        this.roomBounds = roomBounds;
    }

    private static final float playerWidth = 0.25f;
    @Override
    public CollisionState getBlock(int x, int y, int z) {
        if (!roomBounds.canAccessRelative( (x - minX + 2) / 2, (z - minZ + 2) / 2)) return CollisionStateCalculatingCoordinateMap.CollisionState.BLOCKED;
        // TODO: use isInScope to determine.

        float wX = x / 2.0f, wY = y / 2.0f, wZ = z / 2.0f;

        AABB bb = new AABB(wX - playerWidth, wY+0.06251, wZ - playerWidth,
                        wX + playerWidth, wY +0.06251 + 1.8, wZ + playerWidth);
        AABB pearlTest = new AABB(
                wX - 0.5, wY - 0.5, wZ - 0.5, wX + 0.5, wY + 0.5, wZ+0.5
        );

        int minX = MathHelper.floor_double(bb.minX);
        int maxX = MathHelper.floor_double(bb.maxX + 1.0D);
        int minY = MathHelper.floor_double(bb.minY);
        int maxY = MathHelper.floor_double(bb.maxY + 1.0D);
        int minZ = MathHelper.floor_double(bb.minZ);
        int maxZ = MathHelper.floor_double(bb.maxZ + 1.0D);

        AABB testBox = bb.addCoord(0, -0.5, 0);
        VectorI3D blockPos = new VectorI3D(0,0,0);
        List<AABB> list = new ArrayList<>();
        List<AABB> list2 = new ArrayList<>();
        int size = 0;

//        boolean
        boolean stairs = false;
        boolean superboom = false;
        boolean foundstairat = false;
        boolean slabTop = false;
        int notstonkable = 0;
        for (int k1 = minX; k1 < maxX; ++k1) {
            for (int l1 = minZ; l1 < maxZ; ++l1) {
                label: for (int i2 = minY-1; i2 < maxY; ++i2) {
                    blockPos.x = k1;
                    blockPos.y = i2;
                    blockPos.z = l1;

                    UBlockState state = map.getBlock(k1, i2, l1);
//                    Block block = state.getBlock();
                    state.addCollisionBoxesToList(world, blockPos, testBox, list);
                    state.addCollisionBoxesToList(world, blockPos, bb, list2);


                    if (list2.size() != size) {
                        // collision!!

                        if (poses.contains(blockPos)) {
                            for (int i = 0; i < Math.max(0, list2.size() - size); i++)
                                list2.remove(size);
                            superboom = true;
                            continue label;
                        }

                        int breakFactor = instaBreakCalc.getBlock(k1, i2, l1).getFactor();
                        if (breakFactor > 0) {
                            if (i2 == maxY - 1 && (state.getBlock() != Blocks.iron_bars && !(state.getBlock() instanceof BlockFence)) && !(state.getBlock() instanceof BlockSkull)) {
                                // head level no break
                                notstonkable = 99;
                            } else {
                                notstonkable+= breakFactor;
                            }
                            if (state.getBlock() == Blocks.bedrock) {
                                notstonkable = 99;
                            }
                        }

                    }
                    size = list2.size();
                    if (state.isOf(BlockType.TAG_STAIR) && i2 != minY - 1) {
                        stairs = true;
                    }
                    if (state.isOf(BlockType.TAG_STAIR) && i2 == minY) {
                        foundstairat = true;
                        slabTop = state.getStairHalf() == EnumHalf.TOP;
                    }
                }
            }
        }
        boolean isOnGround = false;
        for (AABB axisAlignedBB : list) {
            if (axisAlignedBB.maxY <= bb.minY) {
                isOnGround = true;
                break;
            }
        }
        boolean blocked = !list2.isEmpty();

        int headcut = 0, bodycut = 0;
        for (AABB axisAlignedBB : list2) {
            if (axisAlignedBB.minY >= wY + 0.9f && axisAlignedBB.minY <= wY + 1.4f) headcut++;
            if (axisAlignedBB.minY >= wY) bodycut++;
        }

        // weirdest thing ever check.
        list2.clear();
        size = 0;

        if (!blocked && (x%2 == 0) != (z%2 == 0) && y %2 == 0 && isOnGround) {
            boolean stairFloor = false;
            boolean elligible = false;
            label: for (int k1 = minX; k1 < maxX; ++k1) {
                for (int l1 = minZ; l1 < maxZ; ++l1) {
                    blockPos.x = k1;
                    blockPos.y = minY - 1;
                    blockPos.z = l1;
                    UBlockState state = map.getBlock(k1, minY - 1, l1);

                    state.addCollisionBoxesToList(
                            world, blockPos, testBox, list2
                    );
                    if (size != list2.size()) {
                        elligible = true;
                    } else if (state.isOf(BlockType.TAG_STAIR)) {
                        stairFloor = true;
                    }
                    size = list2.size();


                    blockPos.x = k1;
                    blockPos.y = minY;
                    blockPos.z = l1;

                    state = map.getBlock(k1, minY, l1);

                    if (state.canCollideCheck(true)) {
                        elligible = false;
                        break label;
                    }
                }
            }
            if (elligible && stairFloor) {
                return CollisionStateCalculatingCoordinateMap.CollisionState.ENDERCHEST;
            }
        }

        if (!blocked) { // I'm on ground
            if (superboom) {
                if (isOnGround) {
                    return CollisionStateCalculatingCoordinateMap.CollisionState.SUPERBOOMABLE_GROUND;
                } else {
                    return CollisionStateCalculatingCoordinateMap.CollisionState.SUPERBOOMABLE_AIR;
                }
            }
            if (stairs && isOnGround) {
                return CollisionStateCalculatingCoordinateMap.CollisionState.STAIR;
            }

            if (isOnGround) {
                return CollisionStateCalculatingCoordinateMap.CollisionState.ONGROUND;
            } else {
                return CollisionStateCalculatingCoordinateMap.CollisionState.ONAIR;
            }
        } else {


            // from here, blocked = true.
            if (notstonkable > 2) {
                if (!isOnGround) {
                    return CollisionStateCalculatingCoordinateMap.CollisionState.BLOCKED;
                } else {
                    return CollisionStateCalculatingCoordinateMap.CollisionState.BLOCKED_GROUND;
                }
            }

            if (!isOnGround) {
                return CollisionStateCalculatingCoordinateMap.CollisionState.STONKING_AIR;
            } else {
                return CollisionStateCalculatingCoordinateMap.CollisionState.STONKING;
            }
        }
    }

    @Override
    public boolean isInScope(int x, int y, int z) {
        return !(x < minX || z < minZ || x >= maxX || z >= maxZ || y < minY || y+4 >= maxY);
    }

    @AllArgsConstructor
    @Getter
    public enum CollisionState {
        UNCACHED(false, false, false, false, null),
        ONAIR(true, false, false, false, new Color(0x3300FF00, true)),
        ONGROUND(true, false, false, true, new Color(0x33007700, true)),
        SUPERBOOMABLE_GROUND(true, false, false, true, new Color(0x33007777, true)),
        SUPERBOOMABLE_AIR(true, false, false, false, new Color(0x3300FFFF, true)),
        STAIR(true, true, false, true, new Color(0x33FFFF00, true)), // can't enter stonking while flying, I tried, it's so hard.
        ENDERCHEST(true, true, false, true, new Color(0x33FFFF00, true)),
        STONKING(true, true, true, true, new Color(0x33000077, true)),
        STONKING_AIR(true, true, true, false, new Color(0x330000FF, true)),
        BLOCKED(false, true, true, false, new Color(0x33FF0000, true)),
        BLOCKED_GROUND(false, true, true, true, new Color(0x33FF0000, true));


        private boolean canGo;
        private boolean isClip;
        private boolean blocked;
        private boolean onGround;
        private Color color;

        public static final int BITS = (int) Math.ceil(Math.log(CollisionState.values().length) / Math.log(2));
        public static final CollisionState[] VALUES = CollisionState.values();
    }
}
