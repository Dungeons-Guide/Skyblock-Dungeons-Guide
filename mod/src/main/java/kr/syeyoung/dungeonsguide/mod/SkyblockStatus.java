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

package kr.syeyoung.dungeonsguide.mod;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.HypixelJoinedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.SkyblockJoinedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.SkyblockLeftEvent;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Objective;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.Score;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collection;
import java.util.Set;

public class SkyblockStatus {
    boolean wasOnHypixel = false;
    public static String locationName;


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent ev) {
        if (ev.side == Side.SERVER || ev.phase != TickEvent.Phase.START) return;

        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        boolean isOnDungeonPrev = isOnDungeon();
        boolean isOnSkyblockPrev = isOnSkyblock();
        skyblockStatus.updateStatus();

        if (!wasOnHypixel && skyblockStatus.isOnHypixel()) {
            MinecraftForge.EVENT_BUS.post(new HypixelJoinedEvent());
        }
        wasOnHypixel = skyblockStatus.isOnHypixel();

        if (isOnSkyblockPrev && !isOnSkyblock()) {
            MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
        } else if (!isOnSkyblockPrev && isOnSkyblock()) {
            MinecraftForge.EVENT_BUS.post(new SkyblockJoinedEvent());
        }

        if (isOnDungeonPrev && !isOnDungeon()) {
            MinecraftForge.EVENT_BUS.post(new DungeonLeftEvent());
        }



    }

    public static boolean isOnSkyblock(){
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

        return skyblockStatus != null && skyblockStatus.isOnSkyblock;
    }

    public static boolean isOnDungeon() {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();



        return skyblockStatus != null && (skyblockStatus.forceIsOnDungeon || skyblockStatus.isOnDungeon);
    }


    private boolean isOnSkyblock;
    private boolean isOnDungeon;

    @Getter
    @Setter
    private boolean forceIsOnDungeon;

    public static boolean isOnHypixel() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return false;
        if (!mc.isSingleplayer() && mc.thePlayer.getClientBrand() != null) {
            return mc.thePlayer.getClientBrand().startsWith("Hypixel BungeeCord");
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

        Objective objective = ScoreboardManager.INSTANCE.getSidebarObjective();
        if (objective == null) return;

        String objectiveName = TextUtils.stripColor(objective.getDisplayName());
        boolean skyblockFound = false;
        for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) {
            if (objectiveName.startsWith(skyblock)) {
                skyblockFound = true;
                isOnSkyblock = true;
                break;
            }
        }

        if (!skyblockFound) {
            isOnSkyblock = false;
            isOnDungeon = false;
            return;
        }

        boolean foundDungeon = false;

        Collection<Score> scores = objective.getScores();
        for (Score sc : scores) {
            String strippedLine = TextUtils.keepScoreboardCharacters(
                    TextUtils.stripColor(sc.getJustTeam())).trim();
            if (strippedLine.contains("Cleared: ")) {
                foundDungeon = true;
                DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
                if(context != null){
                    context.setPercentage(Integer.parseInt(
                            strippedLine.substring(9).split(" ")[0]));
                }
            }
            if (sc.getJustTeam().startsWith(" §7⏣")) {
                locationName = strippedLine.trim();
            }
        }

        if (locationName != null)
            isOnDungeon = locationName.startsWith("The Catacombs") | foundDungeon;
        else
            isOnDungeon=false;
    }

}
