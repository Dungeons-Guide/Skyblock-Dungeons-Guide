/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorNecron;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.MarkerData;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonEndedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.TitleEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.waypoints.WidgetTerminalWaypointsEditor;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.WidgetLinePropertiesEditor;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import javax.vecmath.Vector2d;
import java.util.*;

public class FeatureF7TerminalWaypoints extends SimpleFeature {
    public FeatureF7TerminalWaypoints() {
        super("Bossfight", "Terminal Waypoints", "Render terminal waypoints in world and map\n(Configure inside the feature)", "bossfight.f7waypoints");
        this.setEnabled(true);

        addParameter("beacons", new FeatureParameter<Boolean>("beacons", "Highlight waypoints in world", "Render waypoints in the world", true, TCBoolean.INSTANCE, nval -> beacon = nval));
        addParameter("beaconBeam", new FeatureParameter<Boolean>("beacomBeam", "Enable beacon beam", "Render beacon beam along with waypoints", false, TCBoolean.INSTANCE, nval -> beam = nval));
        addParameter("beamColor", new FeatureParameter<AColor>("beamColor", "Beam Color", "Color of the beacon beam", new AColor(0x77FF0000, true), TCAColor.INSTANCE, nval -> beamColor = nval));
        addParameter("beamTargetColor", new FeatureParameter<AColor>("beamTargetColor", "Target Color", "Color of the target", new AColor(0x33FF0000, true), TCAColor.INSTANCE, nval -> highlightColor = nval));
        addParameter("status", new FeatureParameter<Boolean>("status", "Render terminal status on beacon", "", true, TCBoolean.INSTANCE, nval -> status = nval));
        addParameter("all", new FeatureParameter<Boolean>("all", "Only render current sub-phase", "Only render waypoints that are in current sub-phase", true, TCBoolean.INSTANCE, nval -> all = nval));
    }

    @Getter
    private boolean beacon;
    @Getter
    private boolean beam;
    @Getter
    private boolean status;
    @Getter
    private AColor beamColor;
    @Getter
    private AColor highlightColor;

    private boolean all;

    public BossfightProcessorNecron getProcessor() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null) return null;
        BossfightProcessor processor = context.getBossfightProcessor();
        if (!(processor instanceof BossfightProcessorNecron)) return null;
        return (BossfightProcessorNecron) processor;
    }

    @DGEventHandler
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!beacon) return;
        BossfightProcessorNecron necron = getProcessor();
        if (necron == null) return;
        if (!necron.getCurrentPhase().startsWith("goldor-terminals")) return;
        List<WaypointData> allWaypts = new ArrayList<>();
        if (!all) {
            for (List<WaypointData> value : waypoints.values()) {
                allWaypts.addAll(value);
            }
        } else {
            allWaypts .addAll( waypoints.get(necron.getCurrentPhase()));
        }


        for (WaypointData allWaypt : allWaypts) {
            if (beam) {
                RenderUtils.renderBeaconBeam(allWaypt.x, allWaypt.y, allWaypt.z, beamColor, event.partialTicks);
            }
            if (beacon) {
                GlStateManager.pushMatrix();
                RenderUtils.highlightBlock(new BlockPos(allWaypt.x, allWaypt.y, allWaypt.z), highlightColor, event.partialTicks, false);
                GlStateManager.popMatrix();
            }

            if (status) {
                if (completedTerminals.containsKey(allWaypt.id)) {
                    RenderUtils.drawTextAtWorld("Done", allWaypt.x + 0.5f, allWaypt.y + 0.5f, allWaypt.z + 0.5f
                            , 0xFF00FF00, 1f, true, false, event.partialTicks);
                } else if (nearPlayer.get(allWaypt.id) != null) {
                    RenderUtils.drawTextAtWorld(nearPlayer.get(allWaypt.id), allWaypt.x + 0.5f, allWaypt.y + 0.5f, allWaypt.z + 0.5f
                            , 0xFFFFFF00, 1f, true, false, event.partialTicks);
                } else {
                    RenderUtils.drawTextAtWorld("Incomplete", allWaypt.x + 0.5f, allWaypt.y + 0.5f, allWaypt.z + 0.5f
                            , 0xFFFF0000, 1f, true, false, event.partialTicks);
                }
            }
        }
        
    }

    private HashMap<String, String> completedTerminals = new HashMap<>();
    private HashMap<String, String> nearPlayer = new HashMap<>();


    @DGEventHandler
    public void onDungeonQuit(DungeonLeftEvent event) {
        completedTerminals.clear();
        nearPlayer.clear();
    }


    long nextRefresh;
    Set<TabListEntry> playerListCached;

    public Set<TabListEntry> getPlayerListCached(){
        if(playerListCached == null || nextRefresh <= System.currentTimeMillis()){
            ChatTransmitter.sendDebugChat("Refreshing players on map");
            playerListCached = TabList.INSTANCE.getTabListEntries();
            nextRefresh = System.currentTimeMillis() + 10000;
        }
        return playerListCached;
    }

    @DGEventHandler
    public void onTick(DGTickEvent event) {
        BossfightProcessorNecron necron = getProcessor();
        if (necron == null) return;
        if (!necron.getCurrentPhase().startsWith("goldor-terminals")) return;


        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        Set<TabListEntry> playerList = getPlayerListCached();


        nearPlayer.clear();

        // 21 iterations bc we only want to scan the player part of tab list
        int i = 0;
        for (TabListEntry playerInfo : playerList) {
            if (++i >= 20) break;

            String name = TabListUtil.getPlayerNameWithChecks(playerInfo);
            if (name == null) continue;


            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);

            for (List<WaypointData> value : waypoints.values()) {
                for (WaypointData waypointData : value) {
                    if (entityplayer.getDistanceSq(waypointData.x, waypointData.y, waypointData.z) < 25) {
                        nearPlayer.put(waypointData.id, entityplayer.getName());
                    }
                }
            }
        }
    }

    @DGEventHandler
    public void onMessage(ClientChatReceivedEvent event) {
        String txt = event.message.getFormattedText();
        String player = TextUtils.stripColor(txt.split(" ")[0]);
        if (txt.contains("§r§a completed a device! (§r§c")) {
            EntityPlayer player1 = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(player);
            if (player1 == null) {
                System.out.println("umm no player found named "+player);
                return;
            }
            int minDist = 99999999;
            String minDistId = null;
            for (List<WaypointData> value : waypoints.values()) {
                for (WaypointData waypointData : value) {
                    if (waypointData.type == WaypointData.WaypointType.LAMP || waypointData.type == WaypointData.WaypointType.ARROW
                    || waypointData.type == WaypointData.WaypointType.PATH || waypointData.type == WaypointData.WaypointType.SIMON) {
                        int dist = (int) player1.getDistanceSq(waypointData.x, waypointData.y, waypointData.z);
                        if (dist < minDist) {
                            minDistId = waypointData.id;
                            minDist = dist;
                        }
                    }
                }
            }

            completedTerminals.put(minDistId, player);
        } else if (txt.contains("§r§a activated a lever! (§r§c")) {
            EntityPlayer player1 = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(player);
            if (player1 == null) {
                System.out.println("umm no player found named "+player);
                return;
            }
            int minDist = 99999999;
            String minDistId = null;
            for (List<WaypointData> value : waypoints.values()) {
                for (WaypointData waypointData : value) {
                    if (waypointData.type == WaypointData.WaypointType.LEVER) {
                        int dist = (int) player1.getDistanceSq(waypointData.x, waypointData.y, waypointData.z);
                        if (dist < minDist) {
                            minDistId = waypointData.id;
                            minDist = dist;
                        }
                    }
                }
            }

            completedTerminals.put(minDistId, player);
        } else if (txt.contains("§r§a activated a terminal! (§r§c")) {
            EntityPlayer player1 = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(player);
            if (player1 == null) {
                System.out.println("umm no player found named "+player);
                return;
            }
            int minDist = 99999999;
            String minDistId = null;
            for (List<WaypointData> value : waypoints.values()) {
                for (WaypointData waypointData : value) {
                    if (waypointData.type == WaypointData.WaypointType.TERMINAL) {
                        int dist = (int) player1.getDistanceSq(waypointData.x, waypointData.y, waypointData.z);
                        if (dist < minDist) {
                            minDistId = waypointData.id;
                            minDist = dist;
                        }
                    }
                }
            }

            completedTerminals.put(minDistId, player);
        }
    }

    @Override
    public Widget getConfigureWidget() {
        return new WidgetTerminalWaypointsEditor(this);
    }

    public void addMarkers(List<MarkerData> markers) {
        BossfightProcessorNecron necron = getProcessor();
        if (necron == null) return;
        if (!necron.getCurrentPhase().startsWith("goldor-terminals")) return;
        List<WaypointData> allWaypts = new ArrayList<>();
        if (!all) {
            for (List<WaypointData> value : waypoints.values()) {
                allWaypts.addAll(value);
            }
        } else {
            allWaypts .addAll( waypoints.get(necron.getCurrentPhase()));
        }

        for (WaypointData allWaypt : allWaypts) {
            int idx = 0;
            if (allWaypt.type == WaypointData.WaypointType.TERMINAL)
                idx = 63;
            else if (allWaypt.type == WaypointData.WaypointType.LEVER)
                idx = 55;
            else
                idx = 15;

            if (completedTerminals.containsKey(allWaypt.id)) {
                idx -= 1;
            }

            markers.add(new MarkerData(
                    allWaypt.id+"/"+nearPlayer.get(allWaypt.id), MarkerData.MobType.TERMINALS, idx, 16, 16,
                    allWaypt.x + 0.5, allWaypt.z + 0.5, allWaypt.x + 0.5, allWaypt.z + 0.5,
                    0, 0
            ));
        }
    }


    @Data @AllArgsConstructor
    private static class WaypointData {
        private int x;
        private int y;
        private int z;
        private WaypointType type;
        private String id;
        
        private enum WaypointType {
            TERMINAL, LEVER, SIMON, LAMP, PATH, ARROW
        }
    }
    
    private static Map<String, List<WaypointData>> waypoints = new HashMap<>();
    
    static {
        List<WaypointData> phase = new ArrayList<>();
        phase.add(new WaypointData(111, 113, 73, WaypointData.WaypointType.TERMINAL, "gt11"));
        phase.add(new WaypointData(111, 119, 79, WaypointData.WaypointType.TERMINAL, "gt12"));
        phase.add(new WaypointData(111, 121, 91, WaypointData.WaypointType.SIMON, "gt13"));
        phase.add(new WaypointData(89, 112, 92, WaypointData.WaypointType.TERMINAL, "gt14"));
        phase.add(new WaypointData(89, 122, 101, WaypointData.WaypointType.TERMINAL, "gt15"));
        phase.add(new WaypointData(106, 123, 113, WaypointData.WaypointType.LEVER, "gt16"));
        phase.add(new WaypointData(94, 123, 113, WaypointData.WaypointType.LEVER, "gt17"));
        waypoints.put("goldor-terminals-1", phase);
        phase = new ArrayList<>();
        phase.add(new WaypointData(68, 109, 121, WaypointData.WaypointType.TERMINAL, "gt21"));
        phase.add(new WaypointData(59, 120, 123, WaypointData.WaypointType.TERMINAL, "gt22"));
        phase.add(new WaypointData(61, 131, 142, WaypointData.WaypointType.LAMP, "gt23"));
        phase.add(new WaypointData(47, 109, 121, WaypointData.WaypointType.TERMINAL, "gt24"));
        phase.add(new WaypointData(39, 108, 143, WaypointData.WaypointType.TERMINAL, "gt25"));
        phase.add(new WaypointData(27, 123, 127, WaypointData.WaypointType.LEVER, "gt26"));
        phase.add(new WaypointData(23, 131, 138, WaypointData.WaypointType.LEVER, "gt27"));
        waypoints.put("goldor-terminals-2", phase);
        phase = new ArrayList<>();
        phase.add(new WaypointData(-3, 109, 112, WaypointData.WaypointType.TERMINAL, "gt31"));
        phase.add(new WaypointData(19, 122, 93, WaypointData.WaypointType.TERMINAL, "gt32"));
        phase.add(new WaypointData(-3, 119, 93, WaypointData.WaypointType.TERMINAL, "gt33"));
        phase.add(new WaypointData(-3, 109, 77, WaypointData.WaypointType.TERMINAL,"gt34"));
        phase.add(new WaypointData(0, 119, 77, WaypointData.WaypointType.PATH, "gt35"));
        phase.add(new WaypointData(2, 121, 55, WaypointData.WaypointType.LEVER, "gt36"));
        phase.add(new WaypointData(14, 121, 55, WaypointData.WaypointType.LEVER, "gt37"));
        waypoints.put("goldor-terminals-3", phase);
        phase = new ArrayList<>();
        phase.add(new WaypointData(41, 109, 29, WaypointData.WaypointType.TERMINAL, "gt41"));
        phase.add(new WaypointData(44, 121, 29, WaypointData.WaypointType.TERMINAL, "gt42"));
        phase.add(new WaypointData(67, 109, 29, WaypointData.WaypointType.TERMINAL, "gt43"));
        phase.add(new WaypointData(72, 115, 48, WaypointData.WaypointType.TERMINAL, "gt44"));
        phase.add(new WaypointData(84, 120, 34, WaypointData.WaypointType.LEVER, "gt45"));
        phase.add(new WaypointData(86, 127, 46, WaypointData.WaypointType.LEVER, "gt46"));
        phase.add(new WaypointData(63, 126, 35, WaypointData.WaypointType.ARROW, "gt47"));
        waypoints.put("goldor-terminals-4", phase);
    }
    
}
