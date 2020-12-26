package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoomProcessorTeleportMazeSolver extends GeneralRoomProcessor {
    private BlockPos lastPlayerLocation;

    public RoomProcessorTeleportMazeSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
    }

    private List<BlockPos> visitedPortals = new ArrayList<BlockPos>();

    @Override
    public void tick() {
        super.tick();

        World w = getDungeonRoom().getContext().getWorld();
        EntityPlayerSP entityPlayerSP = Minecraft.getMinecraft().thePlayer;
        BlockPos pos2 = new BlockPos(Math.floor(entityPlayerSP.posX), Math.floor(entityPlayerSP.posY), Math.floor(entityPlayerSP.posZ));
        Block b = w.getChunkFromBlockCoords(pos2).getBlock(pos2);
        if (b == Blocks.stone_slab || b == Blocks.stone_slab2) {
            boolean teleport = false;
            for (BlockPos allInBox : BlockPos.getAllInBox(lastPlayerLocation, pos2)) {
                if (w.getChunkFromBlockCoords(allInBox).getBlock(allInBox) == Blocks.iron_bars) {
                    teleport = true;
                    break;
                }
            }
            if (teleport) {
                for (BlockPos allInBox : BlockPos.getAllInBox(pos2.add(-1, 0, -1), pos2.add(1, 0, 1))) {
                    if (w.getChunkFromBlockCoords(allInBox).getBlock(allInBox) == Blocks.end_portal_frame) {
                        if (!visitedPortals.contains(allInBox))
                        visitedPortals.add(allInBox);
                        break;
                    }
                }
                for (BlockPos allInBox : BlockPos.getAllInBox(lastPlayerLocation.add(-1, -1, -1), lastPlayerLocation.add(1, 1, 1))) {
                    if (w.getChunkFromBlockCoords(allInBox).getBlock(allInBox) == Blocks.end_portal_frame) {
                        if (!visitedPortals.contains(allInBox))
                        visitedPortals.add(allInBox);
                        break;
                    }
                }
            }
        }

        lastPlayerLocation = pos2;
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_TELEPORT.isEnabled()) return;
        for (BlockPos bpos:visitedPortals) {
            RenderUtils.highlightBlock(bpos, new Color(255,0,0,100), partialTicks);
        }
    }
    public static class Generator implements RoomProcessorGenerator<RoomProcessorTeleportMazeSolver> {
        @Override
        public RoomProcessorTeleportMazeSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorTeleportMazeSolver defaultRoomProcessor = new RoomProcessorTeleportMazeSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
