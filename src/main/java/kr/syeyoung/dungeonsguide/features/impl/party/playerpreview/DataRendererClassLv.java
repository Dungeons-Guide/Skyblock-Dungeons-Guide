package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.features.impl.party.api.*;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.Arrays;

public class DataRendererClassLv implements DataRenderer {
    private DungeonClass dungeonClass;
    public DataRendererClassLv(DungeonClass dungeonClass) {
        this.dungeonClass = dungeonClass;
    }
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        ClassSpecificData<PlayerProfile.PlayerClassData> dungeonStatDungeonSpecificData = playerProfile.getPlayerClassData().get(dungeonClass);
        boolean selected = playerProfile.getSelectedClass() == dungeonClass;
//        System.out.println(playerProfile.getSelectedClass());
        if (dungeonStatDungeonSpecificData == null) {
            fr.drawString(dungeonClass.getFamilarName(), 0,0, 0xFF55ffff);
            fr.drawString("Unknown", fr.getStringWidth(dungeonClass.getFamilarName()+" "),0,0xFFFFFFFF);
            if (selected)
                fr.drawString("★", fr.getStringWidth(dungeonClass.getFamilarName()+" Unknown "),0,0xFFAAAAAA);
        } else {
            XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());
            fr.drawString(dungeonClass.getFamilarName(), 0,0, 0xFF55ffff);
            fr.drawString(xpCalcResult.getLevel()+"", fr.getStringWidth(dungeonClass.getFamilarName()+" "),0,0xFFFFFFFF);
            if (selected)
                fr.drawString("★", fr.getStringWidth(dungeonClass.getFamilarName()+" "+xpCalcResult.getLevel()+" "),0,0xFFAAAAAA);

            RenderUtils.renderBar(0, fr.FONT_HEIGHT, 100,xpCalcResult.getRemainingXp() == 0 ? 1 : (float) (xpCalcResult.getRemainingXp() / xpCalcResult.getNextLvXp()));
        }

        return new Dimension(100, fr.FONT_HEIGHT*2);
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString(dungeonClass.getFamilarName(), 0,0, 0xFF55ffff);
        fr.drawString("99", fr.getStringWidth(dungeonClass.getFamilarName()+" "),0,0xFFFFFFFF);
        fr.drawString("★", fr.getStringWidth(dungeonClass.getFamilarName()+" 99 "),0,0xFFAAAAAA);
        RenderUtils.renderBar(0, fr.FONT_HEIGHT, 100,1.0f);
        return new Dimension(100, fr.FONT_HEIGHT*2);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT*2);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {
        ClassSpecificData<PlayerProfile.PlayerClassData> dungeonStatDungeonSpecificData = playerProfile.getPlayerClassData().get(dungeonClass);
        if (dungeonStatDungeonSpecificData == null) return;
        XPUtils.XPCalcResult xpCalcResult = XPUtils.getCataXp(dungeonStatDungeonSpecificData.getData().getExperience());
        RenderUtils.drawHoveringText(Arrays.asList("§bCurrent Lv§7: §e"+xpCalcResult.getLevel(),"§bExp§7: §e"+ TextUtils.format((long)xpCalcResult.getRemainingXp()) + "§7/§e"+TextUtils.format((long)xpCalcResult.getNextLvXp()), "§bTotal Xp§7: §e"+ TextUtils.format((long)dungeonStatDungeonSpecificData.getData().getExperience())),mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj);
    }
}
