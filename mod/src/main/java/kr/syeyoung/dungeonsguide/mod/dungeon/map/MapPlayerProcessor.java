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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.Map;
import net.minecraft.network.chat.Component;

public class MapPlayerProcessor {

   private static final Minecraft mc = Minecraft.getInstance();
    private final DungeonContext context;
    @Getter
    private final BiMap<String, String> mapIconToPlayerMap = HashBiMap.create();
    Logger logger = LogManager.getLogger("DG-MapPlayerProcessor");
    private int waitDelay = 0;

    public MapPlayerProcessor(DungeonContext context) {
        this.context = context;
    }

    private static void error(String prefix) {
        ChatTransmitter.addToQueue(Component.literal(ChatTransmitter.prefix + prefix));
    }



    public void tick() {
        if (waitDelay < 5) {
            waitDelay++;
            return;
        }
        ItemStack stack = mc.player.getInventory().getItem(8);

        if (stack.isEmpty() || !(stack.getItem() instanceof MapItem)) {
            return;
        }

        MapItemSavedData mapData = MapItem.getSavedData(stack, mc.level);

        if (mapData != null && mapIconToPlayerMap.size() < context.getPlayers().size()) {
            getPlayersFromMap(mapData);
        }

    }


    private void getPlayersFromMap(MapItemSavedData mapdata) {
        int lim = Minecraft.getInstance().options.renderDistance().get() * 16;
        lim = lim * lim;

        if (context.getScaffoldParser() == null) return;
        // decorations is widened by the class tweaker (D119)
        for (Map.Entry<String, MapDecoration> stringVec4bEntry : mapdata.decorations.entrySet()) {
            String mapDecString = stringVec4bEntry.getKey();
            MapDecoration vec4 = stringVec4bEntry.getValue();
            // 1.8.9's icon type 1, the green one
            if (vec4.type().is(MapDecorationTypes.FRAME)) continue;

            if (!mapIconToPlayerMap.containsValue(mapDecString)) {
                int x = vec4.x() / 2 + 64;
                int y = vec4.y() / 2 + 64;
                BlockPos worldPos = context.getScaffoldParser().getDungeonMapLayout().mapPointToWorldPoint(new Point(x, y));
                if (worldPos.distToLowCornerSqr(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ()) > lim) continue; // too far away
                String potentialPlayer = null;

                int players = 0;

                for (String player : context.getPlayers()) {
                    if (player.equals(Minecraft.getInstance().player.getGameProfile().name())) continue;
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
        Player entityPlayer = mc.level.players().stream().filter(p -> p.getGameProfile().name().equals(player)).findFirst().orElse(null);

        if (entityPlayer != null && !entityPlayer.isInvisible()) {
            BlockPos pos = entityPlayer.blockPosition();
            int dx = mapPos.getX() - pos.getX();
            int dz = mapPos.getZ() - pos.getZ();
            return dx * dx + dz * dz < 256; // deviation is within 16 blocks
        }

        return false;
    }
}
