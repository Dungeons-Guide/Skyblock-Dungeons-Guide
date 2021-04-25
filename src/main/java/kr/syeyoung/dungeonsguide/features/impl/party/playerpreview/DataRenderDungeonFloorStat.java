package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.features.impl.party.api.*;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.Arrays;

public class DataRenderDungeonFloorStat implements DataRenderer {
    private DungeonType dungeonType;
    private int floor;
    public DataRenderDungeonFloorStat(DungeonType dungeonType, int floor) {
        this.dungeonType = dungeonType;
        this.floor = floor;
    }

    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        String floorName = (dungeonType == DungeonType.CATACOMBS ? "F" : "M") + floor;

        boolean flag = false;
        DungeonSpecificData<DungeonStat> dungeonStatDungeonSpecificData = playerProfile.getDungeonStats().get(dungeonType);
        if (dungeonStatDungeonSpecificData != null) {
            FloorSpecificData<DungeonStat.PlayedFloor> playedFloorFloorSpecificData = dungeonStatDungeonSpecificData.getData().getPlays().get(floor);
            if (playedFloorFloorSpecificData != null) {
                flag = true;
                fr.drawString("§b" + floorName + " §a" + playedFloorFloorSpecificData.getData().getBestScore() + " §f" + playedFloorFloorSpecificData.getData().getCompletions() + "§7/§f" + playedFloorFloorSpecificData.getData().getWatcherKills() + "§7/§f" + playedFloorFloorSpecificData.getData().getTimes_played() + " §7(" + (int) (playedFloorFloorSpecificData.getData().getCompletions() / (double) playedFloorFloorSpecificData.getData().getWatcherKills()) + "%)", 0, 0, -1);
                fr.drawString("§6S+ §e" + (playedFloorFloorSpecificData.getData().getFastestTimeSPlus() != -1 ? TextUtils.formatTime(playedFloorFloorSpecificData.getData().getFastestTimeSPlus()) : "n/a") + " §6S §e" + (playedFloorFloorSpecificData.getData().getFastestTimeS() != -1 ? TextUtils.formatTime(playedFloorFloorSpecificData.getData().getFastestTimeS()) : "n/a"), 0, fr.FONT_HEIGHT, -1);
            }
        }
        if (!flag) {
            fr.drawString("§cNo Stat for "+floorName, 0,0,-1);
        }

        return getDimension();
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        String floorName = (dungeonType == DungeonType.CATACOMBS ? "F" : "M") + floor;


        fr.drawString("§b"+floorName+" §a305 §f10§7/§f35§7/§f50 §7("+(int)(1000.0/35.0)+"%)", 0,0,-1);
        fr.drawString("§6S+ §e10m 53s §6S §e15m 13s", 0, fr.FONT_HEIGHT, -1);
        return getDimension();
    }

    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT*2);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {

        DungeonSpecificData<DungeonStat> dungeonStatDungeonSpecificData = playerProfile.getDungeonStats().get(dungeonType);
        if (dungeonStatDungeonSpecificData == null) return;
        FloorSpecificData<DungeonStat.PlayedFloor> playedFloorFloorSpecificData = dungeonStatDungeonSpecificData.getData().getPlays().get(floor);
        if (playedFloorFloorSpecificData == null) return;
        String floorName = (dungeonType == DungeonType.CATACOMBS ? "F" : "M") + floor;

        FeatureEditPane.drawHoveringText(Arrays.asList(
                "§bFloor "+floorName,
                "§bBest Score§7: §f"+playedFloorFloorSpecificData.getData().getBestScore(),
                "§bTotal Completions§7: §f"+playedFloorFloorSpecificData.getData().getCompletions(),
                "§bTotal Watcher kills§7: §f"+playedFloorFloorSpecificData.getData().getWatcherKills(),
                "§bTotal Runs§7: §f"+playedFloorFloorSpecificData.getData().getTimes_played(),
                "§bFastest S+§7: §f"+(playedFloorFloorSpecificData.getData().getFastestTimeSPlus() != -1? TextUtils.formatTime(playedFloorFloorSpecificData.getData().getFastestTimeSPlus()) : "n/a"),
                "§bFastest S§7: §f"+(playedFloorFloorSpecificData.getData().getFastestTimeS() != -1? TextUtils.formatTime(playedFloorFloorSpecificData.getData().getFastestTimeS()) : "n/a"),
                "§bFastest Run§7: §f"+(playedFloorFloorSpecificData.getData().getFastestTime() != -1? TextUtils.formatTime(playedFloorFloorSpecificData.getData().getFastestTime()) : "n/a"),
                "§bMost Mobs Killed§7: §f"+playedFloorFloorSpecificData.getData().getMostMobsKilled(),
                "§bTotal Mobs Killed§7: §f"+playedFloorFloorSpecificData.getData().getMobsKilled()
        ), mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj);
    }
}
