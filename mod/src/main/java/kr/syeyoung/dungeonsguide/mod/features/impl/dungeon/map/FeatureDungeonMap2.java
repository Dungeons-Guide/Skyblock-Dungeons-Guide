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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.ISecret;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.MapPlayerProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.*;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonEndedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonLeftEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonStartedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.RawRenderingGuiFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabList;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec4b;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

//TODO: reduce gl drawcalls somehow

public class FeatureDungeonMap2 extends RawRenderingGuiFeature {

    public static final String DEFAULT_OVERRIDES = "{\n" +
            "    \"name\": \"Puzzle Icons\",\n" +
            "    \"description\": \"Display Icon on Puzzle Rooms\",\n" +
            "    \"overrides\": {\n" +
            "      \"11982f7f-703e-4d98-9d27-4e07ba3fef71\": {\n" +
            "        \"nameOverride\": \"Creeper Beams\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_creeperbeam.png\"\n" +
            "      },\n" +
            "      \"5000be9d-3081-4a5e-8563-dd826705663a\": {\n" +
            "        \"nameOverride\": \"Tic Tac Toe\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_tictactoe.png\"\n" +
            "      },\n" +
            "      \"6367a338-dd48-4c30-9e03-7ff6b5c7a936\": {\n" +
            "        \"nameOverride\": \"Bomb Defuse\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_bombdefuse.png\"\n" +
            "      },\n" +
            "      \"9139cb1c-b6f3-4bac-92de-909b1eb73449\": {\n" +
            "        \"nameOverride\": \"Water Board\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_waterboard.png\"\n" +
            "      },\n" +
            "      \"d3e61abf-4198-4520-a950-a03761a0eb6f\": {\n" +
            "        \"nameOverride\": \"Higher Or Lower (TD)\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_blaze_td.png\"\n" +
            "      },\n" +
            "      \"cf44c95c-950e-49e0-aa4c-82c2b18d0acc\": {\n" +
            "        \"nameOverride\": \"Higher Or Lower (DT)\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_blaze_dt.png\"\n" +
            "      },\n" +
            "      \"cf6d49d3-4f1e-4ec9-836e-049573793ddd\": {\n" +
            "        \"nameOverride\": \"Boulder\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_boulder.png\"\n" +
            "      },\n" +
            "      \"a053f4fa-d6b2-4aef-ae3e-97c7eee0252e\": {\n" +
            "        \"nameOverride\": \"Ice Path\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_icepath.png\"\n" +
            "      },\n" +
            "      \"990f6e4c-f7cf-4d27-ae91-11219b85861f\": {\n" +
            "        \"nameOverride\": \"Ice Fill\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_icefill.png\"\n" +
            "      },\n" +
            "      \"ffd5411b-6ff4-4f60-b387-72f00510ec50\": {\n" +
            "        \"nameOverride\": \"Teleport Maze\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_teleport.png\"\n" +
            "      },\n" +
            "      \"c2ea0a41-d495-437f-86cc-235a71c49f22\": {\n" +
            "        \"nameOverride\": \"Three Weirdos\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_threeweirdo.png\"\n" +
            "      },\n" +
            "      \"b2dce4ed-2bda-4303-a4d7-3ebb914db318\": {\n" +
            "        \"nameOverride\": \"Quiz\",\n" +
            "        \"iconRotation\": \"ROTATE\",\n" +
            "        \"iconLocation\": \"dungeonsguide:map/overrides/icon_quiz.png\"\n" +
            "      }\n" +
            "    }\n" +
            "  }";

    public FeatureDungeonMap2() {
        super("Dungeon HUD", "Dungeon Map", "Display dungeon map!", "dungeon.map2", true, 128, 128);
        this.setEnabled(false);

        addParameter("scale", new FeatureParameter<>("scale", "Color of the background", "Same as name", 1.0, TCDouble.INSTANCE, mapConfiguration::setMapScale));

        addParameter("background_color", new FeatureParameter<>("background_color", "Color of the background", "Same as name", new AColor(0x22000000, true), TCAColor.INSTANCE, mapConfiguration::setBackgroundColor));
        addParameter("border_color", new FeatureParameter<>("border_color", "Color of the border", "Same as name", new AColor(255, 255, 255, 255), TCAColor.INSTANCE, mapConfiguration::setBorder));
        addParameter("border_thickness", new FeatureParameter<>("border_thickness", "Border Thickness", "Same as name", 2.0, TCDouble.INSTANCE, mapConfiguration::setBorderWidth));
        addParameter("map_rotation", new FeatureParameter<>("map_rotation", "Map Rotation", "",  MapConfiguration.MapRotation.VERTICAL, new TCEnum<>(MapConfiguration.MapRotation.values()), mapConfiguration::setMapRotation));

        addParameter("iconScale", new FeatureParameter<>("iconScale", "Scale", "Same as name", 1.0, TCDouble.INSTANCE, mapConfiguration.getCheckmarkSettings()::setScale));
        addParameter("iconRotation", new FeatureParameter<>("iconRotation", "Icon Rotation", "Same as name", MapConfiguration.RoomInfoSettings.IconRotation.ROTATE, new TCEnum<>(MapConfiguration.RoomInfoSettings.IconRotation.values()), mapConfiguration.getCheckmarkSettings()::setIconRotation));
        addParameter("iconCenter", new FeatureParameter<>("iconCenter", "Center checkmark", "", true, new TCBoolean(), mapConfiguration.getCheckmarkSettings()::setCenter));
        addParameter("iconStyle", new FeatureParameter<>("iconStyle", "Secret count", "", MapConfiguration.RoomInfoSettings.Style.CHECKMARK_AND_COUNT, new TCEnum<MapConfiguration.RoomInfoSettings.Style>(MapConfiguration.RoomInfoSettings.Style.values()), mapConfiguration.getCheckmarkSettings()::setStyle));


        addParameter("selfscale", new FeatureParameter<>("selfscale", "Color of the background", "Same as name", 1.0, TCDouble.INSTANCE, mapConfiguration.getSelfSettings()::setIconSize));
        addParameter("otherscale", new FeatureParameter<>("otherscale", "Color of the background", "Same as name", 1.0, TCDouble.INSTANCE, mapConfiguration.getTeammateSettings()::setIconSize));
        addParameter("selfstyle", new FeatureParameter<>("selfstyle", "Secret count", "", MapConfiguration.PlayerHeadSettings.IconType.HEAD, new TCEnum<>(MapConfiguration.PlayerHeadSettings.IconType.values()), mapConfiguration.getSelfSettings()::setIconType));
        addParameter("otherstyle", new FeatureParameter<>("otherstyle", "Secret count", "", MapConfiguration.PlayerHeadSettings.IconType.HEAD, new TCEnum<>(MapConfiguration.PlayerHeadSettings.IconType.values()), mapConfiguration.getTeammateSettings()::setIconType));

        addParameter("drawname", new FeatureParameter<>("drawname", "Draw Name", "", false, new TCBoolean(), mapConfiguration.getNameSettings()::setDrawName));
        addParameter("name_color", new FeatureParameter<>("name_color", "Color of the border", "Same as name", new AColor(255, 255, 255, 255), TCAColor.INSTANCE, mapConfiguration.getNameSettings()::setTextColor));
        addParameter("name_scale", new FeatureParameter<>("name_scale", "Color of the border", "Same as name", 1.0, TCDouble.INSTANCE, mapConfiguration.getNameSettings()::setSize));
        addParameter("name_padding", new FeatureParameter<>("name_padding", "Color of the border", "Same as name", 2.0, TCDouble.INSTANCE, mapConfiguration.getNameSettings()::setPadding));
        addParameter("name_style", new FeatureParameter<>("name_style", "Color of the border", "Same as name", MapConfiguration.NameSettings.NameRotation.SNAP, new TCEnum<MapConfiguration.NameSettings.NameRotation>(MapConfiguration.NameSettings.NameRotation.values()), mapConfiguration.getNameSettings()::setNameRotation));

        Preset preset = new Gson().fromJson(DEFAULT_OVERRIDES, Preset.class);
        mapConfiguration.getRoomOverrides().putAll(preset.getOverrides());


        for (MarkerData.MobType value : MarkerData.MobType.values()) {
            if (!mapConfiguration.getHeadSettingsMap().containsKey(value))
                mapConfiguration.getHeadSettingsMap().put(value, new MapConfiguration.PlayerHeadSettings());
        }
    }

    @Getter
    private final MapConfiguration mapConfiguration = new MapConfiguration();

    public static final Ordering<NetworkPlayerInfo> sorter = Ordering.from((compare1, compare2) -> {
        ScorePlayerTeam scorePlayerTeam = compare1.getPlayerTeam();
        ScorePlayerTeam scorePlayerTeam1 = compare2.getPlayerTeam();
        return ComparisonChain.start().compareTrueFirst(compare1.getGameType() != WorldSettings.GameType.SPECTATOR, compare2.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scorePlayerTeam != null ? scorePlayerTeam.getRegisteredName() : "", scorePlayerTeam1 != null ? scorePlayerTeam1.getRegisteredName() : "").compare(compare1.getGameProfile().getName(), compare2.getGameProfile().getName()).result();
    });

    @Override
    public JsonObject saveConfig() {
        JsonObject jsonObject = super.saveConfig();
        jsonObject.add("overrides", new Gson().toJsonTree(mapConfiguration.getRoomOverrides()));
        jsonObject.add("bosshead", new Gson().toJsonTree(mapConfiguration.getHeadSettingsMap()));
        return jsonObject;
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        if (jsonObject.has("overrides")) {
            Type type = new TypeToken<Map<UUID, MapConfiguration.RoomOverride>>(){}.getType();
            mapConfiguration.setRoomOverrides(new Gson().fromJson(jsonObject.get("overrides"), type));
            if (mapConfiguration.getRoomOverrides() == null)
                mapConfiguration.setRoomOverrides(new HashMap<>());

            for (MapConfiguration.RoomOverride value : mapConfiguration.getRoomOverrides().values()) {
                if (value.getNameOverride() == null) value.setNameOverride("");
                if (value.getTextureLocation() == null) value.setTextureLocation("");
                if (value.getIconRotation() == null)
                    value.setIconRotation(MapConfiguration.RoomInfoSettings.IconRotation.ROTATE);
                if (value.getIconLocation() == null) value.setIconLocation("");
            }
        }
        if (jsonObject.has("bosshead")) {
            Type type = new TypeToken<Map<MarkerData.MobType, MapConfiguration.PlayerHeadSettings>>(){}.getType();
            mapConfiguration.setHeadSettingsMap(new Gson().fromJson(jsonObject.get("bosshead"), type));
            if (mapConfiguration.getHeadSettingsMap() == null) mapConfiguration.setHeadSettingsMap(new HashMap<>());
        }

        for (MarkerData.MobType value : MarkerData.MobType.values()) {
            if (!mapConfiguration.getHeadSettingsMap().containsKey(value))
                mapConfiguration.getHeadSettingsMap().put(value, new MapConfiguration.PlayerHeadSettings());
        }

    }

    private boolean on = false;


    @DGEventHandler
    public void onDungeonStart(DungeonStartedEvent event) {
        on = true;
    }

    @DGEventHandler(ignoreDisabled = true)
    public void onDungeonLeave(DungeonLeftEvent event) {
        on = false;
    }


    @Override
    public void drawHUD(float partialTicks) {
        if (!on) return;
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null || context.getScaffoldParser() == null) return;
        DungeonRoomScaffoldParser mapProcessor = context.getScaffoldParser();

        MapData mapData = mapProcessor.getLatestMapData();
        GUIPosition featureSize = getFeatureRect();
        // TODO: redo chroma
        Gui.drawRect(0, 0, featureSize.getWidth().intValue(), featureSize.getHeight().intValue(), RenderUtils.getColorAt(0,0, mapConfiguration.getBackgroundColor()));
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        if (mapData == null) {
            Gui.drawRect(0, 0, featureSize.getWidth().intValue(), featureSize.getHeight().intValue(), 0xFFFF0000);
        } else {
            renderMap(partialTicks, context);
        }
        GlStateManager.popMatrix();
        GL11.glLineWidth((float) mapConfiguration.getBorderWidth());
        RenderUtils.drawUnfilledBox(0, 0, featureSize.getWidth().intValue(), featureSize.getHeight().intValue(),mapConfiguration.getBorder());
    }

    @Override
    public void drawDemo(float partialTicks) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (SkyblockStatus.isOnDungeon() && context != null && context.getScaffoldParser() != null && on) {
            drawHUD(partialTicks);
            return;
        }
        GUIPosition featureRect = getFeatureRect();
        Gui.drawRect(0, 0, featureRect.getWidth().intValue(), featureRect.getWidth().intValue(), RenderUtils.getColorAt(0,0, mapConfiguration.getBackgroundColor()));
        FontRenderer fr = getFontRenderer();

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Please join a dungeon to see preview", featureRect.getWidth().intValue() / 2 - fr.getStringWidth("Please join a dungeon to see preview") / 2, featureRect.getWidth().intValue() / 2 - fr.FONT_HEIGHT / 2, 0xFFFFFFFF);
        GL11.glLineWidth((float) mapConfiguration.getBorderWidth());
        RenderUtils.drawUnfilledBox(0, 0, featureRect.getWidth().intValue(), featureRect.getWidth().intValue(),mapConfiguration.getBorder());

    }

    @Override
    public Widget getConfigureWidget() {
        return new WidgetMapConfiguration(this);
    }


    public void renderMap(float partialTicks, DungeonContext dungeonContext) {
        DungeonRoomScaffoldParser mapProcessor = dungeonContext.getScaffoldParser();
        MapData mapData = mapProcessor.getLatestMapData();
        MapPlayerProcessor mapPlayerProcessor = dungeonContext.getMapPlayerMarkerProcessor();


        EntityPlayer p = Minecraft.getMinecraft().thePlayer;


        GUIPosition featureRect = getFeatureRect();
        int width = featureRect.getWidth().intValue();
        float scale = width / 128.0f;
        GlStateManager.translate(width / 2.0, width / 2.0, 0);
        GlStateManager.scale(scale, scale, 0);

        Vector2d pt = mapProcessor.getDungeonMapLayout().worldPointToMapPointFLOAT(p.getPositionEyes(partialTicks));

        if (dungeonContext.getBossfightProcessor() != null) {
            GlStateManager.translate(-64, -64, 0);
            BossfightProcessor bossfightProcessor = dungeonContext.getBossfightProcessor();
            BossfightRenderSettings settings = bossfightProcessor.getMapRenderSettings();
            if (settings != null) {
                renderBossfight(partialTicks, scale, settings, bossfightProcessor.getMarkers());
            }


        } else {

            double yaw = ((p.prevRotationYawHead + (p.rotationYawHead - p.prevRotationYawHead) * partialTicks) % 360 + 360) % 360;

            boolean rotated = false;
            GlStateManager.scale(mapConfiguration.getMapScale(), mapConfiguration.getMapScale(), 0);
            if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.VERTICAL) {
                if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.CENTER) {
                    GlStateManager.rotate((float) (180.0 - yaw), 0, 0, 1);
                    rotated = true;
                }
                if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.ROTATE) {
                    GlStateManager.translate(-pt.x, -pt.y, 0);
                } else {
                    GlStateManager.translate(-64, -64, 0);
                }
            } else {
                GlStateManager.translate(-64, -64, 0);
            }


            double snapRotation = 0;
            if (rotated) {
                snapRotation =  (yaw - 180) % 360;
            }
            renderRooms(mapProcessor);
            renderIcons(mapProcessor, scale * mapConfiguration.getMapScale(), snapRotation + 360);
            GlStateManager.color(1,1,1,1);
            renderPlayers(partialTicks, mapProcessor, mapData, dungeonContext, scale * mapConfiguration.getMapScale());
        }
    }


    private void renderBossfight(float partialTicks, float scale, BossfightRenderSettings bossfightRenderSettings, List<MarkerData> markers) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(bossfightRenderSettings.getResourceLocation());
        double x, y, width, height;
        if (bossfightRenderSettings.getTextureWidth() > bossfightRenderSettings.getTextureHeight()) {
            double rHeight = bossfightRenderSettings.getTextureHeight() * 128.0 / bossfightRenderSettings.getTextureWidth();
            x = 0; y = (128 -rHeight) / 2; width = 128; height = rHeight;
            drawScaledCustomSizeModalRect(
                    0,(128 -rHeight) / 2, 0, 0, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight(), 128, rHeight, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight()
            );
        } else {
            double rWidth = bossfightRenderSettings.getTextureWidth() * 128.0 / bossfightRenderSettings.getTextureHeight();
            x = (128 - rWidth) / 2; y = 0; width = rWidth; height = 128;
            drawScaledCustomSizeModalRect(
                    (128 - rWidth) / 2,0, 0, 0, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight(), rWidth, 128, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight()
            );
        }



        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation2);
        for (MarkerData marker : markers) {
            double xCoord = marker.getPrevX() + (marker.getCurrX() - marker.getPrevX()) * partialTicks;
            double zCoord = marker.getPrevZ() + (marker.getCurrZ() - marker.getPrevZ()) * partialTicks;
            double px = width * (xCoord - bossfightRenderSettings.getMinX()) / (bossfightRenderSettings.getMaxX() - bossfightRenderSettings.getMinX()) + x;
            double pz = height * (zCoord - bossfightRenderSettings.getMinZ()) / (bossfightRenderSettings.getMaxZ() - bossfightRenderSettings.getMinZ()) + y;
            double yaw = marker.getPrevYaw() + (marker.getCurrYaw() - marker.getPrevYaw()) * partialTicks;

            GlStateManager.enableTexture2D();
            GlStateManager.pushMatrix();
            GlStateManager.translate(px, pz, 0);
            GlStateManager.rotate((float) yaw, 0, 0, 1);

            GlStateManager.scale(1 / scale, 1 / scale, 0);

            int tx = marker.getMarkerIndex() % 8;
            int ty = marker.getMarkerIndex() / 8;
            Gui.drawScaledCustomSizeModalRect(-4, -4, tx * 72,ty * 72, 72, 72, 8, 8, 576, 576);
            GlStateManager.popMatrix();
        }


        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        Set<TabListEntry> playerList = getPlayerListCached();


        // 21 iterations bc we only want to scan the player part of tab list
        int i = 0;
        for (TabListEntry playerInfo : playerList) {
            if (++i >= 20) break;

            String name = TabListUtil.getPlayerNameWithChecks(playerInfo);
            if (name == null) continue;


            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);

            Vector2d pt2 = null;
            double yaw2 = 0;

            if (entityplayer != null && (!entityplayer.isInvisible() || entityplayer == thePlayer)) {
                // getting location from player entity
                Vec3 playerPos = entityplayer.getPositionEyes(partialTicks);
                double px = width * (playerPos.xCoord - bossfightRenderSettings.getMinX()) / (bossfightRenderSettings.getMaxX() - bossfightRenderSettings.getMinX()) + x;
                double pz = height * (playerPos.zCoord - bossfightRenderSettings.getMinZ()) / (bossfightRenderSettings.getMaxZ() - bossfightRenderSettings.getMinZ()) + y;

                pt2 = new Vector2d(px, pz);
                yaw2 = entityplayer.prevRotationYawHead + (entityplayer.rotationYawHead - entityplayer.prevRotationYawHead) * partialTicks;
                if(DungeonsGuide.getDungeonsGuide().verbose) System.out.println("Got player location from entity");
            }

            if(pt2 == null) return;

            if (entityplayer == thePlayer) {
                drawPlayer(mapConfiguration.getSelfSettings(), scale, playerInfo, entityplayer, pt2, yaw2);
            } else {
                drawPlayer(mapConfiguration.getTeammateSettings(), scale, playerInfo, entityplayer, pt2, yaw2);
            }
        }

    }
    private final ResourceLocation resourceLocation2 = new ResourceLocation("dungeonsguide:map/bossfight/markers.png");

    private final ResourceLocation resourceLocation = new ResourceLocation("dungeonsguide:map/maptexture.png");

    private Rectangle maxFit(int rot, short shape) {
        int[] patterns = new int[]{
                0xF, 0xF0, 0x0F00, 0xF000,
                0x1111, 0x2222, 0x4444, 0x8888
        };
        int[] cnts = new int[8];

        for (int i = 0; i < patterns.length; i++) {
            cnts[i] = patterns[i] & shape;
            if (i < 4) cnts[i] >>= i * 4;
            else cnts[i] >>= (i-4);
        }

        if (rot % 2 == 0) {
            int minY = 0;
            int maxY = 0;
            int currVal = 0;
//            System.out.println("---"+shape);
            for (int i = 0; i < 4; i++) {
                int bits = Integer.bitCount(cnts[i]);
                if (bits > currVal) {
                    minY = i;
                    maxY = i + 1;
                    currVal = bits;
                } else if (bits == currVal && cnts[i] == cnts[minY]) {
                    maxY = i + 1;
                }
            }
            int minX = Integer.numberOfTrailingZeros(cnts[minY]);
            int maxX = minX + currVal;

            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        } else {
            int minX = 0;
            int maxX = 0;
            int currVal = 0;
            for (int i = 4; i < 8; i++) {
                int bits = Integer.bitCount(cnts[i]);
                if (bits > currVal) {
                    minX = i - 4;
                    maxX = i - 3;
                    currVal = bits;
                } else if (bits == currVal && cnts[i] == cnts[minX + 4]) {
                    maxX = i - 3;
                }
            }
            int minY = Integer.numberOfTrailingZeros(cnts[minX]) / 4;
            int maxY = minY + currVal;

            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }

    }

    private void renderIcons(DungeonRoomScaffoldParser scaffoldParser, double scale, double snapRotation) {

        DungeonMapLayout layout = scaffoldParser.getDungeonMapLayout();

        int unitRoomBigWidth = layout.getUnitRoomSize().width + layout.getMapRoomGap();
        int unitRoomBigHeight = layout.getUnitRoomSize().height + layout.getMapRoomGap();
        int unitRoomWidth = layout.getUnitRoomSize().width;
        int unitRoomHeight = layout.getUnitRoomSize().height;
        int gap = layout.getMapRoomGap();

        double chkmark = mapConfiguration.getCheckmarkSettings().getScale();
        int pad = (int) mapConfiguration.getNameSettings().getPadding();
        MapConfiguration.NameSettings.NameRotation nameRotation = mapConfiguration.getNameSettings().getNameRotation();
        for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
            DungeonRoomInfo dungeonRoomInfo = dungeonRoom.getDungeonRoomInfo();
            MapConfiguration.RoomOverride override = dungeonRoomInfo == null ? null : mapConfiguration.getRoomOverrides().get(dungeonRoomInfo.getUuid());
//            MapConfiguration.RoomOverride override = mapConfiguration.getRoomOverrides().values().stream().findFirst().orElse(null);


            int rot = (int) ((Math.round(snapRotation  / 90) % 4 + 4) % 4);
            double firstSnap = rot * 90;

            if (nameRotation == MapConfiguration.NameSettings.NameRotation.FIX) {
                firstSnap = 0;
                rot = 0;
            } else if (nameRotation == MapConfiguration.NameSettings.NameRotation.ROTATE) {
                firstSnap = snapRotation;
            }

            int offX = 0, offY = 0, width = 0;
            if (nameRotation != MapConfiguration.NameSettings.NameRotation.ROTATE) {
                Rectangle fit = maxFit(rot, dungeonRoom.getShape());
                if ((fit.height - fit.width) * (rot % 2 == 0 ? 1 : -1) > 0 && nameRotation == MapConfiguration.NameSettings.NameRotation.SNAP_LONG) {
                    if (fit.height - fit.width > 0) {
                        rot = (((int) (snapRotation / 90) % 4 + 4) % 4);
                        if (rot < 2) {
                            firstSnap = 90;
                        } else {
                            firstSnap = 270;
                        }
                    } else {
                        rot = (((int) (snapRotation / 90) % 4 + 4) % 4);
                        if (rot == 0 || rot == 3) {
                            firstSnap = 0;
                        } else {
                            firstSnap = 180;
                        }
                    }
                    rot = (int) ((firstSnap) / 90 % 4);
                    rot = rot % 4;
                    fit = maxFit(rot, dungeonRoom.getShape());
                }
                Point mapPt = layout.roomPointToMapPoint(dungeonRoom.getMinRoomPt());

                if (rot == 0) {
                    offX = fit.x * unitRoomBigWidth + pad;
                    offY = fit.y * unitRoomBigHeight + pad;
                    width = fit.width;
                } else if (rot == 1) {
                    offX = (fit.x + fit.width) * unitRoomBigWidth - gap - pad;
                    offY = (fit.y) * unitRoomBigHeight + pad;
                    width = fit.height;
                } else if (rot == 2) {
                    offX = (fit.x + fit.width) * unitRoomBigWidth - gap - pad;
                    offY = (fit.y + fit.height) * unitRoomBigHeight - gap - pad;
                    width = fit.width;
                } else if (rot == 3) {
                    offX = (fit.x) * unitRoomBigWidth + pad;
                    offY = (fit.y + fit.height) * unitRoomBigHeight - gap - pad;
                    width = fit.height;
                }
                offX += mapPt.x;
                offY += mapPt.y;
            } else {
                Point mapPt = layout.roomPointToMapPoint(dungeonRoom.getMinRoomPt());
                Rectangle fit = maxFit(0, dungeonRoom.getShape());
                offX = mapPt.x + (fit.width * unitRoomBigWidth - gap) / 2;
                offY = mapPt.y + (fit.height * unitRoomBigHeight - gap) / 2;
                width = fit.width;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(offX, offY, 0);
            GlStateManager.scale(1/scale, 1/scale, 1.0);
            double size = mapConfiguration.getNameSettings().getSize();
            GlStateManager.scale(size, size, 1.0);
            GlStateManager.rotate((float) (firstSnap ), 0, 0, 90);
            int renderWidth = (int) ((width * unitRoomBigWidth - gap - 2 *pad) * scale / size);

            if (renderWidth < 10) {
                renderWidth = 10;
            }

            boolean drawNameSetting = override != null ? override.isDrawName() : mapConfiguration.getNameSettings().isDrawName();

            if (drawNameSetting && dungeonRoomInfo != null) {
                String name = override != null && !override.getNameOverride().isEmpty() ? override.getNameOverride() : dungeonRoomInfo.getName();
                if (dungeonRoomInfo.isRegistered()) {
                    if (nameRotation == MapConfiguration.NameSettings.NameRotation.ROTATE) {
                        Minecraft.getMinecraft().fontRendererObj.drawString(name, -Minecraft.getMinecraft().fontRendererObj.getStringWidth(name) / 2,
                                -Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 2,
                                RenderUtils.getColorAt(0, 0, mapConfiguration.getNameSettings().getTextColor()));
                    } else {
                        Minecraft.getMinecraft().fontRendererObj.drawSplitString(name, 0, 0, renderWidth,
                                RenderUtils.getColorAt(0, 0, mapConfiguration.getNameSettings().getTextColor()));
                    }
                }
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1,1,1,1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        GlStateManager.enableBlend();
        for (Point point : scaffoldParser.getPotential()) {
            Point mapPt = layout.roomPointToMapPoint(point);
            int offX = mapPt.x + unitRoomWidth / 2;
            int offY = mapPt.y + unitRoomHeight / 2;
            GlStateManager.pushMatrix();
            GlStateManager.translate(offX, offY, 0);
            GlStateManager.scale(1/scale, 1/scale, 1.0);
            GlStateManager.scale(chkmark, chkmark, 1.0);
            GlStateManager.rotate((float) (snapRotation), 0, 0, 90);

            GuiScreen.drawScaledCustomSizeModalRect(
                    -8, -8, 128 - 16, 64, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
            );
            GlStateManager.popMatrix();
        }
        for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
            DungeonRoomInfo dungeonRoomInfo = dungeonRoom.getDungeonRoomInfo();
            MapConfiguration.RoomOverride override = dungeonRoomInfo == null ? null : mapConfiguration.getRoomOverrides().get(dungeonRoomInfo.getUuid());
//            MapConfiguration.RoomOverride override = mapConfiguration.getRoomOverrides().values().stream().findFirst().orElse(null);


            Point pt = dungeonRoom.getMinRoomPt();
            Point mapPt = layout.roomPointToMapPoint(pt);
            int offX = mapPt.x + unitRoomWidth / 2;
            int offY = mapPt.y + unitRoomHeight / 2;
            if (mapConfiguration.getCheckmarkSettings().isCenter()) {
                Rectangle fit = maxFit(0, dungeonRoom.getShape());
                offX = mapPt.x + fit.x * unitRoomBigWidth + (fit.width * unitRoomBigWidth - gap) / 2;
                offY = mapPt.y + fit.y * unitRoomBigHeight + (fit.height * unitRoomBigHeight - gap) / 2;
//                System.out.println(dungeonRoom.getShape() + " / "+ fit);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(offX, offY, 0);
            GlStateManager.scale(1/scale, 1/scale, 1.0);

            GlStateManager.scale(chkmark, chkmark, 1.0);


            double iconRotation;
            MapConfiguration.RoomInfoSettings.IconRotation eiconrotation = override != null ? override.getIconRotation() : mapConfiguration.getCheckmarkSettings().getIconRotation();
            if (eiconrotation == MapConfiguration.RoomInfoSettings.IconRotation.ROTATE) {
                iconRotation = snapRotation;
            } else if (eiconrotation == MapConfiguration.RoomInfoSettings.IconRotation.SNAP){
                iconRotation = snapRotation - ((snapRotation - 45) % 90 - 90) % 90 - 45;
            } else {
                iconRotation = 0;
            }

            GlStateManager.rotate((float) (iconRotation ), 0, 0, 90);

            int u = 0;
            int v = 0;
            if (override == null || override.getIconLocation().isEmpty()) {
                switch (dungeonRoom.getCurrentState()) {
                    case FINISHED:
                        u = 256 - 16;
                        v = 16;
                        break;
                    case FAILED:
                        u = 256 - 16;
                        v = 0;
                        break;
                    case COMPLETE_WITHOUT_SECRETS:
                        u = 256 - 16;
                        v = 48;
                        break;
                    case DISCOVERED:
                        u = 256 - 16;
                        v = 32;
                        break;
                }
            } else {
                switch (dungeonRoom.getCurrentState()) {
                    case FINISHED:
                        u = 0;
                        v = 16;
                        break;
                    case FAILED:
                        u = 0;
                        v = 0;
                        break;
                    case COMPLETE_WITHOUT_SECRETS:
                        u = 16;
                        v = 16;
                        break;
                    case DISCOVERED:
                        u = 16;
                        v = 0;
                        break;
                }
            }

                MapConfiguration.RoomInfoSettings.Style style = override != null ? override.getStyle() : mapConfiguration.getCheckmarkSettings().getStyle();

                if (style == MapConfiguration.RoomInfoSettings.Style.CHECKMARK_AND_COUNT && dungeonRoom.getTotalSecrets() != 0 &&
                        (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS || dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED)
                    || style == MapConfiguration.RoomInfoSettings.Style.SECRET_COUNT) {
                    String toDraw;
                    if (dungeonRoom.getDungeonRoomInfo() != null) {
                        toDraw = dungeonRoom.getMechanics().values().stream().filter(a -> a instanceof ISecret)
                                .filter(a -> ((ISecret) a).isFound(dungeonRoom))
                                .count() + "/" +dungeonRoom.getTotalSecrets();
                    } else {
                        toDraw = "?/"+(dungeonRoom.getTotalSecrets() == -1 ? "?" : dungeonRoom.getTotalSecrets());
                    }

                    int color = 0xFFFFFFFF;
                    if (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.FAILED)
                        color = 0xFFFF0000;
                    else if (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.FINISHED)
                        color = 0xFF00FF00;
                    else if (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED)
                        color = 0xFF777777;

                    Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                            toDraw, -Minecraft.getMinecraft().fontRendererObj.getStringWidth(toDraw)/2, -4, color
                    );
                } else {
                    GlStateManager.color(1,1,1,1);
                    GlStateManager.enableBlend();
                    if (override == null || override.getIconLocation().isEmpty()) {
                        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
                        GuiScreen.drawScaledCustomSizeModalRect(
                                -8, -8, u, v, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
                        );
                    } else {
                        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(override.getIconLocation()));
                        GuiScreen.drawScaledCustomSizeModalRect(
                                -8, -8, u, v, 16, 16, unitRoomWidth, unitRoomHeight, 32, 32
                        );
                    }
                }

            GlStateManager.popMatrix();
        }
    }


    public static void drawScaledCustomSizeModalRect(double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float g = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos((double)x, (double)(y + height), 0.0).tex((double)(u * f), (double)((v + (float)vHeight) * g)).endVertex();
        worldRenderer.pos((double)(x + width), (double)(y + height), 0.0).tex((double)((u + (float)uWidth) * f), (double)((v + (float)vHeight) * g)).endVertex();
        worldRenderer.pos((double)(x + width), (double)y, 0.0).tex((double)((u + (float)uWidth) * f), (double)(v * g)).endVertex();
        worldRenderer.pos((double)x, (double)y, 0.0).tex((double)(u * f), (double)(v * g)).endVertex();
        tessellator.draw();
    }
    private void renderRooms(DungeonRoomScaffoldParser scaffoldParser) {

        DungeonMapLayout layout = scaffoldParser.getDungeonMapLayout();

        int unitRoomBigWidth = layout.getUnitRoomSize().width + layout.getMapRoomGap();
        int unitRoomBigHeight = layout.getUnitRoomSize().height + layout.getMapRoomGap();
        int unitRoomWidth = layout.getUnitRoomSize().width;
        int unitRoomHeight = layout.getUnitRoomSize().height;
        int gap = layout.getMapRoomGap();


        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        for (Point point : scaffoldParser.getPotential()) {
            Point mapPt = layout.roomPointToMapPoint(point);
            int offX = mapPt.x;
            int offY = mapPt.y;

            GuiScreen.drawScaledCustomSizeModalRect(
                    offX, offY, 24, 24*3, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
            );
        }
        for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
            DungeonRoomInfo dungeonRoomInfo = dungeonRoom.getDungeonRoomInfo();
            MapConfiguration.RoomOverride override = dungeonRoomInfo == null ? null : mapConfiguration.getRoomOverrides().get(dungeonRoomInfo.getUuid());
//            MapConfiguration.RoomOverride override = mapConfiguration.getRoomOverrides().values().stream().findFirst().orElse(null);

            int offsetX = 0;
            int offsetY = 0;

            if (dungeonRoom.getColor() == 63) {
                // normal
//                offsetY = 24;
            } else if (dungeonRoom.getColor() == 18) {
                // blood
                offsetY = 24 * 1;
            } else if (dungeonRoom.getColor() == 66) {
                // puzzle
                offsetY = 24 * 2;
            } else if (dungeonRoom.getColor() == 30)  {
                // entrance
                offsetY = 24 * 3;
            } else if (dungeonRoom.getColor() == 82) {
                // fairy
                offsetX = 24;
            } else if (dungeonRoom.getColor() == 62) {
                // trap
                offsetX = 24;
                offsetY = 24;
            } else if (dungeonRoom.getColor() == 74) {
                // miniboss
                offsetX = 24;
                offsetY = 24 * 2;
            } else {
                offsetX = 24;
                offsetY = 24 * 3;
            }
            Point mapPt = layout.roomPointToMapPoint(dungeonRoom.getMinRoomPt());
            if (override != null && !override.getTextureLocation().isEmpty() && dungeonRoomInfo != null) {
                int rotation = dungeonRoom.getRoomMatcher().getRotation();
                short shape = dungeonRoomInfo.getShape();

                int[] patterns = new int[]{
                        0xF, 0xF0, 0x0F00, 0xF000,
                        0x1111, 0x2222, 0x4444, 0x8888
                };
                int maxWidth = 0, maxHeight = 0;

                for (int i = 0; i < 4; i++) {
                    int cnts = Integer.bitCount(patterns[i] & shape);
                    if (cnts > maxWidth) maxWidth = cnts;
                }
                for (int i = 0; i < 4; i++) {
                    int cnts = Integer.bitCount(patterns[i + 4] & shape);
                    if (cnts > maxHeight) maxHeight = cnts;
                }

                int widthPixels = maxWidth * unitRoomBigWidth - gap;
                int heightPixels = maxHeight * unitRoomBigHeight - gap;
                int widthTexturePixels = maxWidth * 20 - 4;
                int heightTexturePixels = maxHeight * 20 - 4;
                int rWidthPixels = dungeonRoom.getUnitWidth() * unitRoomBigWidth - gap;
                int rHeightPixels = dungeonRoom.getUnitHeight() * unitRoomBigHeight - gap;

                Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(override.getTextureLocation()));
                GlStateManager.pushMatrix();;
                GlStateManager.translate(mapPt.x + rWidthPixels / 2.0, mapPt.y + rHeightPixels / 2.0, 0);
                GlStateManager.rotate(-rotation * 90, 0, 0, 1);
                drawScaledCustomSizeModalRect(
                        -widthPixels / 2.0, -heightPixels / 2.0, 0, 0, widthTexturePixels, heightTexturePixels, widthPixels, heightPixels, 128, 128
                );
                GlStateManager.popMatrix();
            } else {
                Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
                for (int y = 0; y < 4; y++) {
                    for (int x = 0; x < 4; x++) {
                        boolean isIn = ((dungeonRoom.getShape() >> ((y * 4) + x)) & 0x1) > 0;
                        boolean isRightIn = ((dungeonRoom.getShape() >> ((y * 4) + x +1)) & 0x1) > 0 && x < 3;
                        boolean isBottomIn = ((dungeonRoom.getShape() >> ((y * 4) + x + 4)) & 0x1) > 0 && y < 3;
                        boolean isBottomRightIn = ((dungeonRoom.getShape() >> ((y * 4) + x  + 5)) & 0x1) > 0 && y < 3 && x < 3;

                        int offX = mapPt.x + x * unitRoomBigWidth;
                        int offY = mapPt.y + y * unitRoomBigHeight;

                        if (isIn) {
                            GuiScreen.drawScaledCustomSizeModalRect(
                                    offX, offY, offsetX, offsetY, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
                            );
                            if (isRightIn) {
                                GuiScreen.drawScaledCustomSizeModalRect(
                                        offX+unitRoomWidth, offY, offsetX+16, offsetY, 4, 16, gap, unitRoomHeight, 128, 128
                                );
                            }
                            if (isBottomIn) {
                                GuiScreen.drawScaledCustomSizeModalRect(
                                        offX, offY+unitRoomHeight, offsetX, offsetY + 16, 16, 4, unitRoomWidth, gap, 128, 128
                                );
                            }
                            if (isBottomRightIn && isRightIn && isBottomIn) {
                                GuiScreen.drawScaledCustomSizeModalRect(
                                        offX+unitRoomWidth, offY+unitRoomHeight, offsetX+16, offsetY+16, 4, 4, gap, gap, 128, 128
                                );
                            }
                        }
                    }
                }
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
            for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : dungeonRoom.getDoorsAndStates()) {
                double x = doorsAndState.getFirst().x;
                double y = doorsAndState.getFirst().y;
                if (doorsAndState.getSecond() == EDungeonDoorType.NONE) continue;

                int offsetXX = offsetX;
                int offsetYY = offsetY;

                if (doorsAndState.getSecond() == EDungeonDoorType.WITHER) {
                    offsetXX = 48;
                    offsetYY = 24;
                }

                if (doorsAndState.getSecond() == EDungeonDoorType.BLOOD) {
                    offsetXX = 48;
                    offsetYY = 0;
                }

                if (doorsAndState.getSecond() == EDungeonDoorType.UNOPEN) {
                    offsetXX = 24;
                    offsetYY = 24 * 3;
                }

                if (x % 1 != 0) {
                    GuiScreen.drawScaledCustomSizeModalRect(
                            mapPt.x + (int) (Math.ceil(x) * unitRoomBigWidth) - gap, mapPt.y + (int) (Math.ceil(y) * unitRoomBigHeight), offsetXX + 20, offsetYY, 4, 16, gap, unitRoomHeight, 128, 128
                    );
                } else {
                    GuiScreen.drawScaledCustomSizeModalRect(
                            mapPt.x + (int) (Math.ceil(x) * unitRoomBigWidth), mapPt.y +(int) (Math.ceil(y) * unitRoomBigHeight) - gap, offsetXX, offsetYY + 20, 16, 4, unitRoomWidth, gap, 128, 128
                    );
                }
            }

        }
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


    private void renderPlayers(float partialTicks, DungeonRoomScaffoldParser scaffoldParser, MapData mapData, DungeonContext context, double scale) {

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        Set<TabListEntry> playerList = getPlayerListCached();


        // 21 iterations bc we only want to scan the player part of tab list
        TabListEntry self = null;
        Vector2d selfPt2 = null;
        double selfYaw2 = 0;

        int i = 0;
        for (TabListEntry playerInfo : playerList) {
            if (++i >= 20) break;

            String name = TabListUtil.getPlayerNameWithChecks(playerInfo);
            if (name == null) continue;


            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);

            Vector2d pt2 = null;
            double yaw2 = 0;

            if (entityplayer != null && (!entityplayer.isInvisible() || entityplayer == thePlayer)) {
                // getting location from player entity
                pt2 = scaffoldParser.getDungeonMapLayout().worldPointToMapPointFLOAT(entityplayer.getPositionEyes(partialTicks));
                yaw2 = entityplayer.prevRotationYawHead + (entityplayer.rotationYawHead - entityplayer.prevRotationYawHead) * partialTicks;
            }
            else {
                // getting player location from map
                String iconName = context.getMapPlayerMarkerProcessor().getMapIconToPlayerMap().get(name);
                if (iconName != null) {
                    Vec4b vec = mapData.mapDecorations.get(iconName);
                    if (vec != null) {
                        pt2 = new Vector2d(vec.func_176112_b() / 2d + 64, vec.func_176113_c() / 2d + 64);
                        yaw2 = vec.func_176111_d() * 360 / 16.0f;
                    }
                }
            }

            if(pt2 == null) return;

            if (entityplayer == thePlayer) {
                self = playerInfo;
                selfPt2 = pt2;
                selfYaw2 = yaw2;
            } else {
                drawPlayer(mapConfiguration.getTeammateSettings(), scale, playerInfo, entityplayer, pt2, yaw2);
            }
        }

        if (self != null)
            drawPlayer(mapConfiguration.getSelfSettings(), scale, self, thePlayer, selfPt2, selfYaw2);
    }
    private void drawPlayer(MapConfiguration.PlayerHeadSettings settings, double scale, TabListEntry info, EntityPlayer entityPlayer, Vector2d pt2, double yaw2) {
        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.NONE) return;
        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.ARROW) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
            GlStateManager.enableTexture2D();
            GlStateManager.pushMatrix();
            GlStateManager.translate(pt2.x, pt2.y, 0);
            GlStateManager.rotate((float) yaw2, 0, 0, 1);

            GlStateManager.scale(1 / scale, 1 / scale, 0);

            GlStateManager.scale(settings.getIconSize(), settings.getIconSize(), 0);
            Gui.drawScaledCustomSizeModalRect(-4, -4, 128 - 16,
                    Minecraft.getMinecraft().thePlayer == entityPlayer ? 128 - 32 : 128 - 16, 16, -16, 8, 8, 128, 128);
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            boolean flag1 = settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.HEAD_FLIP;
            GlStateManager.enableTexture2D();
            Minecraft.getMinecraft().getTextureManager().bindTexture(
                    info.getLocationSkin()
            );
            int l2 = 8 + (flag1 ? 8 : 0);
            int i3 = 8 * (flag1 ? -1 : 1);

            GlStateManager.translate(pt2.x, pt2.y, 0);
            GlStateManager.rotate((float) yaw2, 0, 0, 1);

            GlStateManager.scale(1 / scale, 1 / scale, 0);

            GlStateManager.scale(settings.getIconSize(), settings.getIconSize(), 0);

            // cutting out the player head out of the skin texture

            // backside of head
            GlStateManager.pushMatrix();
            GlStateManager.scale(9.0 / 8, 9.0 / 8.0, 1.0);
            Gui.drawScaledCustomSizeModalRect(-4, -4, 56.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            GlStateManager.popMatrix();
            Gui.drawScaledCustomSizeModalRect(-4, -4, 8.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            GlStateManager.scale(9.0 / 8, 9.0 / 8.0, 1.0);
            Gui.drawScaledCustomSizeModalRect(-4, -4, 40.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            GlStateManager.popMatrix();
        }

    }
}
