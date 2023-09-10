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

package kr.syeyoung.dungeonsguide.launcher.auth;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.token.*;
import kr.syeyoung.dungeonsguide.launcher.events.AuthChangedEvent;
import kr.syeyoung.dungeonsguide.launcher.exceptions.auth.AuthFailedException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.auth.AuthenticationUnavailableException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.auth.PrivacyPolicyRequiredException;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.WidgetError;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.WidgetPrivacyPolicy;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.Notification;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.NotificationManager;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.WidgetNotification;
import kr.syeyoung.dungeonsguide.launcher.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.elements.GlobalHUDScale;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AuthManager {
    Logger logger = LogManager.getLogger("AuthManger");
    JsonObject configuration;

    private static AuthManager INSTANCE;

    public static AuthManager getInstance() {
        if(INSTANCE == null) INSTANCE = new AuthManager();
        return INSTANCE;
    }
    private AuthToken currentToken = new NullToken();

    public AuthToken getToken() {
        return currentToken;
    }
    public String getWorkingTokenOrNull() {
        if (currentToken instanceof DGAuthToken) return currentToken.getToken();
        else return null;
    }

    /**
     * @throws AuthenticationUnavailableException variations of it.
     * @return actual dg token
     */
    public String getWorkingTokenOrThrow() {
        if (currentToken instanceof DGAuthToken) return currentToken.getToken();
        else if (currentToken instanceof FailedAuthToken) throw new AuthFailedException(((FailedAuthToken) currentToken).getException());
        else if (currentToken instanceof NullToken) throw new AuthenticationUnavailableException("Null Token");
        else if (currentToken instanceof PrivacyPolicyRequiredToken) throw new PrivacyPolicyRequiredException();
        throw new IllegalStateException("weird token: "+currentToken);
    }
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("DgAuth Pool").build());


    private volatile boolean initlock = false;

    public void init() {
        if (initlock) {
            logger.info("Cannot init AuthManger twice");
            throw new IllegalStateException("Can not init AuthManager twice");
        }

        initlock = true;

        scheduler.scheduleAtFixedRate(() -> {
            boolean shouldReAuth = false;
            if (getToken().isUserVerified() && !getToken().getUUID().replace("-", "").equals(Minecraft.getMinecraft().getSession().getPlayerID())) {
                shouldReAuth = true;
                Main.getMain().unloadWithoutStacktraceReference();
            }
            if (!getToken().isAuthenticated()) {
                shouldReAuth = true;
            }
            if (shouldReAuth)
                try {
                    reAuth();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }, 10,10000, TimeUnit.MILLISECONDS);


        reAuth();
    }


    private volatile boolean reauthLock = false;

    private static final UUID authenticationFailure = UUID.randomUUID();
    private static final UUID privacyPolicyRequired = UUID.randomUUID();

    AuthToken reAuth() {
        if (reauthLock) {
            while (reauthLock) ;
            return currentToken;
        }

        reauthLock = true;

        try {
            String token = DgAuthUtil.requestAuth();
            byte[] encSecret = DgAuthUtil.checkSessionAuthenticityAndReturnEncryptedSecret(token);
            currentToken = DgAuthUtil.verifyAuth(token, encSecret);
            Minecraft.getMinecraft().addScheduledTask(() -> {
                MinecraftForge.EVENT_BUS.post(new AuthChangedEvent(currentToken));
            });


            if (currentToken instanceof PrivacyPolicyRequiredToken) {
                GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(new GlobalHUDScale(new WidgetPrivacyPolicy())));
                throw new PrivacyPolicyRequiredException();
            }


            NotificationManager.getInstance().removeNotification(authenticationFailure);
            NotificationManager.getInstance().removeNotification(privacyPolicyRequired);
        } catch (Exception e) {
            if (e instanceof PrivacyPolicyRequiredException) {
                NotificationManager.getInstance().removeNotification(authenticationFailure);
                NotificationManager.getInstance().updateNotification(privacyPolicyRequired, new WidgetNotification(privacyPolicyRequired, Notification.builder()
                        .title("Privacy Policy")
                        .description("Please accept Dungeons Guide\nPrivacy Policy to enjoy server based\nfeatures of Dungeons Guide\n\n(Including Auto-Update/Remote-Jar)")
                        .titleColor(0xFFFF0000)
                        .onClick(() -> {
                            GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(new GlobalHUDScale(new WidgetPrivacyPolicy())));
                        })
                        .build()));
            } else {
                File configFile = new File(Main.getConfigDir(), "config.json");
                if (configFile.exists()) {
                    try {
                        configuration = (JsonObject) new JsonParser().parse(new InputStreamReader(new FileInputStream(configFile)));
                    } catch (Exception e2) {
                        configuration = new JsonObject();
                    }
                }

                currentToken = new FailedAuthToken(e);

                if (configuration != null && configuration.get("auth").toString().contains("true")) {
                    // Error Hidden
                } else {
                    NotificationManager.getInstance().updateNotification(authenticationFailure, new WidgetNotification(authenticationFailure, Notification.builder()
                        .title("Auth Error")
                        .description("Authentication Error Occurred\n"+e.getMessage()+"\nDisable in Debug Config")
                        .titleColor(0xFFFF0000)
                        .onClick(() -> {
                            GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(new GlobalHUDScale(new WidgetError(e))));
                        })
                        .build()));
                }
            }
            logger.error("Re-auth failed with message {}, trying again in a 2 seconds", String.valueOf(Throwables.getRootCause(e)));
            throw new AuthFailedException(e);
        } finally {
            reauthLock = false;
        }
        return currentToken;
    }

    private volatile boolean accepting = false;
    public synchronized void acceptPrivacyPolicy(long version) {
        if (accepting) return;
        accepting = true;
        scheduler.schedule(() -> {try {
            acceptPrivacyPolicy0(version);
        } catch (Exception e) {e.printStackTrace();} finally {
            accepting = false;
        }}, 0, TimeUnit.MILLISECONDS);
    }

    private AuthToken acceptPrivacyPolicy0(long version) {
        if (reauthLock) {
            while(reauthLock);
            return currentToken;
        }

        if (currentToken instanceof PrivacyPolicyRequiredToken) {
            reauthLock = true;
            NotificationManager.getInstance().removeNotification(authenticationFailure);
            NotificationManager.getInstance().removeNotification(privacyPolicyRequired);
            try {
                currentToken = DgAuthUtil.acceptNewPrivacyPolicy(currentToken.getToken(), version);
                if (currentToken instanceof PrivacyPolicyRequiredToken) throw new PrivacyPolicyRequiredException();
            } catch (Exception e) {
                if (e instanceof PrivacyPolicyRequiredException) {
                    NotificationManager.getInstance().updateNotification(privacyPolicyRequired, new WidgetNotification(privacyPolicyRequired, Notification.builder()
                            .title("Privacy Policy")
                            .description("Please accept the Dungeons Guide\nPrivacy Policy to enjoy server based\nfeatures of Dungeons Guide\n\n(Including Auto-Update/Remote-Jar)")
                            .titleColor(0xFFFF0000)
                            .onClick(() -> {
                                GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(new GlobalHUDScale(new WidgetPrivacyPolicy())));
                            })
                            .build()));
                } else {
                    File configFile = new File(Main.getConfigDir(), "config.json");
                    if (configFile.exists()) {
                        try {
                            configuration = (JsonObject) new JsonParser().parse(new InputStreamReader(new FileInputStream(configFile)));
                        } catch (Exception e2) {
                            configuration = new JsonObject();
                        }
                    }

                    currentToken = new FailedAuthToken(e);

                    System.out.println(configuration.get("auth"));

                    if (configuration != null && configuration.get("auth").toString().contains("true")) {
                        // Error Hidden
                    } else {
                        NotificationManager.getInstance().updateNotification(authenticationFailure, new WidgetNotification(authenticationFailure, Notification.builder()
                                .title("Auth Error")
                                .description("Authentication Error Occurred\n"+e.getMessage()+"\nDisable in Debug Config")
                                .titleColor(0xFFFF0000)
                                .onClick(() -> {
                                    GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(new GlobalHUDScale(new WidgetError(e))));
                                })
                                .build()));
                    }
                }
                logger.error("Accepting the Privacy Policy failed with message {}, trying again in a 2 seconds", String.valueOf(Throwables.getRootCause(e)));
                throw new AuthFailedException(e);
            } finally {
                reauthLock = false;
            }
        }
        return currentToken;
    }
}
