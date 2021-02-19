package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.DungeonActionManager;
import kr.syeyoung.dungeonsguide.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateBat;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.pathfinding.NodeProcessorDungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.panes.SecretEditPane;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.util.Vector3d;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonSecret implements DungeonMechanic {
    private OffsetPoint secretPoint = new OffsetPoint(0,0,0);
    private SecretType secretType = SecretType.CHEST;
    private List<String> preRequisite = new ArrayList<String>();

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        if (secretType == SecretType.CHEST) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = dungeonRoom.getContext().getWorld().getBlockState(pos);
            if (blockState.getBlock() == Blocks.air) {
                return SecretStatus.DEFINITELY_NOT;
            } else if (blockState.getBlock() != Blocks.chest && blockState.getBlock() != Blocks.trapped_chest) {
                return SecretStatus.ERROR;
            } else {
                TileEntityChest chest = (TileEntityChest) dungeonRoom.getContext().getWorld().getTileEntity(pos);
                if (chest.numPlayersUsing > 0) {
                    return SecretStatus.FOUND;
                } else{
                    return SecretStatus.CREATED;
                }
            }
        } else if (secretType == SecretType.ESSENCE) {
            BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
            IBlockState blockState = dungeonRoom.getContext().getWorld().getBlockState(pos);
            if (blockState.getBlock() == Blocks.skull) {
                dungeonRoom.getRoomContext().put("e-"+pos.toString(), true);
                return SecretStatus.DEFINITELY_NOT;
            } else {
                if (dungeonRoom.getRoomContext().containsKey("e-"+pos.toString()))
                    return SecretStatus.FOUND;
                return SecretStatus.NOT_SURE;
            }
        } else if (secretType == SecretType.BAT) {
            Vec3 spawn = new Vec3(secretPoint.getBlockPos(dungeonRoom));
            for (Integer killed : DungeonActionManager.getKilleds()) {
                if (DungeonActionManager.getSpawnLocation().get(killed) == null) continue;
                if (DungeonActionManager.getSpawnLocation().get(killed).squareDistanceTo(spawn) < 100) {
                    return SecretStatus.FOUND;
                }
            }
            return SecretStatus.NOT_SURE;
        } else {
            Vec3 pos = new Vec3(secretPoint.getBlockPos(dungeonRoom));
            if (dungeonRoom.getRoomContext().containsKey("i-"+pos.toString()))
                return SecretStatus.FOUND;
            Vec3 player = Minecraft.getMinecraft().thePlayer.getPositionVector();
            if (player.squareDistanceTo(pos) < 16) {
                Vec3 vec3 = pos.subtract(player).normalize();
                for (int i = 0; i < player.distanceTo(pos); i++) {
                    Vec3 vec = player.addVector(vec3.xCoord * i, vec3.yCoord * i, vec3.zCoord * i);
                    BlockPos bpos = new BlockPos(vec);
                    IBlockState blockState = dungeonRoom.getContext().getWorld().getBlockState(bpos);
                    if (!NodeProcessorDungeonRoom.isValidBlock(blockState))
                        return SecretStatus.NOT_SURE;
                }
                dungeonRoom.getRoomContext().put("i-" + pos.toString(), true);
            }
            return SecretStatus.NOT_SURE;
        }
    }

    @Override
    public Set<Action> getAction(String state, DungeonRoom dungeonRoom) {
        if (state.equalsIgnoreCase("navigate")) {
            Set<Action> base;
            Set<Action> preRequisites = base = new HashSet<Action>();
            ActionMoveNearestAir actionMove = new ActionMoveNearestAir(getRepresentingPoint());
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                preRequisites.add(actionChangeState);
            }
            return base;
        }
        if (!"found".equalsIgnoreCase(state)) throw new IllegalArgumentException(state+" is not valid state for secret");
        Set<Action> base;
        Set<Action> preRequisites = base = new HashSet<Action>();
        if (secretType == SecretType.CHEST || secretType == SecretType.ESSENCE) {
            ActionClick actionClick;
            preRequisites.add(actionClick = new ActionClick(secretPoint));
            preRequisites = actionClick.getPreRequisite();
        } else if (secretType == SecretType.BAT) {
            ActionKill actionKill;
            preRequisites.add(actionKill = new ActionKill(secretPoint));
            actionKill.setPredicate(PredicateBat.INSTANCE);
            actionKill.setRadius(10);
            preRequisites = actionKill.getPreRequisite();
        }
        {
            ActionMove actionMove = new ActionMove(secretPoint);
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
        }
        {
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                ActionChangeState actionChangeState = new ActionChangeState(str.split(":")[0], str.split(":")[1]);
                preRequisites.add(actionChangeState);
            }
        }
        return base;
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = getSecretPoint().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld(getSecretType().name(), pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.375f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    public static enum SecretType {
        BAT, CHEST, ITEM_DROP, ESSENCE
    }

    @AllArgsConstructor
    @Getter
    public static enum SecretStatus {
        DEFINITELY_NOT("definitely_not"), NOT_SURE("not_sure"), CREATED("created"), FOUND("found"), ERROR("error");

        private String stateName;
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
    public OffsetPoint getRepresentingPoint() {
        return secretPoint;
    }
}
