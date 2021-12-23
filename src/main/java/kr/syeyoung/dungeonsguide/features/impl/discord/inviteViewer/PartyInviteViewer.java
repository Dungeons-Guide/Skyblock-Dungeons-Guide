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

package kr.syeyoung.dungeonsguide.features.impl.discord.inviteViewer;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.events.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.*;
import kr.syeyoung.dungeonsguide.rpc.RichPresenceManager;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordActivityJoinRequestReply;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class PartyInviteViewer extends SimpleFeature implements GuiPostRenderListener, ScreenRenderListener, TickListener, GuiClickListener, DiscordUserJoinRequestListener {
    public PartyInviteViewer() {
        super("Discord", "Party Invite Viewer","Simply type /dg asktojoin or /dg atj to toggle whether ask-to-join would be presented as option on discord!\n\nRequires Discord RPC to be enabled", "discord.party_invite_viewer");
    }

    @Override
    public boolean isDisyllable() {
        return false;
    }

    @Override
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        renderRequests(true);
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (!isEnabled()) return;
        try {
            renderRequests(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @Override
    public void onTick() {
        try {
            List<PartyJoinRequest> partyJoinRequestList = new ArrayList<>();
            boolean isOnHypixel = DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnHypixel();
            for (PartyJoinRequest joinRequest:joinRequests) {
                if (joinRequest.getTtl() != -1) {
                    joinRequest.setTtl(joinRequest.getTtl() - 1);
                    if (joinRequest.getTtl() == 0 || !isOnHypixel) {
                        partyJoinRequestList.add(joinRequest);
                    }
                } else if (!isOnHypixel){
//                    DiscordRPC.discordRespond(joinRequest.getDiscordUser().userId, DiscordRPC.DiscordReply.NO);
                    partyJoinRequestList.add(joinRequest);
                } else if (joinRequest.getExpire() < System.currentTimeMillis()) {
                    partyJoinRequestList.add(joinRequest);
                }
            }
            joinRequests.removeAll(partyJoinRequestList);
        } catch (Throwable e) {e.printStackTrace();}
    }


    @Override
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        if (!isEnabled()) return;
        int mouseX = Mouse.getX();
        int mouseY = Minecraft.getMinecraft().displayHeight - Mouse.getY() +3;
        for (PartyJoinRequest joinRequest:joinRequests) {
            if (joinRequest.getWholeRect() != null && joinRequest.getWholeRect().contains(mouseX, mouseY)) {
                mouseInputEvent.setCanceled(true);

                if (Mouse.getEventButton() == -1) return;

                if (joinRequest.getReply() != null) {
                    joinRequests.remove(joinRequest);
                    return;
                }

                if (!joinRequest.isInvite()) {
                    if (joinRequest.getAcceptRect().contains(mouseX, mouseY)) {
                        joinRequest.setReply(PartyJoinRequest.Reply.ACCEPT);
                        joinRequest.setTtl(60);
                        RichPresenceManager.INSTANCE.respond(joinRequest.getDiscordUser().id, EDiscordActivityJoinRequestReply.DiscordActivityJoinRequestReply_Yes);
                        return;
                    }

                    if (joinRequest.getDenyRect().contains(mouseX, mouseY)) {
                        joinRequest.setReply(PartyJoinRequest.Reply.DENY);
                        joinRequest.setTtl(60);
                        RichPresenceManager.INSTANCE.respond(joinRequest.getDiscordUser().id, EDiscordActivityJoinRequestReply.DiscordActivityJoinRequestReply_No);
                        return;
                    }

                    if (joinRequest.getIgnoreRect().contains(mouseX, mouseY)) {
                        joinRequest.setReply(PartyJoinRequest.Reply.IGNORE);
                        joinRequest.setTtl(60);
                        RichPresenceManager.INSTANCE.respond(joinRequest.getDiscordUser().id, EDiscordActivityJoinRequestReply.DiscordActivityJoinRequestReply_Ignore);
                        return;
                    }
                } else {
                    if (joinRequest.getAcceptRect().contains(mouseX, mouseY)) {
                        joinRequest.setReply(PartyJoinRequest.Reply.ACCEPT);
                        joinRequest.setTtl(60);
                        RichPresenceManager.INSTANCE.accept(joinRequest.getDiscordUser().id);
                        return;
                    }

                    if (joinRequest.getDenyRect().contains(mouseX, mouseY)) {
                        joinRequest.setReply(PartyJoinRequest.Reply.DENY);
                        joinRequest.setTtl(60);
                        return;
                    }
                }

                return;
            }
        }
    }



    public CopyOnWriteArrayList<PartyJoinRequest> joinRequests = new CopyOnWriteArrayList<>();

    ExecutorService executorService = Executors.newFixedThreadPool(3);
    public Map<String, Future<ImageTexture>> futureMap = new HashMap<>();
    public Map<String, ImageTexture> imageMap = new HashMap<>();

    public Future<ImageTexture> loadImage(String url) {
        if (imageMap.containsKey(url)) return CompletableFuture.completedFuture(imageMap.get(url));
        if (futureMap.containsKey(url)) return futureMap.get(url);
        Future<ImageTexture> future =  executorService.submit(() -> {
            try {
                ImageTexture imageTexture = new ImageTexture(url);
                imageMap.put(url, imageTexture);
                return imageTexture;
            } catch (Exception e) {
                throw e;
            }
        });
        futureMap.put(url,future);
        return future;
    }


    public void renderRequests(boolean hover) {
        try {
            GlStateManager.pushMatrix();
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            GlStateManager.scale(1.0 / sr.getScaleFactor(), 1.0 / sr.getScaleFactor(), 1.0);
            int height = 90;
            int gap = 5;
            int x = 5;
            int y = 5;
            for (PartyJoinRequest partyJoinRequest : joinRequests) {
                renderRequest(partyJoinRequest, x, y, 350,height, hover);
                y += height + gap;
            }
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    public void renderRequest(PartyJoinRequest partyJoinRequest, int x, int y, int width, int height, boolean hover) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        int mouseX = Mouse.getX();
        int mouseY = Minecraft.getMinecraft().displayHeight - Mouse.getY() +3;

        partyJoinRequest.getWholeRect().setBounds(x,y,width,height);


        GlStateManager.pushMatrix();
            GlStateManager.translate(x,y,0);

            Gui.drawRect(0, 0,width,height, 0xFF23272a);
            Gui.drawRect(2, 2, width-2, height-2, 0XFF2c2f33);
        {
            String avatar = "https://cdn.discordapp.com/avatars/"+Long.toUnsignedString(partyJoinRequest.getDiscordUser().id.longValue())+"/"+partyJoinRequest.getAvatar()+"."+(partyJoinRequest.getAvatar().startsWith("a_") ? "gif":"png");
            Future<ImageTexture> loadedImageFuture = loadImage(avatar);
            ImageTexture loadedImage = null;
            if (loadedImageFuture.isDone()) {
                try {
                    loadedImage = loadedImageFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            if (loadedImage != null) {
                loadedImage.drawFrameAndIncrement( 7,7,height-14,height-14);
            } else {
                Gui.drawRect(7, 7, height - 7, height-7, 0xFF4E4E4E);
            }
        }

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            GlStateManager.pushMatrix();
                GlStateManager.translate(height +3,7, 0);

                GlStateManager.pushMatrix();
                    GlStateManager.scale(3.0,3.0,1.0);
                    fr.drawString(partyJoinRequest.getUsername()+"", 0,0, 0xFFFFFFFF, true);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                    GlStateManager.translate(fr.getStringWidth(partyJoinRequest.getUsername()+"") * 3 + 1, (int)(fr.FONT_HEIGHT*1.5), 0);
                    fr.drawString("#"+partyJoinRequest.getDiscriminator(), 0,0,0xFFaaaaaa, true);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                    GlStateManager.translate(0, fr.FONT_HEIGHT * 3 + 5, 0);
                    GlStateManager.scale(1.0,1.0,1.0);
                    if (partyJoinRequest.isInvite())
                        fr.drawString("§ewants to you to join their party! ("+(TextUtils.formatTime(partyJoinRequest.getExpire() - System.currentTimeMillis()))+")", 0,0,0xFFFFFFFF,false);
                    else
                    fr.drawString("wants to join your party! ("+(TextUtils.formatTime(partyJoinRequest.getExpire() - System.currentTimeMillis()))+")", 0,0,0xFFFFFFFF,false);
                GlStateManager.popMatrix();
            GlStateManager.popMatrix();
            if (partyJoinRequest.getReply() == null) {
                GlStateManager.pushMatrix();
                    GlStateManager.translate(height + 3, height - 32, 0);
                    int widthForTheThing = (width - height) / 3;
                    GlStateManager.pushMatrix();
                        String text = "Accept";
                        partyJoinRequest.getAcceptRect().setBounds(x + height + 3, y + height - 25, widthForTheThing - 10, 25);
                        Gui.drawRect(0, 0, widthForTheThing - 10, 25, hover && partyJoinRequest.getAcceptRect().contains(mouseX, mouseY) ? 0xFF859DF0 : 0xFF7289da);
                        GlStateManager.translate((widthForTheThing - 10 - fr.getStringWidth(text) * 2) / 2, 15 - fr.FONT_HEIGHT, 0);

                        GlStateManager.scale(2.0f, 2.0f, 1.0f);
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        fr.drawString(text, 0, 0, 0xFFFFFFFF);
                    GlStateManager.popMatrix();
                    GlStateManager.translate(widthForTheThing, 0, 0);
                    partyJoinRequest.getDenyRect().setBounds(x + height + 3 + widthForTheThing, y + height - 25, widthForTheThing - 10, 25);
                    Gui.drawRect(0, 0, widthForTheThing - 10, 25, hover && partyJoinRequest.getDenyRect().contains(mouseX, mouseY) ? 0xFFAEC0CB : 0xFF99aab5);
                    GlStateManager.pushMatrix();
                        text = "Deny";
                        GlStateManager.translate((widthForTheThing - 10 - fr.getStringWidth(text) * 2) / 2, 15 - fr.FONT_HEIGHT, 0);
                        GlStateManager.scale(2.0f, 2.0f, 1.0f);
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        fr.drawString(text, 0, 0, 0xFFFFFFFF);
                    GlStateManager.popMatrix();
                if (!partyJoinRequest.isInvite()) {
                    GlStateManager.translate(widthForTheThing, 0, 0);
                    partyJoinRequest.getIgnoreRect().setBounds(x + height + 3 + widthForTheThing + widthForTheThing, y + height - 25, widthForTheThing - 10, 25);
                    Gui.drawRect(0, 0, widthForTheThing - 10, 25, hover && partyJoinRequest.getIgnoreRect().contains(mouseX, mouseY) ? 0xFFAEC0CB : 0xFF99aab5); // AEC0CB

                      GlStateManager.pushMatrix();
                      text = "Ignore";
                      GlStateManager.translate((widthForTheThing - 10 - fr.getStringWidth(text) * 2) / 2, 15 - fr.FONT_HEIGHT, 0);
                      GlStateManager.scale(2.0f, 2.0f, 1.0f);
                      GlStateManager.enableBlend();
                      GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                      GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                      fr.drawString(text, 0, 0, 0xFFFFFFFF);
                      GlStateManager.popMatrix();
                  }
                GlStateManager.popMatrix();
            } else {
                GlStateManager.pushMatrix();
                    GlStateManager.translate(height + 3, height - 28, 0);
                    GlStateManager.scale(2.0f,2.0f,1.0f);
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    fr.drawString(partyJoinRequest.getReply().getPast()+" the invite.",0,0,0xFFFFFFFF);
                GlStateManager.popMatrix();
            }
        GlStateManager.popMatrix();
    }

    @Override
    public void onDiscordUserJoinRequest(DiscordUserJoinRequestEvent event) {
        PartyJoinRequest partyInvite = new PartyJoinRequest();
        partyInvite.setDiscordUser(event.getDiscordUser());
        partyInvite.setExpire(System.currentTimeMillis() + 30000L);
        partyInvite.setInvite(event.isInvite());
        joinRequests.add(partyInvite);
    }
}
