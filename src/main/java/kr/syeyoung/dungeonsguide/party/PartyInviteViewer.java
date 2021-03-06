package kr.syeyoung.dungeonsguide.party;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ScreenRenderListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.arikia.dev.drpc.DiscordRPC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class PartyInviteViewer {
    public static final PartyInviteViewer INSTANCE = new PartyInviteViewer();

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        try {
            if (postRender.type == RenderGameOverlayEvent.ElementType.TEXT) {
                renderRequests(false);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onRender(GuiScreenEvent.DrawScreenEvent.Post postRender) {
        renderRequests(true);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent clientTickEvent) {
        try {
            if (clientTickEvent.phase != TickEvent.Phase.START) return;
            List<PartyJoinRequest> partyJoinRequestList = new ArrayList<>();
            boolean isOnHypixel = e.getDungeonsGuide().getSkyblockStatus().isOnHypixel();
            for (PartyJoinRequest joinRequest:joinRequests) {
                if (joinRequest.getTtl() != -1) {
                    joinRequest.setTtl(joinRequest.getTtl() - 1);
                    if (joinRequest.getTtl() == 0 || !isOnHypixel) {
                        partyJoinRequestList.add(joinRequest);
                    }
                } else if (!isOnHypixel){
                    DiscordRPC.discordRespond(joinRequest.getDiscordUser().userId, DiscordRPC.DiscordReply.NO);
                    partyJoinRequestList.add(joinRequest);
                } else if (joinRequest.getExpire() < System.currentTimeMillis()) {
                    partyJoinRequestList.add(joinRequest);
                }
            }
            joinRequests.removeAll(partyJoinRequestList);
        } catch (Throwable e) {e.printStackTrace();}
    }



    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGH)
    public void onRender(GuiScreenEvent.MouseInputEvent.Pre mouseInput) {
        int mouseX = Mouse.getX();
        int mouseY = Minecraft.getMinecraft().displayHeight - Mouse.getY() +3;
        for (PartyJoinRequest joinRequest:joinRequests) {
            if (joinRequest.getWholeRect() != null && joinRequest.getWholeRect().contains(mouseX, mouseY)) {
                mouseInput.setCanceled(true);

                if (Mouse.getEventButton() == -1) return;

                if (joinRequest.getReply() != null) {
                    joinRequests.remove(joinRequest);
                    return;
                }

                if (joinRequest.getAcceptRect().contains(mouseX, mouseY)) {
                    joinRequest.setReply(PartyJoinRequest.Reply.ACCEPT);
                    joinRequest.setTtl(60);
                    DiscordRPC.discordRespond(joinRequest.getDiscordUser().userId, DiscordRPC.DiscordReply.YES);
                    return;
                }

                if (joinRequest.getDenyRect().contains(mouseX, mouseY)) {
                    joinRequest.setReply(PartyJoinRequest.Reply.DENY);
                    joinRequest.setTtl(60);
                    DiscordRPC.discordRespond(joinRequest.getDiscordUser().userId, DiscordRPC.DiscordReply.NO);
                    return;
                }

                if (joinRequest.getIgnoreRect().contains(mouseX, mouseY)) {
                    joinRequest.setReply(PartyJoinRequest.Reply.IGNORE);
                    joinRequest.setTtl(60);
                    DiscordRPC.discordRespond(joinRequest.getDiscordUser().userId, DiscordRPC.DiscordReply.IGNORE);
                    return;
                }

                return;
            }
        }
    }


    public CopyOnWriteArrayList<PartyJoinRequest> joinRequests = new CopyOnWriteArrayList<>();

    ExecutorService executorService = Executors.newFixedThreadPool(3);
    public Map<String, Future<LoadedImage>> futureMap = new HashMap<>();
    public Map<String, LoadedImage> imageMap = new HashMap<>();

    public Future<LoadedImage> loadImage(String url) {
        if (imageMap.containsKey(url)) return CompletableFuture.completedFuture(imageMap.get(url));
        if (futureMap.containsKey(url)) return futureMap.get(url);
        Future<LoadedImage> future =  executorService.submit(() -> {
            try {
                URL urlObj = new URL(url);
                HttpURLConnection huc = (HttpURLConnection) urlObj.openConnection();
                huc.addRequestProperty("User-Agent", "DungeonsGuideMod (dungeonsguide.kro.kr, 1.0)");
                BufferedImage bufferedImage = ImageIO.read(huc.getInputStream());
                LoadedImage loadedImage = new LoadedImage();
                loadedImage.setImage(bufferedImage);
                imageMap.put(url, loadedImage);
                return loadedImage;
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
            String avatar = "https://cdn.discordapp.com/avatars/"+partyJoinRequest.getDiscordUser().userId+"/"+partyJoinRequest.getDiscordUser().avatar+".png";
            Future<LoadedImage> loadedImageFuture = loadImage(avatar);
            LoadedImage loadedImage = null;
            if (loadedImageFuture.isDone()) {
                try {
                    loadedImage = loadedImageFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            if (loadedImage != null) {
                if (loadedImage.getResourceLocation() == null) loadedImage.buildGLThings();
                TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
                textureManager.bindTexture(loadedImage.getResourceLocation());
                GlStateManager.pushAttrib();

                GlStateManager.disableLighting();
                GlStateManager.color(1, 1, 1, 1.0F);
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

                Gui.drawModalRectWithCustomSizedTexture(7, 7, 0, 0, height-14, height-14,loadedImage.getImage().getWidth(),loadedImage.getImage().getHeight());


                GlStateManager.enableLighting();
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.popAttrib();
            } else {
                Gui.drawRect(7, 7, height - 7, height-7, 0xFF4E4E4E);
            }
        }

            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            GlStateManager.pushMatrix();
                GlStateManager.translate(height +3,7, 0);

                GlStateManager.pushMatrix();
                    GlStateManager.scale(3.0,3.0,1.0);
                    fr.drawString(partyJoinRequest.getDiscordUser().username, 0,0, 0xFFFFFFFF, true);
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                    GlStateManager.translate(fr.getStringWidth(partyJoinRequest.getDiscordUser().username) * 3 + 1, (int)(fr.FONT_HEIGHT*1.5), 0);
                    fr.drawString("#"+partyJoinRequest.getDiscordUser().discriminator, 0,0,0xFFaaaaaa, true);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                    GlStateManager.translate(0, fr.FONT_HEIGHT * 3 + 5, 0);
                    GlStateManager.scale(1.0,1.0,1.0);
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
                        fr.drawString(text, 0, 0, 0xFFFFFFFF);
                    GlStateManager.popMatrix();
                    GlStateManager.translate(widthForTheThing, 0, 0);
                    partyJoinRequest.getDenyRect().setBounds(x + height + 3 + widthForTheThing, y + height - 25, widthForTheThing - 10, 25);
                    Gui.drawRect(0, 0, widthForTheThing - 10, 25, hover && partyJoinRequest.getDenyRect().contains(mouseX, mouseY) ? 0xFFAEC0CB : 0xFF99aab5);
                    GlStateManager.pushMatrix();
                        text = "Deny";
                        GlStateManager.translate((widthForTheThing - 10 - fr.getStringWidth(text) * 2) / 2, 15 - fr.FONT_HEIGHT, 0);
                        GlStateManager.scale(2.0f, 2.0f, 1.0f);
                        fr.drawString(text, 0, 0, 0xFFFFFFFF);
                    GlStateManager.popMatrix();
                    GlStateManager.translate(widthForTheThing, 0, 0);
                    partyJoinRequest.getIgnoreRect().setBounds(x + height + 3 + widthForTheThing + widthForTheThing, y + height - 25, widthForTheThing - 10, 25);
                    Gui.drawRect(0, 0, widthForTheThing - 10, 25, hover && partyJoinRequest.getIgnoreRect().contains(mouseX, mouseY) ? 0xFFAEC0CB : 0xFF99aab5); // AEC0CB
                    GlStateManager.pushMatrix();
                        text = "Ignore";
                        GlStateManager.translate((widthForTheThing - 10 - fr.getStringWidth(text) * 2) / 2, 15 - fr.FONT_HEIGHT, 0);
                        GlStateManager.scale(2.0f, 2.0f, 1.0f);
                        fr.drawString(text, 0, 0, 0xFFFFFFFF);
                    GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            } else {
                GlStateManager.pushMatrix();
                    GlStateManager.translate(height + 3, height - 28, 0);
                    GlStateManager.scale(2.0f,2.0f,1.0f);
                    fr.drawString(partyJoinRequest.getReply().getPast()+" the invite.",0,0,0xFFFFFFFF);
                GlStateManager.popMatrix();
            }
        GlStateManager.popMatrix();
    }
}
