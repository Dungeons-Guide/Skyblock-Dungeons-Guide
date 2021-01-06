package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.awt.*;
import java.util.Map;

public class GeneralRoomProcessor implements RoomProcessor {

    @Getter
    @Setter
    private DungeonRoom dungeonRoom;
    public GeneralRoomProcessor(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }

    @Override
    public void tick() {

    }

    @Override
    public void drawScreen(float partialTicks) {

    }

    @Override
    public void drawWorld(float partialTicks) {
        if (FeatureRegistry.DEBUG.isEnabled() && (EditingContext.getEditingContext() == null || EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit)) {
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getDungeonRoomInfo().getMechanics().entrySet()) {
                if (value.getValue() == null) continue;;
                value.getValue().highlight(new Color(0,255,255,50), value.getKey(), dungeonRoom, partialTicks);
            }
        }
    }

    @Override
    public void chatReceived(IChatComponent chat) {

    }

    private int stack = 0;
    private long secrets2 = 0;
    @Override
    public void actionbarReceived(IChatComponent chat) {
        if (!e.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;
        if (chat.getFormattedText().contains("-")) return;
        if (dungeonRoom.getTotalSecrets() == -1) {
            e.sendDebugChat(new ChatComponentText(chat.getFormattedText().replace('§', '&') + " - received"));
        }
        BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();
        DungeonContext context = e.getDungeonsGuide().getSkyblockStatus().getContext();
        Point pt1 = context.getMapProcessor().worldPointToRoomPoint(pos.add(2, 0, 2));
        Point pt2 = context.getMapProcessor().worldPointToRoomPoint(pos.add(-2, 0, -2));
        if (!pt1.equals(pt2)) {
            stack = 0;
            secrets2 = -1;
            return;
        }
        BlockPos pos2 = dungeonRoom.getMin().add(5, 0, 5);

        String text = chat.getFormattedText();
        int secretsIndex = text.indexOf("Secrets");
        int secrets = 0;
        if (secretsIndex != -1) {
            int theindex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theindex = i;
                }
            }
            String it = text.substring(theindex + 2, secretsIndex - 1);
            secrets = Integer.parseInt(it.split("/")[1]);
        }

        if (secrets2 == secrets) stack++;
        else {
            stack = 0;
            secrets2 = secrets;
        }

        if (stack == 4 && dungeonRoom.getTotalSecrets() != secrets) {
            dungeonRoom.setTotalSecrets(secrets);
            if (FeatureRegistry.DUNGEON_INTERMODCOMM.isEnabled())
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pchat $DG-Comm " + pos2.getX() + "/" + pos2.getZ() + " " + secrets);
        }
    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }

    public static class Generator implements RoomProcessorGenerator<GeneralRoomProcessor> {
        @Override
        public GeneralRoomProcessor createNew(DungeonRoom dungeonRoom) {
            GeneralRoomProcessor defaultRoomProcessor = new GeneralRoomProcessor(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
