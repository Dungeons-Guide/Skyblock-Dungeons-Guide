package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.goldenpath;

import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

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


    // 1 up 2 right 3 down 4 left
    private static final Point vectors[] = new Point[] {
            new Point(0,1),
            new Point(-1,0),
            new Point(0, -1),
            new Point(1, 0)
    };

    private LinkedList<BlockPos> blocksolution = new LinkedList<BlockPos>();
    private String goldenPathsolution;
    @Override
    public void tick() {
        super.tick();
        if (goldenPathsolution != null) return;

        List<Integer> solution = new ArrayList<Integer>();
        Set<BlockPos> visited = new HashSet<BlockPos>();
        BlockPos lastLoc = new BlockPos(4,0,0);
        visited.add(lastLoc);
        blocksolution.add(getChamber().getBlockPos(4,1,0));
        BlockPos target = new BlockPos(4,0,5);
        while (!lastLoc.equals(target)) {
            boolean solution2 = false;
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
                    solution2 = true;
                    break;
                }
            }
            if (!solution2){
                return;
            }
        }

        goldenPathsolution = "";
        for (Integer i:solution)
            goldenPathsolution += i;
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (goldenPathsolution == null) return;
        drawPressKey();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawLines(blocksolution, Color.blue, partialTicks, false);
    }

    @Override
    public void onSendData() {
        if (goldenPathsolution == null) return;
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc $DG-BDGP "+goldenPathsolution);

        ChatComponentText text = new ChatComponentText("$DG-BDGP "+goldenPathsolution);
        for (RoomProcessorBombDefuseSolver.ChamberSet ch: getSolver().getChambers()) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().chatReceived(text);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().chatReceived(text);
        }
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        super.chatReceived(chat);
        if (chat.getFormattedText().contains("$DG-BDGP ")) {
            String data = chat.getFormattedText().substring(chat.getFormattedText().indexOf("$DG-BDGP "));
            String actual = TextUtils.stripColor(data).trim().split(" ")[1].trim();

            blocksolution.clear();
            BlockPos lastLoc = new BlockPos(4,0,0);
            blocksolution.addFirst(getChamber().getBlockPos(4,1,0));
            for (Character c:actual.toCharArray()) {
                int dir = (int) (Integer.parseInt(c+"") % 4);
                lastLoc = lastLoc.add(vectors[dir].x, 0, vectors[dir].y);
                blocksolution.add(getChamber().getBlockPos(lastLoc.getX(), 1, lastLoc.getZ()));
            }
        }
    }
}
