/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.chat;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessResult;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatSubscriber;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.events.annotations.EventHandlerRegistry;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerList;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChatRoutine {
    List<Action> actions = new ArrayList<>();

    public static interface Action {
        void execute(Runnable next);
    }
    @AllArgsConstructor
    public static class ActionSay implements Action {
        String str;

        @Override
        public void execute(Runnable next) {
            ChatProcessor.INSTANCE.addToChatQueue(str, next, false);
        }
    }
    @AllArgsConstructor
    public static class ActionOtherSay implements Action {
        String str;

        @Override
        public void execute(Runnable next) {
            try {
                HttpURLConnection huc = (HttpURLConnection) new URL("http://localhost:3000/").openConnection();
                huc.setDoOutput(true);
                huc.setRequestMethod("POST");
                huc.getOutputStream().write(str.getBytes());
                huc.connect();
                huc.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
            next.run();
        }
    }

    @AllArgsConstructor
    public static class ActionListen implements Action{
        Consumer<List<String>> callback;

        @Override
        public void execute(Runnable next) {
            ChatProcessor.INSTANCE.subscribe(new ChatSubscriber() {
                int state = 0;
                List<String> lol = new ArrayList<>();
                @Override
                public ChatProcessResult process(String txt, Map<String, Object> context) {
                    if (txt.startsWith("§9§m----------")) {
                        state++;
                    }
                    if (state > 0) {
                        lol.add(txt);
                    }
                    if (state == 2){
                        callback.accept(lol);
                        Minecraft.getInstance().execute(next);
                        return ChatProcessResult.REMOVE_LISTENER;
                    }
                    return ChatProcessResult.NONE;
                }
            });
        }
    }

    @AllArgsConstructor
    public static class ActionListen2 implements Action{
        Predicate<String> starting;
        Consumer<String> callback;

        @Override
        public void execute(Runnable next) {
            ChatProcessor.INSTANCE.subscribe(new ChatSubscriber() {
                @Override
                public ChatProcessResult process(String txt, Map<String, Object> context) {
                    if (starting.test(txt)) {
                        callback.accept(txt);
                        Minecraft.getInstance().execute(next);
                        return ChatProcessResult.REMOVE_LISTENER;
                    }
                    return ChatProcessResult.NONE;
                }
            });
        }
    }
    private static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(DungeonsGuide.THREAD_FACTORY);

    public static class ActionRejoinHypickle implements Action {

        @Override
        public void execute(Runnable next) {
            // 1.8.9 sent the disconnect packet, unloaded the world and opened the multiplayer screen over the main menu
            JoinMultiplayerScreen guiMultiplayer;
            Minecraft.getInstance().disconnect(guiMultiplayer = new JoinMultiplayerScreen(new TitleScreen()), false);
            ses.schedule(() -> {
                Minecraft.getInstance().execute(() -> {
                    // 1.8.9's selectServer(0) + connectToSelected, the first saved server
                    ServerList servers = new ServerList(Minecraft.getInstance());
                    servers.load();
                    guiMultiplayer.join(servers.get(0));

                    ses.schedule(() -> {
                        Minecraft.getInstance().execute(next::run);
                    }, 10, TimeUnit.SECONDS);
                });
            }, 3, TimeUnit.SECONDS);
        }
    }

    @AllArgsConstructor
    public static class ActionRun implements Action {
        private Runnable runnable;
        @Override
        public void execute(Runnable next) {
            runnable.run();
            next.run();
        }
    }
    @AllArgsConstructor
    public static class ActionWait implements Action {
        private int ms;

        @Override
        public void execute(Runnable next) {
            ses.schedule(() -> {
                Minecraft.getInstance().execute(next);
            }, ms, TimeUnit.MILLISECONDS);
        }
    }

    public void run() {

    }

    public void say(String a) {
        actions.add(new ActionSay(a));
    }
    public void otherSay(String a) {
        actions.add(new ActionOtherSay(a));
    }
    public void justRun(Runnable runnable) {
        actions.add(new ActionRun(runnable));
    }
    public void waitForPartyMessage(Consumer<List<String>> callback) {
        actions.add(new ActionListen(callback));
    }
    public void waitForSingleMessageMatching(Predicate<String> matcher, Consumer<String> callback) {
        actions.add(new ActionListen2(matcher, callback));
    }
    public void justWait(int ms) {
        actions.add(new ActionWait(ms));
    }

    public void rejoinHypickle() {
        actions.add(new ActionRejoinHypickle());
    }
    Iterator<Action> iterator;
    private void next() {
        iterator.next().execute(this::next);
    }
    public void execute() {
        run();
        iterator = actions.iterator();
        next();
    }
}
