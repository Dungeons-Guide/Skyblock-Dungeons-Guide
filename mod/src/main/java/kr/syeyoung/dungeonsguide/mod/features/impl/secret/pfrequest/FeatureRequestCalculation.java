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
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.frontpage.WidgetRequestSetsList;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.legacy.WidgetRequestCalculation;
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
        widgets.add(new WidgetRequestCalculation());
    }


    @Getter
    private List<PathfindPrecalculationRequestSet> pathfindPrecalculationRequestSets = new ArrayList<>();

    public void addPathfindPrecalculationRequestSet(PathfindPrecalculationRequestSet requestSet) {
        this.pathfindPrecalculationRequestSets.add(requestSet);
    }


    private AtomicBoolean calculating = new AtomicBoolean();


    private UUID calcuuid = UUID.randomUUID();
    private UUID calcuuid2 = UUID.randomUUID();



    public void uploadToService(WidgetRequestCalculation widgetRequestCalculation) {
        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            WidgetNotificationProgress progress = new WidgetNotificationProgress(calcuuid, "Pathfind Request Uploading Progress");
            FeatureRegistry.NOTIFICATIONS.getRootWidget().updateNotification(calcuuid2, progress); // should be thread safe. shouuuuld be.


            try {


                Frame parent = new Frame();
                FileDialog dialog = new FileDialog(parent, "Choose a Pathfind Request ZIP", FileDialog.LOAD);
                dialog.setDirectory(Main.getConfigDir().getAbsolutePath());

                dialog.setFilenameFilter((dir, name) -> name.endsWith(".zip")); //osx
                dialog.setFile("*.zip"); // windows

                dialog.setVisible(true);

                File[] chosen = dialog.getFiles();

                dialog.dispose();
                parent.dispose();

                if (chosen.length == 0) {
                    widgetRequestCalculation.reload();
                    return;
                }
                File f = chosen[0];

                String uploadUrl;
                WidgetNotificationProgress.Progress p1 = new WidgetNotificationProgress.Progress ("Getting upload url...", null, null, false);
                try {
                    progress.addProgress(p1);
                    HttpsURLConnection connection = (HttpsURLConnection) new URL("https://pathfind.dungeons.guide/upload").openConnection();
                    connection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("Authorization", "Bearer "+AuthManager.getInstance().getWorkingTokenOrThrow());
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
                    String servers = IOUtils.toString(inputStreamReader);
                    JsonObject key = new Gson().fromJson(servers, JsonObject.class);
                    uploadUrl = key.get("url").getAsString();
                } finally {
                    progress.removeProgress(p1);
                }
                p1 = new WidgetNotificationProgress.Progress ("Uploading..." , new AtomicInteger(), new AtomicInteger((int) f.length()), true);
                try {
                    progress.addProgress(p1);
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(uploadUrl).openConnection();
                    httpsURLConnection.setDoOutput(true);
                    httpsURLConnection.setRequestProperty("Content-Length", f.length()+"");
                    httpsURLConnection.setRequestProperty("Content-Type", "application/zip");
                    httpsURLConnection.setFixedLengthStreamingMode(f.length());
                    httpsURLConnection.setRequestMethod("PUT");
                    FileInputStream fileInputStream = new FileInputStream(f);
                    byte buf[] = new byte[1024 *1024];
                    int len = 0;
                    long total = 0;
                    while((len = fileInputStream.read(buf)) != -1) {
                        httpsURLConnection.getOutputStream().write(buf, 0, len);
                        total += len;
                        p1.getCurrent().set((int) total);
                    }
                    System.out.println(httpsURLConnection.getResponseCode());
                    System.out.println(httpsURLConnection.getResponseMessage());
                    if (httpsURLConnection.getResponseCode() != 200) {
                        throw new RuntimeException("Status code "+httpsURLConnection.getResponseCode());
                    }
                } finally {
                    progress.removeProgress(p1);
                }
                p1 = new WidgetNotificationProgress.Progress ("Requesting Calculation", null, null, false);
                try {
                    progress.addProgress(p1);
                    HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL("https://pathfind.dungeons.guide/process").openConnection();
                    httpsURLConnection.setRequestMethod("POST");
                    httpsURLConnection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
                    httpsURLConnection.addRequestProperty("Authorization", "Bearer "+AuthManager.getInstance().getWorkingTokenOrThrow());
                    System.out.println(httpsURLConnection.getResponseCode());
                    System.out.println(httpsURLConnection.getResponseMessage());
                    if (httpsURLConnection.getResponseCode() != 200) {
                        throw new RuntimeException("Status code "+httpsURLConnection.getResponseCode());
                    }
                } finally {
                    progress.removeProgress(p1);
                }
                p1 = new WidgetNotificationProgress.Progress ("Requested calculation! Track status in config", new AtomicInteger(1), new AtomicInteger(1), true);
                progress.addProgress(p1);
                try {
                    Thread.sleep(5000);
                } finally {
                    progress.removeProgress(p1);
                }

            } catch (Exception e) {
                ChatTransmitter.addToQueue("An error occured while doing stuff: contact dg support");
                System.out.println("An error occured while requesting pfreqs");
                e.printStackTrace();
            } finally {
                FeatureRegistry.NOTIFICATIONS.getRootWidget().removeNotification(calcuuid2);
            }
        }).start();
    }

    public boolean calculating() {
        return calculating.get();
    }
}
