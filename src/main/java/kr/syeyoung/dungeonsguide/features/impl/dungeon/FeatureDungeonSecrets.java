package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FeatureDungeonSecrets extends TextHUDFeature {
    public FeatureDungeonSecrets() {
        super("Dungeon", "Display #Secrets", "Display how much total secrets have been found in a dungeon run.\n+ sign means DG does not know the correct number, but it's somewhere above that number.", "dungeon.stats.secrets", true, getFontRenderer().getStringWidth("Secrets: 999/999+"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    public int getSecretsFound() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Secrets Found: §r§b")) {
                String noColor = TextUtils.stripColor(name);
                return Integer.parseInt(noColor.substring(16));
            }
        }
        return 0;
    }

    public int getTotalSecretsInt() {
        DungeonContext context = skyblockStatus.getContext();
        int totalSecrets = 0;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
        }
        return totalSecrets;
    }
    public boolean sureOfTotalSecrets() {
        DungeonContext context = skyblockStatus.getContext();
        if (context.getMapProcessor().getUndiscoveredRoom() > 0) return false;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() == -1) allknown = false;
        }
        return allknown;
    }

    public String getTotalSecrets() {
        DungeonContext context = skyblockStatus.getContext();
        int totalSecrets = 0;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
            else allknown = false;
        }
        return totalSecrets + (allknown ? "":"+");
    }


    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Secrets","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("999","currentSecrets"));
        dummyText.add(new StyledText("/","separator2"));
        dummyText.add(new StyledText("2","totalSecrets"));
        dummyText.add(new StyledText("+","unknown"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList(new String[] {
                "title", "separator", "currentSecrets", "separator2", "totalSecrets", "unknown"
        });
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Secrets","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(getSecretsFound() +"","currentSecrets"));
        actualBit.add(new StyledText("/","separator2"));
        actualBit.add(new StyledText(getTotalSecrets().replace("+", ""),"totalSecrets"));
        actualBit.add(new StyledText(getTotalSecrets().contains("+") ? "+" : "","unknown"));
        return actualBit;
    }

}
