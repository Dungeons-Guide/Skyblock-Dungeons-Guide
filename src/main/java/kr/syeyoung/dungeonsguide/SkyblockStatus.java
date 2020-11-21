package kr.syeyoung.dungeonsguide;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.*;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkyblockStatus {
    @Getter
    private boolean isOnSkyblock;
    @Getter
    private boolean isOnDungeon;

    @Getter
    @Setter
    private DungeonContext context;

    private final Pattern SERVER_BRAND_PATTERN = Pattern.compile("(.+) <- (?:.+)");



    public boolean isOnHypixel() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return false;
        if (!mc.isSingleplayer() && mc.thePlayer.getClientBrand() != null) {
            Matcher matcher = SERVER_BRAND_PATTERN.matcher(mc.thePlayer.getClientBrand());
            if (matcher.find())
                return matcher.group(1).equals("BungeeCord (Hypixel)");
            return false;
        }
        return false;
    }

    private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES = Sets.newHashSet("SKYBLOCK");

    public void updateStatus() {
        if (!isOnHypixel()) {
            isOnDungeon = false;
            isOnSkyblock = false;
            return;
        }

        Scoreboard scoreboard = Minecraft.getMinecraft().thePlayer.getWorldScoreboard();
        ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreObjective == null) return;

        String objectiveName = TextUtils.stripColor(scoreObjective.getDisplayName());
        boolean skyblockFound = false;
        for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) {
            if (objectiveName.startsWith(skyblock)) {
                skyblockFound = true;
                break;
            }
        }

        if (!skyblockFound) {
            isOnSkyblock = false;
            isOnDungeon = false;
            return;
        }

        Collection<Score> scores = scoreboard.getSortedScores(scoreObjective);
        boolean foundDungeon = false;
        for (Score sc:scores) {
            if (sc.getPlayerName() == null) continue;
            ScorePlayerTeam scorePlayerTeam = scoreboard.getTeam(sc.getPlayerName());
            String strippedLine = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, sc.getPlayerName()))).trim();

            if (strippedLine.contains("Dungeon Cleared: ")) {
                foundDungeon = true;
            }
        }

        isOnDungeon = foundDungeon;
    }
}
