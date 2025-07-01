/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.goldenpath;

import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.BlockType;
import net.kyori.adventure.text.Component;

import java.awt.*;
import java.util.List;
import java.util.*;

public class GoldenPathLeftProcessor extends GeneralDefuseChamberProcessor {
    public GoldenPathLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

    }

    @Override
    public String getName() {
        return "goldenPathLeft";
    }


    // 1 up 2 right 3 down 4 left
    private static final Point[] vectors = new Point[] {
            new Point(0,1),
            new Point(-1,0),
            new Point(0, -1),
            new Point(1, 0)
    };

    private final LinkedList<VectorI3D> blocksolution = new LinkedList<>();
    private String goldenPathsolution;
    @Override
    public void tick() {
        super.tick();
        if (goldenPathsolution != null) return;

        List<Integer> solution = new ArrayList<Integer>();
        Set<VectorI3D> visited = new HashSet<>();
        VectorI3D lastLoc = new VectorI3D(4,0,0);
        visited.add(lastLoc);
        blocksolution.add(getChamber().getBlockPos(4,1,0));
        VectorI3D target = new VectorI3D(4,0,5);
        while (!lastLoc.equals(target)) {
            boolean solution2 = false;
            for (int i =0; i<vectors.length; i++) {
                VectorI3D target2 = lastLoc.add(vectors[i].x, 0, vectors[i].y);
                if (visited.contains(target2)) continue;
                if (target2.getX() < 0 || target2.getZ() < 0 || target2.getX() > 8 || target2.getZ() > 5) continue;

                visited.add(target2);
                if (getChamber().getBlock(target2.getX(), 0, target2.getZ()).isOf(BlockType.HARDENED_CLAY, BlockType.STAINED_HARDENED_CLAY)) {
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
        RenderUtils.drawLines(blocksolution, new AColor(0,0,255,0), 1,partialTicks, false);
    }

    @Override
    public void onSendData() {
        if (goldenPathsolution == null) return;
        ChatProcessor.INSTANCE.addToChatQueue("/pc $DG-BDGP "+goldenPathsolution, null, false);

        DGChatReceivedEvent text = new DGChatReceivedEvent("$DG-BDGP "+goldenPathsolution, Component.text("$DG-BDGP "+goldenPathsolution), Component.text("$DG-BDGP "+goldenPathsolution), false);
        for (RoomProcessorBombDefuseSolver.ChamberSet ch: getSolver().getChambers()) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().chatReceived(text);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().chatReceived(text);
        }
    }

    @Override
    public void chatReceived(DGChatReceivedEvent chat) {
        super.chatReceived(chat);
        if (chat.getOriginalFormattedText().contains("$DG-BDGP ")) {
            String data = chat.getOriginalFormattedText().substring(chat.getOriginalFormattedText().indexOf("$DG-BDGP "));
            String actual = TextUtils.stripColor(data).trim().split(" ")[1].trim();

            blocksolution.clear();
            VectorI3D lastLoc = new VectorI3D(4,0,0);
            blocksolution.addFirst(getChamber().getBlockPos(4,1,0));
            for (Character c:actual.toCharArray()) {
                int dir = Integer.parseInt(c+"") % 4;
                lastLoc = lastLoc.add(vectors[dir].x, 0, vectors[dir].y);
                blocksolution.add(getChamber().getBlockPos(lastLoc.getX(), 1, lastLoc.getZ()));
            }
        }
    }
}
