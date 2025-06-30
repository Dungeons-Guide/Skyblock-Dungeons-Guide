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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UWorld;
import net.minecraft.util.IChatComponent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class RoomProcessorRiddle extends GeneralRoomProcessor {

    public RoomProcessorRiddle(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
    }

    private static final List<Pattern> patternList = Arrays.asList(
            Pattern.compile("My chest doesn't have the reward. We are all telling the truth.*"),
            Pattern.compile("The reward isn't in any of our chests.*"),
            Pattern.compile("The reward is not in my chest!.*"),
            Pattern.compile("At least one of them is lying, and the reward is not in .+'s chest.*"),
            Pattern.compile("Both of them are telling the truth. Also,.+has the reward in their chest.*"),
            Pattern.compile("My chest has the reward and I'm telling the truth.*")
    );

    @Override
    public void chatReceived(IChatComponent chat) {
        super.chatReceived(chat);
        if (!FeatureRegistry.SOLVER_RIDDLE.isEnabled()) return;
        String ch2 = chat.getUnformattedText();
        if (!ch2.startsWith("§e[NPC] ")) {
            return;
        }
        String riddleHint = TextUtils.stripColor(ch2.split(":")[1]).trim();
        boolean foundMatch = false;
        for (Pattern p:patternList) {
            if (p.matcher(riddleHint).matches()) {
                foundMatch = true;
                break;
            }
        }
        if (foundMatch) {
            ChatTransmitter.addToQueue("§eDungeons Guide §7:: §eRiddle §7:: "+ch2.split(":")[0].trim()+" §fhas the reward!");
            final String name = TextUtils.stripColor(ch2.split(":")[0]).replace("[NPC] ","").trim();
            final VectorI3D low = getDungeonRoom().getRoomBounds().getMin();
            final VectorI3D high = getDungeonRoom().getRoomBounds().getMax();
            UWorld w = getDungeonRoom().getContext().getUworld();
            List<UEntity> armor = w.getEntitiesWithinAabb(EntityType.ARMOR_STAND,
                    new AABB(low.getX(), 0, low.getZ(), high.getX(), 255, high.getZ()));
            UEntity target = null;
            for (UEntity uEntity : armor) {
                if (TextUtils.stripColor(uEntity.getName()).equalsIgnoreCase(name)) {
                    target = uEntity;
                }
            }

            if (target != null) {
                this.chest = null;
                VectorI3D pos = target.getPosition();
                for (VectorI3D allInBox : VectorI3D.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
                    UBlockState b = w.getBlockStateAt(allInBox);

                    if ((b.isOf(BlockType.CHEST, BlockType.TRAP_CHEST)) && allInBox.distanceSq(pos) == 1 ) {
                        this.chest = allInBox;
                        return;
                    }
                }
            }

        }
    }

    VectorI3D chest;

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_RIDDLE.isEnabled()) return;
        if (chest != null) {
            RenderUtils.highlightBoxAColor(new AABB(chest.getX(), chest.getY(), chest.getZ(), chest.getX()+1, chest.getY() + 1, chest.getZ() + 1),  FeatureRegistry.SOLVER_RIDDLE.getTargetColor(), partialTicks, true);
        }
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorRiddle> {
        @Override
        public RoomProcessorRiddle createNew(DungeonRoom dungeonRoom) {
            RoomProcessorRiddle defaultRoomProcessor = new RoomProcessorRiddle(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
