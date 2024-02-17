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

package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class DungeonRoomInfo implements Serializable {
    private static final long serialVersionUID = -8291811286448196640L;

    public DungeonRoomInfo(short shape, byte color) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.shape = shape;
        this.color = color;
    }

    private transient boolean registered;

    private boolean isUserMade = false;

    private short shape;
    private byte color;

    private int[][] blocks;

    private UUID uuid;
    private String name;

    private String processorId = "default";

    private Map<String, Object> properties = new HashMap<>();

    private Map<String, DungeonMechanic> mechanics = new HashMap<>();
    private int totalSecrets = -1;

    private char[] world;
    private int width, length;

    public void setSize(int width, int length, int height) {
        this.width = width;
        this.length = length;
        this.world = new char[width * length * 256];
    }
    public void setBlock(OffsetPoint offsetPoint, IBlockState iBlockState) {
        int index = offsetPoint.getX() + ((offsetPoint.getY()+70) * length + offsetPoint.getZ()) * width;
        world[index] = (char) Block.BLOCK_STATE_IDS.get(iBlockState);
    }

    public IBlockState getBlock(OffsetPoint offsetPoint, int rot) {
        return getBlock(offsetPoint.getX(), offsetPoint.getY(), offsetPoint.getZ(), rot);
    }

    public IBlockState getBlock(int x, int y, int z, int rot) {
        if (y < -70 || y >= 186) return Blocks.air.getDefaultState();
        if (x < 0 || x >= width) return Blocks.air.getDefaultState();
        if (z < 0 || z >= length) return Blocks.air.getDefaultState();

        int index = x + ((y+70) * length + z) * width;
        IBlockState blockState = Block.BLOCK_STATE_IDS.getByValue(world[index]);

        Optional<PropertyDirection> propertyDirection = blockState.getPropertyNames().stream()
                .filter(a -> a instanceof PropertyDirection)
                .map(PropertyDirection.class::cast).findFirst();

        if (propertyDirection.isPresent()) {
            EnumFacing enumFacing = blockState.getValue(propertyDirection.get());
            if (!(enumFacing == EnumFacing.UP || enumFacing == EnumFacing.DOWN)) {
                for (int i = 0; i < 4- rot; i++)
                    enumFacing = enumFacing.rotateY();
                blockState = blockState.withProperty(propertyDirection.get(), enumFacing);
            }
        }

        for (DungeonMechanic value : mechanics.values()) {
            if (value instanceof DungeonSecretEssence) {
                OffsetPoint offsetPoint = ((DungeonSecretEssence) value).getSecretPoint();
                if (offsetPoint.getX() == x && offsetPoint.getY() == y && offsetPoint.getZ() == z) {
                    if (blockState.getBlock() != Blocks.skull)
                        return Blocks.skull.getStateFromMeta(0);
                    break;
                }
            } else if (value instanceof DungeonRedstoneKey) {
                OffsetPoint offsetPoint = ((DungeonRedstoneKey) value).getSecretPoint();
                if (offsetPoint.getX() == x && offsetPoint.getY() == y && offsetPoint.getZ() == z) {
                    if (blockState.getBlock() != Blocks.skull)
                        return Blocks.skull.getStateFromMeta(0);
                    break;
                }
            } else if (value instanceof DungeonWizardCrystal) {
                OffsetPoint offsetPoint = ((DungeonWizardCrystal) value).getSecretPoint();
                if (offsetPoint.getX() == x && offsetPoint.getY() == y && offsetPoint.getZ() == z) {
                    if (blockState.getBlock() != Blocks.skull)
                        return Blocks.skull.getStateFromMeta(0);
                    break;
                }
            } else if (value instanceof DungeonSecretChest) {
                OffsetPoint offsetPoint = ((DungeonSecretChest) value).getSecretPoint();
                if (offsetPoint.getX() == x && offsetPoint.getY() == y && offsetPoint.getZ() == z) {
                    if (blockState.getBlock() != Blocks.chest)
                        return Blocks.chest.getStateFromMeta(0);
                    break;

                }
            } else if (value instanceof DungeonOnewayLever) {
                OffsetPoint offsetPoint = ((DungeonOnewayLever) value).getLeverPoint();
                if (offsetPoint.getX() == x && offsetPoint.getY() == y && offsetPoint.getZ() == z) {
                    if (blockState.getBlock() != Blocks.lever)
                        return Blocks.lever.getStateFromMeta(0);
                    break;
                }
            } else if (value instanceof DungeonSecretDoubleChest) {
                OffsetPoint offsetPoint = ((DungeonSecretDoubleChest) value).getSecretPoint();
                if (offsetPoint.getX() == x && offsetPoint.getY() == y && offsetPoint.getZ() == z) {
                    if (blockState.getBlock() != Blocks.chest)
                        return Blocks.chest.getStateFromMeta(0);
                    break;
                }
                offsetPoint = ((DungeonSecretDoubleChest) value).getSecretPoint2();
                if (offsetPoint.getX() == x && offsetPoint.getY() == y && offsetPoint.getZ() == z) {
                    if (blockState.getBlock() != Blocks.chest)
                        return Blocks.chest.getStateFromMeta(0);
                    break;
                }
            }
        }


        return blockState == null ? Blocks.air.getDefaultState() : blockState;
    }
    public boolean hasSchematic() {
        return world != null;
    }
}
