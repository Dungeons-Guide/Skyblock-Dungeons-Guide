package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.goldenpath;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import javax.vecmath.Vector2f;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GoldenPathLeftProcessor extends GeneralDefuseChamberProcessor {
    public GoldenPathLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

    }

    @Override
    public String getName() {
        return "goldenPathLeft";
    }


    private long answer = -1;
    // 1 up 2 right 3 down 4 left
    private static final Point vectors[] = new Point[] {
            new Point(0,1),
            new Point(-1,0),
            new Point(0, -1),
            new Point(1, 0)
    };

    private LinkedList<BlockPos> blocksolution = new LinkedList<BlockPos>();
    @Override
    public void tick() {
        super.tick();
        if (answer != -1) return;

        List<Integer> solution = new ArrayList<Integer>();
        Set<BlockPos> visited = new HashSet<BlockPos>();
        BlockPos lastLoc = new BlockPos(4,0,0);
        blocksolution.add(getChamber().getBlockPos(4,1,0));
        BlockPos target = new BlockPos(4,0,5);
        while (!lastLoc.equals(target)) {
            for (int i =0; i<vectors.length; i++) {
                BlockPos target2 = lastLoc.add(vectors[i].x, 0, vectors[i].y);
                if (visited.contains(target2)) continue;
                if (target2.getX() < 0 || target2.getZ() < 0 || target2.getX() > 8 || target2.getZ() > 5) continue;

                visited.add(target2);
                if (getChamber().getBlock(target2.getX(), 0, target2.getZ()).getBlock() == Blocks.hardened_clay
                || getChamber().getBlock(target2.getX(), 0, target2.getZ()).getBlock() == Blocks.stained_hardened_clay) {
                    lastLoc = target2;
                    blocksolution.add(getChamber().getBlockPos(lastLoc.getX(), 1, lastLoc.getZ()));
                    solution.add(i);
                    break;
                }
            }
        }

        answer = 0;
        for (Integer inte:solution) {
            answer *= 4;
            answer += inte;
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (answer == -1) return;
        drawPressKey();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawLines(blocksolution, Color.green, partialTicks, false);
    }

    @Override
    public void onSendData() {
        if (answer == -1) return;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("a", "d");
        nbt.setLong("b", answer);
        getSolver().communicate(nbt);
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if ("d".equals(compound.getString("a"))) {
            answer = compound.getInteger("b");

            blocksolution.clear();
            BlockPos lastLoc = new BlockPos(4,0,0);
            blocksolution.addFirst(getChamber().getBlockPos(4,1,0));
            while (answer != 0) {
                int dir = (int) (answer % 4);
                lastLoc = lastLoc.add(vectors[dir].x, 0, vectors[dir].y);
                blocksolution.add(getChamber().getBlockPos(lastLoc.getX(), 1, lastLoc.getZ()));
                answer /= 4;
            }

            answer = compound.getInteger("b");
        }
    }
}
