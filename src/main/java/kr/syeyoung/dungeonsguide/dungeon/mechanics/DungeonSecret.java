package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;

@Data
public class DungeonSecret implements DungeonMechanic {
    private OffsetPoint secretPoint = new OffsetPoint(0,0,0);
    private SecretType secretType = SecretType.CHEST;

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
        return dungeonSecret;
    }
}
