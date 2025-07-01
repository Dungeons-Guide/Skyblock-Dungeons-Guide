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

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.tileentities.UTileEntityChest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DungeonSecretChestState implements DungeonMechanicState, ISecret {
    private final DungeonSecretChestData data;
    private final DungeonRoom room;

    public DungeonSecretChestState(DungeonSecretChestData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    public void markFound() {
        lastMeasuredChestStatus = LastMeasuredChestStatus.OPENED;
    }

    public enum LastMeasuredChestStatus {
        WASNT_THERE, UNOPEN, OPENED
    }

    private LastMeasuredChestStatus lastMeasuredChestStatus = LastMeasuredChestStatus.WASNT_THERE;

    public void tick(DungeonRoom dungeonRoom) {
        VectorI3D pos = data.secretPoint.getBlockPos(dungeonRoom);
        UBlockState blockState = dungeonRoom.getContext().getWorld().getBlockStateAt(pos);
        if (blockState.isOf(BlockType.CHEST, BlockType.TRAP_CHEST)) {
            UTileEntityChest chest = (UTileEntityChest) dungeonRoom.getContext().getWorld().getTileEntityAt(pos);
            if (chest != null) {
                if (chest.getViewers() > 0) {
                    lastMeasuredChestStatus = LastMeasuredChestStatus.OPENED;
                } else {
                    if (lastMeasuredChestStatus == LastMeasuredChestStatus.WASNT_THERE)
                        lastMeasuredChestStatus = LastMeasuredChestStatus.UNOPEN;
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
        VectorI3D pos = data.secretPoint.getBlockPos(dungeonRoom);
        UBlockState blockState = dungeonRoom.getContext().getWorld().getBlockStateAt(pos);
        if (lastMeasuredChestStatus != LastMeasuredChestStatus.WASNT_THERE)
            return (lastMeasuredChestStatus == LastMeasuredChestStatus.OPENED || blockState.isOf(BlockType.AIR)) ? SecretStatus.FOUND : SecretStatus.CREATED;

        if (blockState.isOf(BlockType.AIR)) {
            return SecretStatus.DEFINITELY_NOT;
        } else if (!blockState.isOf(BlockType.CHEST, BlockType.TRAP_CHEST)) {
            return SecretStatus.ERROR;
        } else {
            UTileEntityChest chest = (UTileEntityChest) dungeonRoom.getContext().getWorld().getTileEntityAt(pos);
            if (chest != null && chest.getViewers() > 0) {
                return SecretStatus.FOUND;
            } else {
                return SecretStatus.CREATED;
            }
        }
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }
        if (!"found".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not valid state for secret");
        if (action.equals("found") && getSecretStatus(room) == SecretStatus.FOUND) return;

        List<String> requiredRequisite = data.preRequisite.stream().filter(a -> {
            return room.getMechanics().get(a.split(":")[0]) instanceof DungeonOnewayDoorState;
        }).collect(Collectors.toList());
        List<String> optionalRequisite = data.preRequisite.stream().filter(a -> {
            return !requiredRequisite.contains(a);
        }).collect(Collectors.toList());

        if (data.secretCache != null)
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretCache, optionalRequisite, requiredRequisite, algorithmSetting);
        else
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretPoint, builder1 -> {
                boolean doneDoor = false;
                for (String str : data.preRequisite) {
                    if (room.getMechanics().get(str.split(":")[0]) instanceof DungeonOnewayDoorState) {
                        builder1.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
                        doneDoor = true;
                    }
                }
                if (doneDoor)
                    builder1 = builder1.requires(new ActionRoot(), algorithmSetting);
                for (String str : data.preRequisite) {
                    if (str.isEmpty()) continue;
                    if (room.getMechanics().get(str) instanceof DungeonOnewayDoorState) continue;
                    builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
                }
                return null;
            }, algorithmSetting);


    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        VectorI3D pos = getSecretPoint().getBlockPos(room);
        RenderUtils.highlightBlockStencil(pos, partialTicks, color, false);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


        if (data.secretCache != null && FeatureRegistry.DEBUG_ST.isEnabled())
            data.secretCache.render(partialTicks, room);
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


    @Override
    public String getCurrentState() {
        return getSecretStatus(room).getStateName();
    }

    @Override
    public Set<String> getAvailableActions() {
        SecretStatus status = getSecretStatus(room);
        if (status == SecretStatus.FOUND) return Sets.newHashSet("navigate");
        else return Sets.newHashSet("found", "navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("found"/*, "definitely_not", "not_sure", "created", "error"*/);
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Override
    public OffsetPoint getSecretPoint() {
        return data.secretPoint;
    }

    @Override
    public List<String> getPreRequisite() {
        return data.preRequisite;
    }

    @Data
    public static class DungeonSecretChestData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedStonk secretCache;
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonSecretChestData() {
        }

        @Override
        public DungeonSecretChestState createState(DungeonRoom room) {
            return new DungeonSecretChestState(this, room);
        }

        public DungeonSecretChestData clone() throws CloneNotSupportedException {
            DungeonSecretChestData data = new DungeonSecretChestData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.secretCache = secretCache;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
