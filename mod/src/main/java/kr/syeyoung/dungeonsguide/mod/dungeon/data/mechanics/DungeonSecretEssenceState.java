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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UTileEntity;
import kr.syeyoung.modapi.world.tileentities.UTileEntitySkull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class DungeonSecretEssenceState implements DungeonMechanicState, ISecret {
    private final DungeonSecretEssenceData data;
    private final DungeonRoom room;

    public DungeonSecretEssenceState(DungeonSecretEssenceData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    private boolean essenceWasThere = false;
    private boolean found = false;
    private int nearbyTicks = 0;
    public void tick(DungeonRoom dungeonRoom) {
        VectorI3D pos = data.secretPoint.getBlockPos(dungeonRoom);
        UBlockState blockState = dungeonRoom.getContext().getWorld().getBlockStateAt(pos);
        if (blockState.isOf(BlockType.SKULL)) {
            essenceWasThere = true;
            List<UEntity> entities = dungeonRoom.getContext().getWorld().getEntitiesWithinAabb(EntityType.ARMOR_STAND, new AABB(pos.getX(),pos.getY()-3,pos.getZ(), pos.getX()+1, pos.getY()+2, pos.getZ()+1));
            UTileEntity tileEntity = dungeonRoom.getContext().getWorld().getTileEntityAt(pos);

            if (ModAPI.getAPI().getPlayer().getPosition().distanceSq(pos) < 25) {
                if (tileEntity instanceof UTileEntitySkull) {
                    String texture = ((UTileEntitySkull) tileEntity).getTexture();
                    if (texture == null) return;
                    for (UEntity entity : entities) {
                        UItemStack itemStackIn = ((UEntityArmorStand)entity).getEquipmentInSlot(4);
                        System.out.println(itemStackIn);
                        if (itemStackIn == null) continue;
                        if (itemStackIn.getItem() != Item.SKULL) continue;
                        if (texture.equals(itemStackIn.getSkullTexture())) {
                            found = true;
                            return;
                        }
                    }
                }
            }
        } else if (blockState.isOf(BlockType.AIR) && essenceWasThere) {
            found = true;
        } else if (blockState.isOf(BlockType.AIR) && !essenceWasThere) {
            if (ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(pos) < 25) {
                nearbyTicks++;
            }
            if (nearbyTicks > 100) {
                essenceWasThere = true;
                found = true;
            }
        }

    }

    @Override
    public boolean isFound(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom) == SecretStatus.FOUND;
    }

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        if (found) return SecretStatus.FOUND;
        if (essenceWasThere) return SecretStatus.DEFINITELY_NOT;
        return SecretStatus.NOT_SURE;
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


        if (data.secretCache != null)
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretCache, data.preRequisite, Collections.emptyList(), algorithmSetting);
        else
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretPoint, builder1 -> {
                for (String str : data.preRequisite) {
                    if (str.isEmpty()) continue;
                    builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
                }
                return null;
            }, algorithmSetting);
    }

    @Override
    public void highlight(Color color, String name, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = getSecretPoint().getBlockPos(room);
        context.highlightBlockStencil(pos, partialTicks, color, false);
        context.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        context.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


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
    public static class DungeonSecretEssenceData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedStonk secretCache;
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonSecretEssenceData() {
        }

        @Override
        public DungeonSecretEssenceState createState(DungeonRoom room) {
            return new DungeonSecretEssenceState(this, room);
        }

        public DungeonSecretEssenceData clone() throws CloneNotSupportedException {
            DungeonSecretEssenceData data = new DungeonSecretEssenceData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.secretCache = secretCache;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
