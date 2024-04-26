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
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.DRIWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.CachedPathfinderRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindCache;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WidgetRequestCalculation extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "calculationdisable")
    public final BindableAttribute<Boolean> calculationDisable = new BindableAttribute<>(Boolean.class, true);

    @Bind(variableName = "generatingreq")
    public final BindableAttribute<Boolean> generatingReq = new BindableAttribute<>(Boolean.class, true);
    @Bind(variableName = "error")
    public final BindableAttribute<String> err = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "token")
    public final BindableAttribute<String> token = new BindableAttribute<>(String.class, "Loading...");

    @Bind(variableName = "completes")
    public final BindableAttribute<Column> colApi = new BindableAttribute<>(Column.class);
    @Bind(variableName = "progress")
    public final BindableAttribute<Widget> widgetBindableAttribute = new BindableAttribute<>(Widget.class);


    public WidgetRequestCalculation() {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/requestcalculation.gui"));

        generatingReq.setValue(FeatureRegistry.SECRET_PATHFIND_REQUEST.calculating());
        calculationDisable.setValue(true);

        reload();
    }

    private void doReload() {
        if (colApi.getValue() != null)
            colApi.getValue().removeAllWidget();
        widgetBindableAttribute.setValue(null);
        this.err.setValue("");
        try {
            JsonObject jsonObject = ApiFetcher.getJsonWithAuth("https://pathfind.dungeons.guide/info", AuthManager.getInstance().getWorkingTokenOrThrow());
            token.setValue(jsonObject.get("token").getAsInt()+" tokens");
            calculationDisable.setValue(jsonObject.get("token").getAsInt() <= 0 || (jsonObject.has("process") && !jsonObject.get("process").isJsonNull()));
            if (colApi.getValue() != null) {
                for (JsonElement results : jsonObject.getAsJsonArray("results")) {
                    colApi.getValue().addWidget(new WidgetCalculationResult(results.getAsJsonObject().get("timestamp").getAsString(), results.getAsJsonObject().get("key").getAsString()));
                }
            }
            if (jsonObject.has("processData"))
                widgetBindableAttribute.setValue(new WidgetCalculationProcess(jsonObject.getAsJsonObject("processData")));
        } catch (IOException e) {
            this.err.setValue(e.getMessage());
            e.printStackTrace();
        }
    }

    @On(functionName = "requestcalc")
    public void reqCalc() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        calculationDisable.setValue(true);
        FeatureRegistry.SECRET_PATHFIND_REQUEST.uploadToService(this);
    }

    @On(functionName = "generatereq")
    public void generateReq() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        generatingReq.setValue(true);
        FeatureRegistry.SECRET_PATHFIND_REQUEST.requestCalc();
    }

    @On(functionName = "purchaseToken")
    public void purchaseToken() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        try {
            Desktop.getDesktop().browse(new URL("https://pathfind.dungeons.guide/purchase?uuid="+Minecraft.getMinecraft().getSession().getProfile().getId()).toURI());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @On(functionName = "reload")
    public void reload() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        token.setValue("Loading...");
        ApiFetcher.ex.submit(this::doReload);
    }
}
