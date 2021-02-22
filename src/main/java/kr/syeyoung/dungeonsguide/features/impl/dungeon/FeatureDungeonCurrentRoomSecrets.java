package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDungeonCurrentRoomSecrets extends TextHUDFeature implements ChatListener {
    public FeatureDungeonCurrentRoomSecrets() {
        super("Dungeon", "Display #Secrets in current room", "Display what your actionbar says", "dungeon.stats.secretsroom", true, getFontRenderer().getStringWidth("Secrets: 8/8"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();


    private static final List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Secrets","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("5","currentSecrets"));
        dummyText.add(new StyledText("/","separator2"));
        dummyText.add(new StyledText("8","totalSecrets"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "currentSecrets", "separator2", "totalSecrets"
        });
    }

    @Override
    public List<StyledText> getDummyText() {
        return dummyText;
    }

    private int latestCurrSecrets = 0;
    private int latestTotalSecrets = 0;


    @Override
    public List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Secrets","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(latestCurrSecrets +"","currentSecrets"));
        actualBit.add(new StyledText("/","separator2"));
        actualBit.add(new StyledText(latestTotalSecrets +"","totalSecrets"));
        return actualBit;
    }

    @Override
    public void onChat(ClientChatReceivedEvent chat) {
        if (chat.type != 2) return;
        String text = chat.message.getFormattedText();
        if (!text.contains("/")) return;

        int secretsIndex = text.indexOf("Secrets");
        if (secretsIndex != -1) {
            int theindex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theindex = i;
                }
            }
            String it = text.substring(theindex + 2, secretsIndex - 1);

            latestCurrSecrets = Integer.parseInt(it.split("/")[0]);
            latestTotalSecrets = Integer.parseInt(it.split("/")[1]);
        }
    }
}
