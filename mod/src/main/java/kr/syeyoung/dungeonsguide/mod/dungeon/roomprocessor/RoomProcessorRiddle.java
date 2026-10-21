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

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;
import org.jetbrains.annotations.Nullable;

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
    public void chatReceived(Component chat) {
        super.chatReceived(chat);
        if (!FeatureRegistry.SOLVER_RIDDLE.isEnabled()) return;
        String ch2 = chat.getString();
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
            ChatTransmitter.addToQueue(Component.literal("§eDungeons Guide §7:: §eRiddle §7:: "+ch2.split(":")[0].trim()+" §fhas the reward!"));
            final String name = TextUtils.stripColor(ch2.split(":")[0]).replace("[NPC] ","").trim();
            final BlockPos low = getDungeonRoom().getRoomBounds().getMin();
            final BlockPos high = getDungeonRoom().getRoomBounds().getMax();
            DGWorld w = getDungeonRoom().getContext().getWorld();
            Level level = Minecraft.getInstance().level;
            // the room's bounds (D114)
            AABB roomBox = new AABB(low.getX(), level.getMinY(), low.getZ(), high.getX() + 1, level.getMaxY() + 1, high.getZ() + 1);
            List<ArmorStand> armor = level.getEntitiesOfClass(ArmorStand.class, roomBox, new Predicate<ArmorStand>() {
                @Override
                public boolean apply(@Nullable ArmorStand input) {
                    BlockPos pos = input.blockPosition();
                    // TODO: D5 entity name stopgap (D116)
                    return low.getX() < pos.getX() && pos.getX() < high.getX()
                            && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && TextUtils.stripColor(TextUtils.getFormattedText(input.getName())).equalsIgnoreCase(name);
                }
            });

            if (armor != null) {
                this.chest = null;
                BlockPos pos = armor.get(0).blockPosition();
                for (BlockPos allInBox : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 0, 1))) {
                    BlockState b = w.getBlockState(allInBox);

                    if ((LegacyBlock.CHEST.is(b) || LegacyBlock.TRAPPED_CHEST.is(b))&& allInBox.distSqr(pos) == 1 ) {
                        this.chest = allInBox.immutable();
                        return;
                    }
                }
            }

        }
    }

    BlockPos chest;

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
