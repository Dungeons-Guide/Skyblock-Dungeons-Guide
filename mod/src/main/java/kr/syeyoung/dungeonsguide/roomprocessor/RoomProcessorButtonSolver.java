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

package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.util.Arrays;

public class RoomProcessorButtonSolver extends GeneralRoomProcessor {
    public RoomProcessorButtonSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        OffsetPointSet ops = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("buttons");
        if (ops == null) {
            bugged = true;
            return;
        }

        buttons = new BlockPos[12];
        woods = new BlockPos[12];
        for (int i = 0; i < ops.getOffsetPointList().size(); i++) {
            buttons[i] = ops.getOffsetPointList().get(i).getBlockPos(dungeonRoom);
            woods[i] = buttons[i].add(0,-1,0);
        }
    }

    private boolean bugged;

    private BlockPos[] buttons;
    private BlockPos[] woods;

    private long clicked;
    private int clickedButton = -1;

    private final int[] result = new int[12];

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        super.onInteractBlock(event);
        if (bugged) return;

        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        for (int i = 0; i < buttons.length; i++) {
            if (event.pos.equals(buttons[i])) {
                clicked = System.currentTimeMillis();
                clickedButton = i;
                return;
            }
        }
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        super.chatReceived(chat);
        if (bugged) return;

        if (clickedButton == -1) return;
        if (clicked + 500 < System.currentTimeMillis()) return;

        String msg = chat.getFormattedText();
        if (msg.equals("§r§cThis button doesn't seem to do anything...§r")) {
            result[clickedButton] = -1;
            clickedButton = -1;
        } else if (msg.equals("§r§aThis button seems connected to something§r")) {
            Arrays.fill(result, -1);
            if (clickedButton % 4 != 0) result[clickedButton - 1] = 1;
            if (clickedButton % 4 != 3) result[clickedButton + 1] = 1;
            clickedButton = -1;
        } else if (msg.equals("§r§aClick! you Hear the sound of a door opening§r")) {
            Arrays.fill(result, -1);
            result[clickedButton] = 2;
            clickedButton = -1;
        } else if (msg.equals("§r§aWrong button, looks like the system reset!§r")) {
            Arrays.fill(result, 0);
            clickedButton = -1;
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (bugged) return;
        if (Minecraft.getMinecraft().thePlayer.getPosition().distanceSq(woods[6]) > 100) return;


        for (int i = 0; i < woods.length; i++) {
            int data = result[i];
            BlockPos pos = woods[i];

            if (data == 0) {
                RenderUtils.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, false);
            } else if (data == -1) {
                RenderUtils.highlightBlock(pos, new Color(255, 0, 0, 50), partialTicks, false);
            } else if (data == 1) {
                RenderUtils.highlightBlock(pos, new Color(0, 255, 0, 50), partialTicks, false);
            } else if (data == 2) {
                RenderUtils.highlightBlock(pos, new Color(0, 255, 0, 100), partialTicks, false);
            }
        }
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorButtonSolver> {
        @Override
        public RoomProcessorButtonSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorButtonSolver defaultRoomProcessor = new RoomProcessorButtonSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
