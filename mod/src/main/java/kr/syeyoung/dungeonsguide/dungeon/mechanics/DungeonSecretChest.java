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

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateBat;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DungeonSecretChest implements DungeonMechanic, ISecret {
    private static final long serialVersionUID = 8784808599222706537L;

    private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
    private PrecalculatedStonk secretCache;
    private List<String> preRequisite = new ArrayList<String>();

    public void tick(DungeonRoom dungeonRoom) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = dungeonRoom.getContext().getWorld().getBlockState(pos);
            if (blockState.getBlock() == Blocks.chest || blockState.getBlock() == Blocks.trapped_chest) {
                TileEntityChest chest = (TileEntityChest) dungeonRoom.getContext().getWorld().getTileEntity(pos);
                if(chest != null){
                    if (chest.numPlayersUsing > 0) {
                        dungeonRoom.getRoomContext().put("c-" + ISecret.toString(pos), 2);
                    } else {
                        dungeonRoom.getRoomContext().put("c-" + ISecret.toString(pos), 1);
                    }
                } else {
                    System.out.println("Expected TileEntityChest at " + pos + " to not be null");
                }
            }
    }

    @Override
    public boolean isFound(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom) == SecretStatus.FOUND;
    }
    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = dungeonRoom.getCachedWorld().getBlockState(pos);
            if (dungeonRoom.getRoomContext().containsKey("c-" + ISecret.toString(pos)))
                return ((int) dungeonRoom.getRoomContext().get("c-" + ISecret.toString(pos)) == 2 || blockState.getBlock() == Blocks.air) ? SecretStatus.FOUND : SecretStatus.CREATED;

            if (blockState.getBlock() == Blocks.air) {
                return SecretStatus.DEFINITELY_NOT;
            } else if (blockState.getBlock() != Blocks.chest && blockState.getBlock() != Blocks.trapped_chest) {
                return SecretStatus.ERROR;
            } else {
                TileEntityChest chest = (TileEntityChest) dungeonRoom.getContext().getWorld().getTileEntity(pos);
                if (chest != null && chest.numPlayersUsing > 0) {
                    return SecretStatus.FOUND;
                } else {
                    return SecretStatus.CREATED;
                }
            }
    }

    @Override
    public void buildAction(String state, DungeonRoom dungeonRoom, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (state.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)));
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            return;
        }
        if (!"found".equalsIgnoreCase(state))
            throw new PathfindImpossibleException(state + " is not valid state for secret");
        if (state.equals("found") && getSecretStatus(dungeonRoom) == SecretStatus.FOUND) return;

        List<String> requiredRequisite = preRequisite.stream().filter(a -> {
            return dungeonRoom.getMechanics().get(a.split(":")[0]) instanceof DungeonOnewayDoor;
        }).collect(Collectors.toList());
        List<String> optionalRequisite = preRequisite.stream().filter(a -> {
            return !requiredRequisite.contains(a);
        }).collect(Collectors.toList());

        if (secretCache != null)
            ActionUtils.buildActionMoveAndClick(builder, dungeonRoom, secretCache, optionalRequisite, requiredRequisite);
        else
            ActionUtils.buildActionMoveAndClick(builder, dungeonRoom, secretPoint, builder1 -> {
                boolean doneDoor = false;
                for (String str : preRequisite) {
                    if (dungeonRoom.getMechanics().get(str.split(":")[0]) instanceof DungeonOnewayDoor) {
                        builder1.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
                        doneDoor = true;
                    }
                }
                if (doneDoor)
                    builder1 = builder1.requires(new ActionRoot());
                for (String str : preRequisite) {
                    if (str.isEmpty()) continue;
                    if (dungeonRoom.getMechanics().get(str) instanceof DungeonOnewayDoor) continue;
                    builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
                }
                return null;
            });



    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = getSecretPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


        if (secretCache != null && FeatureRegistry.DEBUG_ST.isEnabled())
            secretCache.render(partialTicks, dungeonRoom);
    }

    public enum SecretType {
        BAT, CHEST, ITEM_DROP, ESSENCE
    }

    @AllArgsConstructor
    @Getter
    public enum SecretStatus {
        DEFINITELY_NOT("definitely_not"), NOT_SURE("not_sure"), CREATED("created"), FOUND("found"), ERROR("error");

        private final String stateName;
    }

    public DungeonSecretChest clone() throws CloneNotSupportedException {
        DungeonSecretChest dungeonSecret = new DungeonSecretChest();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
        dungeonSecret.secretCache = secretCache;
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }


    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom).getStateName();
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        SecretStatus status = getSecretStatus(dungeonRoom);
        if (status == SecretStatus.FOUND) return Sets.newHashSet("navigate");
        else return Sets.newHashSet("found", "navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("found"/*, "definitely_not", "not_sure", "created", "error"*/);
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return secretPoint;
    }
}
