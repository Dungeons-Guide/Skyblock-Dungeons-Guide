package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
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
        String watsaid = TextUtils.stripColor(ch2.split(":")[1]).trim();
        boolean foundMatch = false;
        for (Pattern p:patternList) {
            if (p.matcher(watsaid).matches()) {
                foundMatch = true;
                break;
            }
        }
        if (foundMatch) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §eRiddle §7:: "+ch2.split(":")[0].trim()+" §fhas the reward!"));
            final String name = TextUtils.stripColor(ch2.split(":")[0]).replace("[NPC] ","").toLowerCase();
            final BlockPos low = getDungeonRoom().getMin();
            final BlockPos high = getDungeonRoom().getMax();
            World w = getDungeonRoom().getContext().getWorld();
            List<EntityArmorStand> armor = w.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
                @Override
                public boolean apply(@Nullable EntityArmorStand input) {
                    BlockPos pos = input.getPosition();
                    return low.getX() < pos.getX() && pos.getX() < high.getX()
                            && low.getZ() < pos.getZ() && pos.getZ() < high.getZ() && input.getName().toLowerCase().contains(name);
                }
            });

            if (armor != null) {
                this.chest = null;
                BlockPos pos = armor.get(0).getPosition();
                for (BlockPos allInBox : BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
                    Block b = w.getChunkFromBlockCoords(allInBox).getBlock(allInBox);

                    if ((b == Blocks.chest || b == Blocks.trapped_chest)&& allInBox.distanceSq(pos) == 1 ) {
                        this.chest = allInBox;
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
            RenderUtils.highlightBlock(chest, new Color(0,255,0, 50),partialTicks, true);
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
