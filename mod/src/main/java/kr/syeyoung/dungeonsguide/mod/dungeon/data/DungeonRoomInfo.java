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
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.serialization.DungeonRoomInfoBlocksDeserializer;
import kr.syeyoung.dungeonsguide.mod.dungeon.serialization.DungeonRoomInfoBlocksSerializer;
import kr.syeyoung.dungeonsguide.mod.dungeon.serialization.DungeonRoomInfoWorldDeserializer;
import kr.syeyoung.dungeonsguide.mod.dungeon.serialization.DungeonRoomInfoWorldSerializer;
import kr.syeyoung.dungeonsguide.mod.dungeon.mechanics.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
public class DungeonRoomInfo implements Serializable {
    private static final long serialVersionUID = -8291811286448196640L;

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

    @JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    private Map<String, Object> properties = new HashMap<>();

    @JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = DungeonArrowTrap.class, name = "trap_arrow"),
            @JsonSubTypes.Type(value = DungeonCrusherTrap.class, name = "trap_crusher"),
            @JsonSubTypes.Type(value = DungeonFakeChestTrap.class, name = "trap_fakechest"),
            @JsonSubTypes.Type(value = DungeonFireTrap.class, name = "trap_fire"),
            @JsonSubTypes.Type(value = DungeonFloorTrap.class, name = "trap_floor"),
            @JsonSubTypes.Type(value = DungeonTripwireTrap.class, name = "trap_tripwire"),

            @JsonSubTypes.Type(value = DungeonSecretBat.class, name = "secret_bat"),
            @JsonSubTypes.Type(value = DungeonSecretChest.class, name = "secret_chest"),
            @JsonSubTypes.Type(value = DungeonSecretDoubleChest.class, name = "secret_doublechest"),
            @JsonSubTypes.Type(value = DungeonSecretEssence.class, name = "secret_essence"),
            @JsonSubTypes.Type(value = DungeonSecretItemDrop.class, name = "secret_itemdrop"),

            @JsonSubTypes.Type(value = DungeonFairySoul.class, name = "fairy_soul"),


            @JsonSubTypes.Type(value = DungeonDoor.class, name = "door_reversible"),
            @JsonSubTypes.Type(value = DungeonOnewayDoor.class, name = "door_irreversible"),
            @JsonSubTypes.Type(value = DungeonLever.class, name = "lever_reversible"),
            @JsonSubTypes.Type(value = DungeonOnewayLever.class, name = "lever_irreversible"),
            @JsonSubTypes.Type(value = DungeonRedstoneKey.class, name = "redstone_key"),
            @JsonSubTypes.Type(value = DungeonRedstoneKeySlot.class, name = "redstone_key_slot"),
            @JsonSubTypes.Type(value = DungeonPressurePlate.class, name = "pressure_plate"),

            @JsonSubTypes.Type(value = DungeonBreakableWall.class, name = "superboom_wall"),
            @JsonSubTypes.Type(value = DungeonTomb.class, name = "crypt"),

            @JsonSubTypes.Type(value = DungeonDummy.class, name = "dummy"),
            @JsonSubTypes.Type(value = DungeonJournal.class, name = "journal"),
            @JsonSubTypes.Type(value = DungeonNPC.class, name = "npc"),

            @JsonSubTypes.Type(value = DungeonRoomDoor2.class, name = "entrance_exit"),

            @JsonSubTypes.Type(value = DungeonMushroom.class, name = "special_mushroom"),
            @JsonSubTypes.Type(value = DungeonWizard.class, name = "npc_wizard"),
            @JsonSubTypes.Type(value = DungeonWizardCrystal.class, name = "special_wizard_crystal"),
    })
    private Map<String, DungeonMechanic> mechanics = new HashMap<>();
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
