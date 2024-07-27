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
import kr.syeyoung.dungeonsguide.dungeon.data.PrecalculatedMoveNearest;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class DungeonSecretItemDrop implements DungeonMechanic, ISecret {
    private static final long serialVersionUID = 8784808599222706537L;

    private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
    private PrecalculatedMoveNearest moveNearest;
    private List<String> preRequisite = new ArrayList<String>();

    @Override
    public boolean isFound(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom) == SecretStatus.FOUND;
    }
    public void tick(DungeonRoom dungeonRoom) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            if (Minecraft.getMinecraft().thePlayer.getDistanceSq(pos) < 2) {
                List<EntityItem> items = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(-4, -4, -4, 4, 4, 4).addCoord(pos.getX(), pos.getY(),pos.getZ()));
                if (items.size() == 0) {
                    dungeonRoom.getRoomContext().put("i-"+ISecret.toString(pos), true); // was there, but gone!
                    ChatTransmitter.sendDebugChat("Assume at "+ISecret.toString(pos)+"found.");
                }
            }
            if (Minecraft.getMinecraft().thePlayer.getDistanceSq(pos) < 100) {
                List<EntityItem> items = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(-4, -4, -4, 4, 4, 4).addCoord(pos.getX(), pos.getY(),pos.getZ()));
                if (items.size() != 0) {
                    dungeonRoom.getRoomContext().put("i-"+ISecret.toString(pos), false);
//                    ChatTransmitter.sendDebugChat("Assume at "+ISecret.toString(pos)+" not found? "+items.size());
                } else if (Boolean.FALSE.equals(dungeonRoom.getRoomContext().get("i-"+ISecret.toString(pos)))) {
                    dungeonRoom.getRoomContext().put("i-"+ISecret.toString(pos), true); // was there, but gone!
                    ChatTransmitter.sendDebugChat("Assume at "+ISecret.toString(pos)+"found? "+items.size());
                }
            }
    }

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
            BlockPos bpos = secretPoint.getBlockPos(dungeonRoom);
            if (Boolean.TRUE.equals(dungeonRoom.getRoomContext().get("i-"+ISecret.toString(bpos)))) {
                return SecretStatus.FOUND;
            }
            Vec3 pos = new Vec3(bpos);
            for (Integer pickedup : DungeonActionContext.getPickedups()) {
                if (DungeonActionContext.getSpawnLocation().get(pickedup) == null) continue;
                if (DungeonActionContext.getSpawnLocation().get(pickedup).squareDistanceTo(pos) < 4) {
                    dungeonRoom.getRoomContext().put("i-"+ISecret.toString(bpos), true);
                    return SecretStatus.FOUND;
                }
            }

            return Boolean.FALSE.equals(dungeonRoom.getRoomContext().get("i-"+ISecret.toString(bpos))) ? SecretStatus.DEFINITELY_NOT : SecretStatus.NOT_SURE;

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


        if (moveNearest != null) {
            ActionUtils.buildActionMoveAnd(builder, dungeonRoom, moveNearest, Collections.emptyList(), preRequisite, um -> um, "MoveNearest");
        } else {
            builder = builder.requires(new ActionMoveNearestAir(secretPoint));
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
        }
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = getSecretPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


        if (moveNearest != null && FeatureRegistry.DEBUG_AIR.isEnabled())
            moveNearest.render(partialTicks, dungeonRoom);
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

    public DungeonSecretItemDrop clone() throws CloneNotSupportedException {
        DungeonSecretItemDrop dungeonSecret = new DungeonSecretItemDrop();
        dungeonSecret.moveNearest = moveNearest;
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
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
