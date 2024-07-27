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


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorNecron;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FeatureArrowPathSolver extends SimpleFeature {
    public FeatureArrowPathSolver() {
        super("Bossfight.Floor 7","Arrow Maze Solver","Solver for Arrow Maze device", "Dungeon.Bossfight.arrowpath");
    }

    private int[][] solution = new int[5][5];
    private int[][] pendingClicks = new int[5][5];

    @DGEventHandler
    public void drawWorld(RenderWorldLastEvent event) {
        if (!isEnabled()) return;
        DungeonContext dc = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dc == null) {
            return;
        }
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;
        if (Minecraft.getMinecraft().thePlayer.getPosition().distanceSq(-2,120,75) > 400) return;

        for (int y = 0; y < 5; y++){
            for (int x = 0; x < 5; x++) {
                if (solution[y][x] == -1) continue;
                RenderUtils.drawTextAtWorldDepth((solution[y][x]-pendingClicks[y][x]) + "", -2, 120.5f + y, 75.5f + x, 0xFF00FF00, 0.03f, false, false, event.partialTicks);
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
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;


        if (Minecraft.getMinecraft().thePlayer.getPosition().distanceSq(-2,120,75) > 400) return;

        if (System.currentTimeMillis() < nextUpdate) return;
        World w = dc.getWorld();
        List<EntityItemFrame> frames = w.getEntities(EntityItemFrame.class, filter -> {
            BlockPos pos = filter.getPosition();
            if (pos.getX() != -2) return false;
            if (pos.getZ() < 75 || pos.getZ() > 79) return false;
            if (pos.getY() < 121 || pos.getY() > 125) return false;
            ItemStack itemStack = filter.getDisplayedItem();
            if (itemStack == null) return false;
            if (itemStack.getItem() == Item.getItemFromBlock(Blocks.wool) || itemStack.getItem() == Items.arrow) return true;
            return false;
        });

        int[][] mapping = new int[5][5];
        int[][] bfsAble = new int[5][5];
        int[][] solution = new int[5][5];
        Queue<Point> begin = new LinkedList<>();
        for (EntityItemFrame frame : frames) {
            int x = frame.getPosition().getZ() - 75;
            int y = frame.getPosition().getY() - 121;

            if (frame.getDisplayedItem().getItem() == Items.arrow) {
                mapping[y][x] = frame.getRotation()+1;
                bfsAble[y][x] = 9999;
            } else if (frame.getDisplayedItem().getMetadata() == EnumDyeColor.LIME.getMetadata()) {
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
        if (!(dc.getBossfightProcessor() instanceof BossfightProcessorNecron)) return;
        if (event.isAttack()) return;
        if (event.isInteractAt()) return;

        Entity e = event.getEntity();
        if (!(e instanceof EntityItemFrame)) return;
        EntityItemFrame itemFrame = (EntityItemFrame) e;
        BlockPos pos = itemFrame.getPosition();
        if (pos.getX() != -2) return;
        if (pos.getZ() < 75 || pos.getZ() > 79) return;
        if (pos.getY() < 121 || pos.getY() > 125) return;
        int y = pos.getY() - 121;
        int x = pos.getZ() - 75;

        if (((solution[y][x] - pendingClicks[y][x]) % 8 + 8) % 8 == 0) {
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
