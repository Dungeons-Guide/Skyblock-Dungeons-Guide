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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRedstoneKey;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor2;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.ISecret;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.AbstractAction;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip.WidgetNotificationProgress;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage.WidgetPathfindCredits;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage.WidgetRemoteRequestList;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage.WidgetRequestSetsList;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.remotereq.RemoteCache;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import lombok.Getter;
import net.minecraft.util.BlockPos;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FeatureRequestCalculation extends SimpleFeature {
    public static final String DOMAIN = "https://2wut55i2i2.execute-api.us-east-1.amazonaws.com/v1";

    public FeatureRequestCalculation() {
        super("Pathfinding & Secrets", "Request path calculation", "- View which precalculations are missing\n- Request pre-calculation (Requires purchase on dg)", "secret.requestcalculation");
        setEnabled(true);
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Override
    public void setupConfigureWidget(List<Widget> widgets) {
        super.setupConfigureWidget(widgets);
        widgets.add(new WidgetPathfindCredits());
        widgets.add(new WidgetRequestSetsList());
        widgets.add(new WidgetRemoteRequestList());
    }


    @Getter
    private List<PathfindPrecalculationRequestSet> pathfindPrecalculationRequestSets = new ArrayList<>();

    public void addPathfindPrecalculationRequestSet(PathfindPrecalculationRequestSet requestSet) {
        this.pathfindPrecalculationRequestSets.add(requestSet);
    }

    @Getter
    private Map<String, RemoteCache> remoteCacheMap = new HashMap<>();

    @Override
    public JsonObject saveConfig() {
        JsonObject jsonObject = super.saveConfig();
        JsonArray array = new JsonArray();
        for (RemoteCache value : remoteCacheMap.values()) {
            array.add(value.serialize());
        }
        jsonObject.add("cache", array);
        return jsonObject;
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);

        remoteCacheMap.clear();
        try {
            if (jsonObject.has("cache")) {
                for (JsonElement cache : jsonObject.get("cache").getAsJsonArray()) {
                    RemoteCache cache1 = RemoteCache.fromJson(cache.getAsJsonObject());
                    remoteCacheMap.put(cache1.getRequestId(), cache1);
                }
            }
        } catch (Exception e) {}

    }

    private AtomicBoolean calculating = new AtomicBoolean();



    public boolean calculating() {
        return calculating.get();
    }
}
