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
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateBat;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonSecret implements DungeonMechanic {
    private static final long serialVersionUID = 8784808599222706537L;

    private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
    private SecretType secretType = SecretType.CHEST;
    private List<String> preRequisite = new ArrayList<String>();

    public void tick(DungeonRoom dungeonRoom) {
        if (secretType == SecretType.CHEST) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(pos);
            if (blockState.getBlock() == Blocks.chest || blockState.getBlock() == Blocks.trapped_chest) {
                TileEntityChest chest = (TileEntityChest) dungeonRoom.getContext().getWorld().getTileEntity(pos);
                if(chest != null){
                    if (chest.numPlayersUsing > 0) {
                        dungeonRoom.getRoomContext().put("c-" + pos.toString(), 2);
                    } else {
                        dungeonRoom.getRoomContext().put("c-" + pos.toString(), 1);
                    }
                } else {
                    System.out.println("Expected TileEntityChest at " + pos + " to not be null");
                }
            }
        } else if (secretType == SecretType.ESSENCE) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(pos);
            if (blockState.getBlock() == Blocks.skull) {
                dungeonRoom.getRoomContext().put("e-" + pos.toString(), true);
            }
        } else if (secretType == SecretType.ITEM_DROP) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            if (Minecraft.getMinecraft().thePlayer.getDistanceSq(pos) < 2) {
                List<EntityItem> items = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(-4, -4, -4, 4, 4, 4).addCoord(pos.getX(), pos.getY(),pos.getZ()));
                if (items.size() == 0) {
                    dungeonRoom.getRoomContext().put("i-"+pos.toString(), true); // was there, but gone!
                    ChatTransmitter.sendDebugChat("Assume at "+pos.toString()+"found.");
                }
            }
            if (Minecraft.getMinecraft().thePlayer.getDistanceSq(pos) < 100) {
                List<EntityItem> items = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(-4, -4, -4, 4, 4, 4).addCoord(pos.getX(), pos.getY(),pos.getZ()));
                if (items.size() != 0) {
                    dungeonRoom.getRoomContext().put("i-"+pos.toString(), false);
                    ChatTransmitter.sendDebugChat("Assume at "+pos.toString()+" not found? "+items.size());
                } else if (Boolean.FALSE.equals(dungeonRoom.getRoomContext().get("i-"+pos.toString()))) {
                    dungeonRoom.getRoomContext().put("i-"+pos.toString(), true); // was there, but gone!
                    ChatTransmitter.sendDebugChat("Assume at "+pos.toString()+"found? "+items.size());
                }
            }

        }
    }

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        if (secretType == SecretType.CHEST) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(pos);
            if (dungeonRoom.getRoomContext().containsKey("c-" + pos.toString()))
                return ((int) dungeonRoom.getRoomContext().get("c-" + pos.toString()) == 2 || blockState.getBlock() == Blocks.air) ? SecretStatus.FOUND : SecretStatus.CREATED;

            if (blockState.getBlock() == Blocks.air) {
                return SecretStatus.DEFINITELY_NOT;
            } else if (blockState.getBlock() != Blocks.chest && blockState.getBlock() != Blocks.trapped_chest) {
                return SecretStatus.ERROR;
            } else {
                TileEntityChest chest = (TileEntityChest) dungeonRoom.getContext().getWorld().getTileEntity(pos);
                if (chest.numPlayersUsing > 0) {
                    return SecretStatus.FOUND;
                } else {
                    return SecretStatus.CREATED;
                }
            }
        } else if (secretType == SecretType.ESSENCE) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(pos);
            if (blockState.getBlock() == Blocks.skull) {
                dungeonRoom.getRoomContext().put("e-" + pos.toString(), true);
                return SecretStatus.DEFINITELY_NOT;
            } else {
                if (dungeonRoom.getRoomContext().containsKey("e-" + pos.toString()))
                    return SecretStatus.FOUND;
                return SecretStatus.NOT_SURE;
            }
        } else if (secretType == SecretType.BAT) {
            BlockPos bpos = secretPoint.getBlockPos(dungeonRoom);
            if (dungeonRoom.getRoomContext().containsKey("b-"+bpos.toString())) {
                return SecretStatus.FOUND;
            }
            Vec3 spawn = new Vec3(bpos);
            for (Integer killed : DungeonActionContext.getKilleds()) {
                if (DungeonActionContext.getSpawnLocation().get(killed) == null) continue;
                if (DungeonActionContext.getSpawnLocation().get(killed).squareDistanceTo(spawn) < 100) {
                    dungeonRoom.getRoomContext().put("b-"+bpos.toString(), true);
                    return SecretStatus.FOUND;
                }
            }
            return SecretStatus.NOT_SURE;
        } else {
            BlockPos bpos = secretPoint.getBlockPos(dungeonRoom);
            if (Boolean.TRUE.equals(dungeonRoom.getRoomContext().get("i-"+bpos.toString()))) {
                return SecretStatus.FOUND;
            }
            Vec3 pos = new Vec3(bpos);
            for (Integer pickedup : DungeonActionContext.getPickedups()) {
                if (DungeonActionContext.getSpawnLocation().get(pickedup) == null) continue;
                if (DungeonActionContext.getSpawnLocation().get(pickedup).squareDistanceTo(pos) < 4) {
                    dungeonRoom.getRoomContext().put("i-"+bpos.toString(), true);
                    return SecretStatus.FOUND;
                }
            }

            return Boolean.FALSE.equals(dungeonRoom.getRoomContext().get("i-"+bpos.toString())) ? SecretStatus.DEFINITELY_NOT : SecretStatus.NOT_SURE;
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
        if (secretType == SecretType.CHEST || secretType == SecretType.ESSENCE) {
            builder = builder.requires(new AtomicAction.Builder()
                    .requires(new ActionClick(secretPoint))
                    .requires(new ActionMove(secretPoint))
                    .build("MoveAndClick"));
        } else if (secretType == SecretType.BAT) {
            builder = builder.requires(new AtomicAction.Builder()
                    .requires(() -> {
                        ActionKill actionKill = new ActionKill(secretPoint);
                        actionKill.setRadius(10);
                        actionKill.setPredicate(PredicateBat.INSTANCE);
                        return actionKill;
                    }).requires(new ActionMove(secretPoint))
                    .build("MoveAndKill"));
        } else {
            builder = builder.requires(new ActionMove(secretPoint));
        }

        if (secretType == SecretType.CHEST) {
            boolean doneDoor = false;
            for (String str : preRequisite) {
                if (dungeonRoom.getMechanics().get(str) instanceof DungeonOnewayDoor) {
                    builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
                    doneDoor = true;
                }
            }
            if (doneDoor)
                builder = builder.requires(new ActionRoot());
        }
        for (String str : preRequisite) {
            if (str.isEmpty()) continue;
            if (secretType == SecretType.CHEST && dungeonRoom.getMechanics().get(str) instanceof DungeonOnewayDoor) continue;
            builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
        }
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = getSecretPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color, partialTicks);
        RenderUtils.drawTextAtWorld(getSecretType().name(), pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
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

    public DungeonSecret clone() throws CloneNotSupportedException {
        DungeonSecret dungeonSecret = new DungeonSecret();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
        dungeonSecret.secretType = secretType;
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
