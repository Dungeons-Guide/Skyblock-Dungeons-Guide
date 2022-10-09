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

package kr.syeyoung.dungeonsguide.wsresource;

import kr.syeyoung.dungeonsguide.events.impl.StompConnectedEvent;
import kr.syeyoung.dungeonsguide.stomp.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StaticResourceCache {
    public static final StaticResourceCache INSTANCE = new StaticResourceCache();

    private Map<UUID, StaticResource> staticResourceMap = new HashMap<>();

    private Map<UUID, CompletableFuture<StaticResource>> staticResourceRequest = new HashMap<>();

    public void purgeCache() {
        staticResourceRequest.clear();
        staticResourceMap.clear();
    }

    public CompletableFuture<StaticResource> getResource(UUID resourceID) {
        if (staticResourceMap.containsKey(resourceID)) return CompletableFuture.completedFuture(staticResourceMap.get(resourceID));
        if (staticResourceRequest.containsKey(resourceID)) return staticResourceRequest.get(resourceID);

        StompManager.getInstance().send(new StompPayload().destination("/app/staticresource.get").payload(resourceID.toString()));
        CompletableFuture<StaticResource> comp = new CompletableFuture<>();
        staticResourceRequest.put(resourceID, comp);
        return comp;
    }

    @SubscribeEvent
    public void stompConnect(StompConnectedEvent event) {

        event.getStompInterface().subscribe("/user/queue/staticresource.get", (stompClient ,payload) -> {
            JSONObject object = new JSONObject(payload);
            StaticResource staticResource = new StaticResource();
            staticResource.setResourceID(UUID.fromString(object.getString("resourceID")));
            staticResource.setExists(object.getBoolean("exists"));
            staticResource.setValue((!object.has("value") || object.isNull("value")) ? null : object.getString("value"));

            staticResourceMap.put(staticResource.getResourceID(), staticResource);
            CompletableFuture<StaticResource> completed = staticResourceRequest.remove(staticResource.getResourceID());
            if (completed != null) completed.complete(staticResource);
        });


        getResource(BONUS_SCORE);
        getResource(TRIVIA_ANSWERS);
        getResource(DATA_COLLECTION);
    }

    public static final UUID BONUS_SCORE = UUID.fromString("13f10001-66b5-46e5-94f9-1a5161a23429");
    public static final UUID TRIVIA_ANSWERS = UUID.fromString("5657f2cc-2bb8-4fcd-b55c-ffc0a35b9349");
    public static final UUID DATA_COLLECTION = UUID.fromString("c11f026f-9f26-4d14-8d52-88325dd6397a");
}
