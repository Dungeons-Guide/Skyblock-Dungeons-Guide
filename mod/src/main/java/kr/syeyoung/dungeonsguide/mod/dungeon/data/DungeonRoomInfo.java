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

package kr.syeyoung.dungeonsguide.mod.dungeon.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization.DungeonRoomInfoBlocksDeserializer;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization.DungeonRoomInfoBlocksSerializer;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization.DungeonRoomInfoWorldDeserializer;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.serialization.DungeonRoomInfoWorldSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public class DungeonRoomInfo {
    public DungeonRoomInfo(@JsonProperty("shape") short shape, @JsonProperty("color") byte color) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.shape = shape;
        this.color = color;
    }

    private transient boolean registered;

    private boolean isUserMade = false;

    private short shape;
    private byte color;

    @JsonDeserialize(using = DungeonRoomInfoBlocksDeserializer.class)
    @JsonSerialize(using = DungeonRoomInfoBlocksSerializer.class)
    private int[][] blocks;

    private UUID uuid;
    private String name;

    private String processorId = "default";

    @JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = OffsetPoint.class, names = {"OffsetPoint", "kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint"}),
            @JsonSubTypes.Type(value = OffsetPointSet.class, names = {"OffsetPointSet", "kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet"})
    })
    private Map<String, Object> properties = new HashMap<>();

    @JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DungeonArrowTrapState.DungeonArrowTrapData.class, name = "trap_arrow"),
            @JsonSubTypes.Type(value = DungeonCrusherTrapState.DungeonCrusherTrapData.class, name = "trap_crusher"),
            @JsonSubTypes.Type(value = DungeonFakeChestTrapState.DungeonFakeChestTrapData.class, name = "trap_fakechest"),
            @JsonSubTypes.Type(value = DungeonFireTrapState.DungeonFireTrapData.class, name = "trap_fire"),
            @JsonSubTypes.Type(value = DungeonFloorTrapState.DungeonFloorTrapData.class, name = "trap_floor"),
            @JsonSubTypes.Type(value = DungeonTripwireTrapState.DungeonTripwireTrapData.class, name = "trap_tripwire"),

            @JsonSubTypes.Type(value = DungeonSecretBatState.DungeonSecretBatData.class, name = "secret_bat"),
            @JsonSubTypes.Type(value = DungeonSecretChestState.DungeonSecretChestData.class, name = "secret_chest"),
            @JsonSubTypes.Type(value = DungeonSecretDoubleChestState.DungeonSecretDoubleChestData.class, name = "secret_doublechest"),
            @JsonSubTypes.Type(value = DungeonSecretEssenceState.DungeonSecretEssenceData.class, name = "secret_essence"),
            @JsonSubTypes.Type(value = DungeonSecretItemDropState.DungeonSecretItemDropData.class, name = "secret_itemdrop"),

            @JsonSubTypes.Type(value = DungeonFairySoulState.DungeonFairySoulData.class, name = "fairy_soul"),


            @JsonSubTypes.Type(value = DungeonDoorState.DungeonDoorData.class, name = "door_reversible"),
            @JsonSubTypes.Type(value = DungeonOnewayDoorState.DungeonOnewayDoorData.class, name = "door_irreversible"),
            @JsonSubTypes.Type(value = DungeonLeverState.DungeonLeverData.class, name = "lever_reversible"),
            @JsonSubTypes.Type(value = DungeonOnewayLeverState.DungeonOnewayLeverData.class, name = "lever_irreversible"),
            @JsonSubTypes.Type(value = DungeonRedstoneKeyState.DungeonRedstoneKeyData.class, name = "redstone_key"),
            @JsonSubTypes.Type(value = DungeonRedstoneKeySlotState.DungeonRedstoneKeySlotData.class, name = "redstone_key_slot"),
            @JsonSubTypes.Type(value = DungeonPressurePlateState.DungeonPressurePlateData.class, name = "pressure_plate"),

            @JsonSubTypes.Type(value = DungeonBreakableWallState.DungeonBreakableWallData.class, name = "superboom_wall"),
            @JsonSubTypes.Type(value = DungeonTombState.DungeonTombData.class, name = "crypt"),

            @JsonSubTypes.Type(value = DungeonDummyState.DungeonDummyData.class, name = "dummy"),
            @JsonSubTypes.Type(value = DungeonJournalState.DungeonJournalData.class, name = "journal"),
            @JsonSubTypes.Type(value = DungeonNPCState.DungeonNPCData.class, name = "npc"),

            @JsonSubTypes.Type(value = DungeonRoomDoor2State.DungeonRoomDoor2Data.class, name = "entrance_exit"),

            @JsonSubTypes.Type(value = DungeonMushroomState.DungeonMushroomData.class, name = "special_mushroom"),
            @JsonSubTypes.Type(value = DungeonWizardState.DungeonWizardData.class, name = "npc_wizard"),
            @JsonSubTypes.Type(value = DungeonWizardCrystalState.DungeonWizardCrystalData.class, name = "special_wizard_crystal"),
    })
    private Map<String, DungeonMechanicData> mechanics = new HashMap<>();
    private int totalSecrets = -1;

    @Getter
    @JsonDeserialize(using = DungeonRoomInfoWorldDeserializer.class)
    @JsonSerialize(using = DungeonRoomInfoWorldSerializer.class)
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

    private static final PropertyDirection[] directionMap = new PropertyDirection[4096];
    static {
        for (Block block : Block.blockRegistry) {
            Optional<PropertyDirection> propertyDirection = block.getDefaultState().getPropertyNames().stream()
                    .filter(a -> a instanceof PropertyDirection)
                    .map(PropertyDirection.class::cast).findFirst();
            directionMap[Block.getIdFromBlock(block)] = propertyDirection.orElse(null);
        }
    }

    public IBlockState getBlock(int x, int y, int z, int rot) {
        if (y < -70 || y >= 186) return Blocks.bedrock.getDefaultState();
        if (x <= 0 || x >= width) return Blocks.bedrock.getDefaultState();
        if (z <= 0 || z >= length) return Blocks.bedrock.getDefaultState();

        int index = x + ((y + 70) * length + z) * width;
        IBlockState blockState = Block.BLOCK_STATE_IDS.getByValue(world[index]);
        if (rot != 0) {
            PropertyDirection directions = directionMap[world[index] >> 4];

            if (directions != null) {
                EnumFacing enumFacing = blockState.getValue(directions);
                if (!(enumFacing == EnumFacing.UP || enumFacing == EnumFacing.DOWN)) {
                    for (int i = 0; i < 4 - rot; i++)
                        enumFacing = enumFacing.rotateY();
                    blockState = blockState.withProperty(directions, enumFacing);
                }
            }
        }




        return blockState == null ? Blocks.air.getDefaultState() : blockState;
    }
    public boolean hasSchematic() {
        return world != null;
    }
}
