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
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.MarkerProvider;
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
import kr.syeyoung.dungeonsguide.mod.features.AbstractHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.RawRenderingGuiFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlay;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlayMarker;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlayPlayer;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Clip;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.overlay.GUIRectPositioner;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
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

public class FeatureDungeonMap2 extends AbstractHUDFeature {

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
        super("Dungeon HUD", "Dungeon Map", "Display dungeon map!", "dungeon.map2");
        this.setEnabled(false);
        this.getFeatureRect().setWidth(128.0);
        this.getFeatureRect().setHeight(128.0);

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
        checkVisibility();
    }

    @DGEventHandler(ignoreDisabled = true, triggerOutOfSkyblock = true)
    public void onDungeonLeave(DungeonLeftEvent event) {
        on = false;
        checkVisibility();
    }


    @Override
    public boolean isVisible() {
        return on && SkyblockStatus.isOnDungeon() && isEnabled();
    }

    @Override
    public OverlayWidget instantiateWidget() {
        if (mapConfiguration == null) return null;
        Clip clip = new Clip();
        clip.widget.setValue(new WidgetFeatureWrapper2(new WidgetDungeonMap(mapConfiguration, this::getOverlay)));
        return new OverlayWidget(
                clip,
                OverlayType.UNDER_CHAT,
                new GUIRectPositioner(this::getFeatureRect),
                getClass().getSimpleName()
        );
    }

    public List<MapOverlay> getOverlay() {

        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null) return Collections.EMPTY_LIST;

        List<MapOverlay> overlays = new ArrayList<>();
        if (dungeonContext.getBossfightProcessor() != null) {
            for (MarkerData marker : dungeonContext.getBossfightProcessor().getMarkers()) {
                MapConfiguration.PlayerHeadSettings settings = mapConfiguration.getHeadSettingsMap().get(marker.getType());
                if (settings == null || settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.NONE) continue;
                overlays.add(new MapOverlayMarker(marker, settings));
            }
        }

        int i = 0;
        for (TabListEntry playerInfo : TabList.INSTANCE.getTabListEntries()) {
            if (++i >= 20) break;

            String name = TabListUtil.getPlayerNameWithChecks(playerInfo);
            if (name == null) continue;

            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);

            overlays.add(new MapOverlayPlayer(playerInfo,
                    entityplayer == Minecraft.getMinecraft().thePlayer ? mapConfiguration.getSelfSettings() : mapConfiguration.getTeammateSettings()));
        }

        return overlays;

        // add players last
    }


    @Override
    public Widget getConfigureWidget() {
        return new WidgetMapConfiguration(this);
    }


    public class WidgetFeatureWrapper2 extends Widget implements Layouter, MarkerProvider {
        private  Widget widget;
        public WidgetFeatureWrapper2(Widget widget) {
            this.widget = widget;
        }

        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.singletonList(widget);
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            Size toBe = new Size(getFeatureRect().getWidth(), getFeatureRect().getWidth());
            SingleChildPassingLayouter.INSTANCE.layout(buildContext, ConstraintBox.tight(toBe.getWidth(), toBe.getHeight()));
            return toBe;
        }

        @Override
        public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
            return getFeatureRect().getWidth();
        }

        @Override
        public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
            return getFeatureRect().getWidth();
        }

        @Override
        public List<Position> getMarkers() {
            Size size = getDomElement().getSize();

            return Arrays.asList(
                    new Position(size.getWidth()/2, 0),
                    new Position(0, size.getHeight()/2),
                    new Position(size.getWidth()/2,size.getHeight()),
                    new Position(size.getWidth(),size.getHeight()/2)
            );
        }
    }

    @Override
    public Widget instantiateDemoWidget() {
        return new WidgetFeatureWrapper2(new WidgetMapDemo(FeatureDungeonMap2.this));
    }

    @Override
    public Double getKeepRatio() {
        return 1.0;
    }

    @Override
    public boolean requiresHeightBound() {
        return false;
    }

    @Override
    public boolean requiresWidthBound() {
        return true;
    }
}
