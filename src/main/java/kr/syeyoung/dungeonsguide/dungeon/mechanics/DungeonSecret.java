package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.action.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates.PredicateBat;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        } else {
            return SecretStatus.NOT_SURE;
        }
    }

    @Override
    public Set<Action> getAction(String state, DungeonRoom dungeonRoom) {
        if (!"claimed".equalsIgnoreCase(state)) throw new IllegalArgumentException(state+" is not valid state for secret");
        Set<Action> base;
        Set<Action> preRequisites = base = new HashSet<Action>();
        if (secretType == SecretType.CHEST) {
            ActionClick actionClick;
            preRequisites.add(actionClick = new ActionClick(secretPoint));
            preRequisites = actionClick.getPreRequisite();
        } else if (secretType == SecretType.BAT) {
            ActionKill actionKill;
            preRequisites.add(actionKill = new ActionKill(secretPoint));
            actionKill.setPredicate(PredicateBat.INSTANCE);
            preRequisites = actionKill.getPreRequisite();
        }
        {
            ActionMove actionMove = new ActionMove(secretPoint);
            preRequisites.add(actionMove);
            preRequisites = actionMove.getPreRequisite();
        }
        {
            for (String str : preRequisite) {
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
        RenderUtils.drawTextAtWorld(getSecretType().name(), pos.getX() +0.5f, pos.getY()+1f, pos.getZ()+0.5f, 0xFF000000, 2f, true, false, partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0f, pos.getZ()+0.5f, 0xFF000000, 2f, true, false, partialTicks);
    }

    public static enum SecretType {
        BAT, CHEST, ITEM_DROP
    }

    public static enum SecretStatus {
        DEFINITELY_NOT, NOT_SURE, CREATED, FOUND, ERROR
    }

    public DungeonSecret clone() throws CloneNotSupportedException {
        DungeonSecret dungeonSecret = new DungeonSecret();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
        dungeonSecret.secretType = secretType;
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        return dungeonSecret;
    }
}
