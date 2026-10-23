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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal;


import net.minecraft.world.phys.AABB;
import net.minecraft.tags.ItemTags;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorMasterModeNecron;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorNecron;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEvent;
import java.util.Arrays;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FeatureArrowPathSolver extends SimpleFeature {
    public FeatureArrowPathSolver() {
        super("Bossfight.Floor 7","Arrow Maze Solver","Solver for Arrow Maze device", "Dungeon.Bossfight.arrowpath");
        addParameter("cancelwrongclick", new FeatureParameter<>("cancelwrongclick", "Block clicks on complete arrows", "", true, TCBoolean.INSTANCE, nval -> cancelwrongclick = nval));
        addParameter("depth", new FeatureParameter<>("depth", "Disable depth", "", false, TCBoolean.INSTANCE, nval -> depth = nval));
    }

    private boolean depth;
    private boolean cancelwrongclick;

    private int[][] solution = new int[5][5];
    private int[][] pendingClicks = new int[5][5];

    @DGEventHandler
    public void drawWorld(RenderWorldEvent event) {
        if (!isEnabled()) return;
        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) {
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron || dc.getBossfightProcessor() instanceof BossfightProcessorMasterModeNecron)) return;

        if (depth && Minecraft.getInstance().player.blockPosition().distToLowCornerSqr(-2,120,75) > 400) return;
        if (!depth && Minecraft.getInstance().player.blockPosition().distToLowCornerSqr(-2,120,75) > 25) return;

        for (int y = 0; y < 5; y++){
            for (int x = 0; x < 5; x++) {
                if (solution[y][x] == -1) continue;
                if (depth)
                    RenderUtils.drawTextAtWorldDepth((solution[y][x]-pendingClicks[y][x]) + "", -1.7f, 120.5f + y, 75.5f + x, 0xFF00FF00, 0.03f, false, false, event.partialTicks);
                else
                    RenderUtils.drawTextAtWorld((solution[y][x]-pendingClicks[y][x]) + "", -1.7f, 120.5f + y, 75.5f + x, 0xFF00FF00, 0.03f, false, false, event.partialTicks);
            }
        }
    }
    private boolean wasButton = false;
    private long nextUpdate = 0;
    @DGEventHandler
    public void onTick(DGTickEvent tickEvent) {
        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) {
            wasButton = false;
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron || dc.getBossfightProcessor() instanceof BossfightProcessorMasterModeNecron)) return;


        if (Minecraft.getInstance().player.blockPosition().distToLowCornerSqr(-2,120,75) > 400) return;

        if (System.currentTimeMillis() < nextUpdate) return;
        // the frames' blocks: x -2, y 121..125, z 75..79 (D114)
        List<ItemFrame> frames = Minecraft.getInstance().level.getEntitiesOfClass(ItemFrame.class, new AABB(-2, 121, 75, -1, 126, 80), filter -> {
            BlockPos pos = filter.blockPosition();
            if (pos.getX() != -2) return false;
            if (pos.getZ() < 75 || pos.getZ() > 79) return false;
            if (pos.getY() < 121 || pos.getY() > 125) return false;
            ItemStack itemStack = filter.getItem();
            if (itemStack.isEmpty()) return false;
            // 1.8.9's wool item was every color
            if (itemStack.is(ItemTags.WOOL) || itemStack.is(Items.ARROW)) return true;
            return false;
        });

        int[][] mapping = new int[5][5];
        int[][] bfsAble = new int[5][5];
        int[][] solution = new int[5][5];
        Queue<Point> begin = new LinkedList<>();
        for (ItemFrame frame : frames) {
            int x = frame.blockPosition().getZ() - 75;
            int y = frame.blockPosition().getY() - 121;

            if (frame.getItem().is(Items.ARROW)) {
                mapping[y][x] = frame.getRotation()+1;
                bfsAble[y][x] = 9999;
            // 1.8.9's lime wool metadata
            } else if (frame.getItem().is(Items.LIME_WOOL)) {
                mapping[y][x] = 10; // starting
                bfsAble[y][x] = 99999;
            } else {
                mapping[y][x] = 11; // end
                bfsAble[y][x] = 1;
                begin.add(new Point(x,y));
            }
        }
        // do bfs.
        while (!begin.isEmpty()) {
            Point p = begin.poll();
            int dist = bfsAble[p.y][p.x];
            if (p.x > 0) {
                Point toChk = new Point(p.x - 1, p.y);
                if (bfsAble[toChk.y][toChk.x] == 9999) { // haven't visited yet.
                    bfsAble[toChk.y][toChk.x] = dist + 1;
                    begin.add(toChk);
                    int currSolution = solution[toChk.y][toChk.x];
                    if ((currSolution - mapping[toChk.y][toChk.x] + 8) % 8 > (6 - mapping[toChk.y][toChk.x] + 8) % 8 || currSolution == 0) {
                        solution[toChk.y][toChk.x] = 6;
                    }
                }
            }
            if (p.y > 0) {
                Point toChk = new Point(p.x , p.y - 1);
                if (bfsAble[toChk.y][toChk.x] == 9999) { // haven't visited yet.
                    bfsAble[toChk.y][toChk.x] = dist + 1;
                    begin.add(toChk);
                    int currSolution = solution[toChk.y][toChk.x];
                    if ((currSolution - mapping[toChk.y][toChk.x] + 8) % 8 > (8 - mapping[toChk.y][toChk.x] + 8) % 8 || currSolution == 0) {
                        solution[toChk.y][toChk.x] = 8;
                    }
                }
            }
            if (p.x < 4) {
                Point toChk = new Point(p.x + 1, p.y);
                if (bfsAble[toChk.y][toChk.x] == 9999) { // haven't visited yet.
                    bfsAble[toChk.y][toChk.x] = dist + 1;
                    begin.add(toChk);
                    int currSolution = solution[toChk.y][toChk.x];
                    if ((currSolution - mapping[toChk.y][toChk.x] + 8) % 8 > (2 - mapping[toChk.y][toChk.x] + 8) % 8 || currSolution == 0) {
                        solution[toChk.y][toChk.x] = 2;
                    }
                }
            }
            if (p.y < 4) {
                Point toChk = new Point(p.x, p.y + 1);
                if (bfsAble[toChk.y][toChk.x] == 9999) { // haven't visited yet.
                    bfsAble[toChk.y][toChk.x] = dist + 1;
                    begin.add(toChk);
                    int currSolution = solution[toChk.y][toChk.x];
                    if ((currSolution - mapping[toChk.y][toChk.x] + 8) % 8 > (4 - mapping[toChk.y][toChk.x] + 8) % 8 || currSolution == 0) {
                        solution[toChk.y][toChk.x] = 4;
                    }
                }
            }
        }

        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
//                this.solution[y][x] = solution[y][x];
                if (mapping[y][x] == 0 || mapping[y][x] == 10 || mapping[y][x] == 11) {
                    this.solution[y][x] = -1;
                } else {
                    this.solution[y][x] = (solution[y][x] - mapping[y][x] + 8) % 8;
                }
                pendingClicks[y][x] = 0;
            }
        }


    }

    @DGEventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!isEnabled()) return;

        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) return;
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron || dc.getBossfightProcessor() instanceof BossfightProcessorMasterModeNecron)) return;
        if (event.isAttack()) return;
        if (event.isInteractAt()) return;

        Entity e = event.getEntity();
        if (!(e instanceof ItemFrame)) return;
        ItemFrame itemFrame = (ItemFrame) e;
        BlockPos pos = itemFrame.blockPosition();
        if (pos.getX() != -2) return;
        if (pos.getZ() < 75 || pos.getZ() > 79) return;
        if (pos.getY() < 121 || pos.getY() > 125) return;
        int y = pos.getY() - 121;
        int x = pos.getZ() - 75;

        if (((solution[y][x] - pendingClicks[y][x]) % 8 + 8) % 8 == 0) {
            if (cancelwrongclick)
                event.setCanceled(true);
            // prevent click.
        } else {
            pendingClicks[y][x] ++;
            nextUpdate = System.currentTimeMillis() + 1000;
        }
//        World w = dc.getWorld();
//
//        BlockPos pos = event.pos.add(1,0,0);
//        if (120 <= pos.getY() && pos.getY() <= 123 && pos.getX() == 111 && 92 <= pos.getZ() && pos.getZ() <= 95) {
//            if (w.getBlockState(event.pos).getBlock() != Blocks.stone_button) return;
//            if (pos.equals(orderClick.peek())) {
//                orderClick.poll();
//            }
//        }
    }
}
