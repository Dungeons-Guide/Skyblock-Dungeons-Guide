/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.stomp;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.events.impl.StompConnectedEvent;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StompManager {
    Logger logger = LogManager.getLogger("StompManager");
    public static final String STOMP_URL = "wss://dungeons.guide/ws";
    static StompManager instance;

    public static StompManager getInstance() {
        if (instance == null) {
            instance = new StompManager();
        }
        return instance;
    }

    public void init() {
        connectStomp();
    }

    @Getter
    private StompClient stompConnection;

    private int exponentialBackoffCoefficient = 0;

    void resetExponentialBackoff() {
        exponentialBackoffCoefficient = 0;
    }

    public boolean isStompConnected(){
        if(stompConnection != null && stompConnection.getStompClientStatus() == StompClient.StompClientStatus.CONNECTED) return true;
        return false;
    }

    public void send(StompPayload payload){
        if(stompConnection != null){
            stompConnection.sendFake(payload);
        } else {
            logger.error("OOPS STOMP CONNECTION IS NULL AND SOMEONE TRIED TO SEND SOMETHING THIS SHOULD NOT HAPPEN");
        }
    }

    ScheduledExecutorService ex = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
            .setNameFormat("DG-StompManager-%d").build()));

    private volatile boolean disconnecting = false;
    public void onStompDied(StompDiedEvent event) {
        if (disconnecting) return;
        logger.info("Stomp Connection closed, trying to reconnect in - {} - {} - {}", event.reason, event.code, 2 << exponentialBackoffCoefficient);
        connectStomp();
    }

    public void connectStomp() {
        ex.schedule(() -> {
            if (exponentialBackoffCoefficient < 5)
                exponentialBackoffCoefficient++;
            if (AuthManager.getInstance().getToken() == null) return;
            try {
                try {
                    if (stompConnection != null
                            && stompConnection.getStompClientStatus() == StompClient.StompClientStatus.CONNECTED) {
                        stompConnection.disconnect();
                    }
                } catch (Exception e) {
                    logger.error("Failed to reconnect (disconnection) to Stomp with message: {}", String.valueOf(Throwables.getRootCause(e)));
                }
                stompConnection = new StompClient(new URI(StompManager.STOMP_URL), AuthManager.getInstance().getWorkingTokenOrNull());
                MinecraftForge.EVENT_BUS.post(new StompConnectedEvent(stompConnection));
            } catch (Exception e) {
                logger.error("Failed to connect to Stomp with message: {}", String.valueOf(Throwables.getRootCause(e)));
            }
        }, 1L * (2L << exponentialBackoffCoefficient), TimeUnit.SECONDS);
    }

    public void cleanup() {
        if (stompConnection != null) {
            disconnecting = true;
            stompConnection.disconnect();
        }

        ex.shutdownNow();
    }
}
