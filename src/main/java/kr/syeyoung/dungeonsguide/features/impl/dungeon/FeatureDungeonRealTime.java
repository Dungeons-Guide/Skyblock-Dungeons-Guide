/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonEndListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonStartListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDungeonRealTime extends TextHUDFeature implements DungeonStartListener, DungeonEndListener {
    public FeatureDungeonRealTime() {
        super("Dungeon", "Display Real Time-Dungeon Time", "Display how much real time has passed since dungeon run started", "dungeon.stats.realtime", true, getFontRenderer().getStringWidth("Time(Real): 59m 59s"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("discriminator", new AColor(0xAA,0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("number", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
    }

    private long started = -1;

    public long getTimeElapsed() {
        return System.currentTimeMillis() - started;
    }

    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Time","title"));
        dummyText.add(new StyledText("(Real)","discriminator"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("-42h","number"));
    }

    @Override
    public boolean isHUDViewable() {
        return started != -1;
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("title", "discriminator", "separator", "number");
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Time","title"));
        actualBit.add(new StyledText("(Real)","discriminator"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(TextUtils.formatTime(getTimeElapsed()),"number"));
        return actualBit;
    }

    @Override
    public void onDungeonEnd() {
    started = -1;
    }

    @Override
    public void onDungeonStart() {
    started= System.currentTimeMillis();
    }
}
