package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.goldenpath;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.*;

public class GoldenPathRightProcessor extends GeneralDefuseChamberProcessor {
    public GoldenPathRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        center = chamber.getBlockPos(4,4,4);
    }

    @Override
    public String getName() {
        return "goldenPathRight";
    }


    private long answer = -1;
    private BlockPos center;
    // 1 up 2 right 3 down 4 left
    private static final Point vectors[] = new Point[] {
            new Point(0,1),
            new Point(-1,0),
            new Point(0, -1),
            new Point(1, 0)
    };

    private LinkedList<BlockPos> blocksolution = new LinkedList<BlockPos>();

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(answer == -1 ? "Answer not received yet. Visit left room to obtain solution" : "" , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawLines(blocksolution, Color.green, partialTicks, false);
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
