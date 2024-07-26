/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.Map;

public class MapPlayerProcessor {

   private static final Minecraft mc = Minecraft.getMinecraft();
    private final DungeonContext context;
    @Getter
    private final BiMap<String, String> mapIconToPlayerMap = HashBiMap.create();
    Logger logger = LogManager.getLogger("DG-MapPlayerProcessor");
    private int waitDelay = 0;

    public MapPlayerProcessor(DungeonContext context) {
        this.context = context;
    }

    private static void error(String prefix) {
        ChatTransmitter.addToQueue(new ChatComponentText(ChatTransmitter.prefix + prefix));
    }



    public void tick() {
        if (waitDelay < 5) {
            waitDelay++;
            return;
        }
        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(8);

        if (stack == null || !(stack.getItem() instanceof ItemMap)) {
            return;
        }

        MapData mapData = ((ItemMap) stack.getItem()).getMapData(stack, mc.theWorld);

        if (mapData != null && mapIconToPlayerMap.size() < context.getPlayers().size()) {
            getPlayersFromMap(mapData);
        }

    }


    private void getPlayersFromMap(MapData mapdata) {
        int lim = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
        lim = lim * lim;

        if (context.getScaffoldParser() == null) return;
        for (Map.Entry<String, Vec4b> stringVec4bEntry : mapdata.mapDecorations.entrySet()) {
            String mapDecString = stringVec4bEntry.getKey();
            Vec4b vec4 = stringVec4bEntry.getValue();
            if (vec4.func_176110_a() == 1) continue;

            if (!mapIconToPlayerMap.containsValue(mapDecString)) {
                int x = vec4.func_176112_b() / 2 + 64;
                int y = vec4.func_176113_c() / 2 + 64;
                BlockPos worldPos = context.getScaffoldParser().getDungeonMapLayout().mapPointToWorldPoint(new Point(x, y));
                if (Minecraft.getMinecraft().thePlayer.getDistanceSq(worldPos) > lim) continue; // too far away
                String potentialPlayer = null;

                int players = 0;

                for (String player : context.getPlayers()) {
                    if (player.equals(Minecraft.getMinecraft().thePlayer.getName())) continue;
                    if (!mapIconToPlayerMap.containsKey(player) && isPlayerNear(player, worldPos)) {
                        potentialPlayer = player;
                        players++;
                    }
                }

                if (players == 1) {
                    mapIconToPlayerMap.put(potentialPlayer, stringVec4bEntry.getKey());
                }
            }
        }
    }

    private boolean isPlayerNear(String player, BlockPos mapPos) {
        EntityPlayer entityPlayer = mc.theWorld.getPlayerEntityByName(player);

        if (entityPlayer != null && !entityPlayer.isInvisible()) {
            BlockPos pos = entityPlayer.getPosition();
            int dx = mapPos.getX() - pos.getX();
            int dz = mapPos.getZ() - pos.getZ();
            return dx * dx + dz * dz < 256; // deviation is within 16 blocks
        }

        return false;
    }
}
