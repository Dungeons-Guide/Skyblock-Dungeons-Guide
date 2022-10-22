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

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.MapProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.BossroomEnterListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonEndListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonStartListener;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.*;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.MapData;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureDungeonMap extends GuiFeature implements DungeonEndListener, DungeonStartListener, BossroomEnterListener {
    private AColor backgroudColor;
    private AColor playerColor;
    private boolean shouldCacheMap;
    private float playerHeadScale;
    private boolean shouldShowOtherPlayers;
    private float textScale;
    private boolean showSecretCount;
    private boolean showPlayerHeads;
    private boolean shouldRotateWithPlayer;
    private boolean shouldScale;
    private float postscaleOfMap;
    private boolean centerMapOnPlayer;

    public FeatureDungeonMap() {
        super("Dungeon", "Dungeon Map", "Display dungeon map!", "dungeon.map", true, 128, 128);
        this.setEnabled(false);
        addParameter("scale", new FeatureParameter<>("scale", "Scale map", "Whether to scale map to fit screen", true, "boolean", nval -> shouldScale = nval));
        addParameter("cacheMap", new FeatureParameter<>("cacheMap", "Should cache map data", "name", true, "boolean", nval -> shouldCacheMap = nval));
        addParameter("playerCenter", new FeatureParameter<>("playerCenter", "Center map at player", "Render you in the center", false, "boolean", nval -> centerMapOnPlayer = nval));
        addParameter("rotate", new FeatureParameter<>("rotate", "Rotate map centered at player", "Only works with Center map at player enabled", false, "boolean", nval -> shouldRotateWithPlayer = nval));
        addParameter("postScale", new FeatureParameter<>("postScale", "Scale factor of map", "Only works with Center map at player enabled", 1.0f, "float", nval -> postscaleOfMap = nval));
        addParameter("useplayerheads", new FeatureParameter<>("useplayerheads", "Use player heads instead of arrows", "Option to use player heads instead of arrows", true, "boolean", nval -> showPlayerHeads = nval));
        addParameter("showotherplayers", new FeatureParameter<>("showotherplayers", "Show other players", "Option to show other players in map", true, "boolean", nval -> shouldShowOtherPlayers = nval));
        addParameter("showtotalsecrets", new FeatureParameter<>("showtotalsecrets", "Show Total secrets in the room", "Option to overlay total secrets in the specific room", true, "boolean", nval -> showSecretCount = nval));
        addParameter("playerheadscale", new FeatureParameter<>("playerheadscale", "Player head scale", "Scale factor of player heads, defaults to 1", 1.0f, "float", nval -> playerHeadScale = nval));
        addParameter("textScale", new FeatureParameter<>("textScale", "Text scale", "Scale factor of texts on map, defaults to 1", 1.0f, "float", nval -> textScale = nval));

        addParameter("border_color", new FeatureParameter<>("border_color", "Color of the border", "Same as name", new AColor(255, 255, 255, 255), "acolor"));
        addParameter("background_color", new FeatureParameter<>("background_color", "Color of the background", "Same as name", new AColor(0x22000000, true), "acolor", nval -> backgroudColor = nval));
        addParameter("player_color", new FeatureParameter<>("player_color", "Color of the player border", "Same as name", new AColor(255, 255, 255, 0), "acolor", nval -> playerColor = nval));
    }

    public static final Ordering<NetworkPlayerInfo> sorter = Ordering.from((compare1, compare2) -> {
        ScorePlayerTeam scoreplayerteam = compare1.getPlayerTeam();
        ScorePlayerTeam scoreplayerteam1 = compare2.getPlayerTeam();
        return ComparisonChain.start().compareTrueFirst(compare1.getGameType() != WorldSettings.GameType.SPECTATOR, compare2.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(compare1.getGameProfile().getName(), compare2.getGameProfile().getName()).result();
    });

    private boolean on = false;

    @Override
    public void onDungeonEnd() {
        on = false;
    }

    @Override
    public void onDungeonStart() {
        on = true;
    }

    @Override
    public void onBossroomEnter() {
        on = false;
    }

    @Override
    public void drawHUD(float partialTicks) {
        if (!on) return;
        if (!DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getSkyblockStatus().getContext();
        if (context == null || !context.getMapProcessor().isInitialized()) return;

        MapProcessor mapProcessor = context.getMapProcessor();
        MapData mapData = mapProcessor.getLastMapData2();
        Rectangle featureRect = getFeatureRect().getRectangle();
        Gui.drawRect(0, 0, featureRect.width, featureRect.height, RenderUtils.getColorAt(featureRect.x, featureRect.y, backgroudColor));
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        if (mapData == null) {
            Gui.drawRect(0, 0, featureRect.width, featureRect.height, 0xFFFF0000);
        } else {
            renderMap(partialTicks, mapProcessor, mapData, context);
        }
        GlStateManager.popMatrix();
        GL11.glLineWidth(2);
        RenderUtils.drawUnfilledBox(0, 0, featureRect.width, featureRect.height, this.<AColor>getParameter("border_color").getValue());
    }

    @Override
    public void drawDemo(float partialTicks) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext();
        if (DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon() && context != null && context.getMapProcessor().isInitialized() && on) {
            drawHUD(partialTicks);
            return;
        }
        Rectangle featureRect = getFeatureRect().getRectangle();
        Gui.drawRect(0, 0, featureRect.width, featureRect.height, RenderUtils.getColorAt(featureRect.x, featureRect.y, backgroudColor));
        FontRenderer fr = getFontRenderer();

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Please join a dungeon to see preview", featureRect.width / 2 - fr.getStringWidth("Please join a dungeon to see preview") / 2, featureRect.height / 2 - fr.FONT_HEIGHT / 2, 0xFFFFFFFF);
        GL11.glLineWidth(2);
        RenderUtils.drawUnfilledBox(0, 0, featureRect.width, featureRect.height, this.<AColor>getParameter("border_color").getValue());
    }

    public void renderMap(float partialTicks, MapProcessor mapProcessor, MapData mapData, DungeonContext context) {
        EntityPlayer p = Minecraft.getMinecraft().thePlayer;

        float postScale = this.centerMapOnPlayer ? postscaleOfMap : 1;

        Rectangle featureRect = getFeatureRect().getRectangle();
        int width = featureRect.width;

        float scale = shouldScale ? width / 128.0f : 1;

        GlStateManager.translate(width / 2d, width / 2d, 0);
        GlStateManager.scale(scale, scale, 0);
        GlStateManager.scale(postScale, postScale, 0);

        Vector2d pt = mapProcessor.worldPointToMapPointFLOAT(p.getPositionEyes(partialTicks));
        double yaw = p.rotationYaw;
        if (this.centerMapOnPlayer) {
            if (this.shouldRotateWithPlayer) {
                GlStateManager.rotate((float) (180.0 - yaw), 0, 0, 1);
            }
            GlStateManager.translate(-pt.x, -pt.y, 0);
        } else {
            GlStateManager.translate(-64, -64, 0);
        }

        updateMapTexture(mapData.colors, mapProcessor, context.getDungeonRoomList());

        render();


        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(1, 771, 0, 1);

        if (this.showPlayerHeads) {
            renderHeads(mapProcessor, mapData, scale, postScale, partialTicks);
        } else {
            renderArrows(mapData, scale, postScale);
        }


        FontRenderer fr = getFontRenderer();
        if (this.showSecretCount) {
            for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
                GlStateManager.pushMatrix();

                Point mapPt = mapProcessor.roomPointToMapPoint(dungeonRoom.getUnitPoints().get(0));
                GlStateManager.translate(mapPt.x + mapProcessor.getUnitRoomDimension().width / 2d, mapPt.y + mapProcessor.getUnitRoomDimension().height / 2d, 0);

                if (this.centerMapOnPlayer && this.shouldRotateWithPlayer) {
                    GlStateManager.rotate((float) (yaw - 180), 0, 0, 1);
                }
                GlStateManager.scale(1 / scale, 1 / scale, 0);
                GlStateManager.scale(1 / postScale, 1 / postScale, 0);
                GlStateManager.scale(textScale, textScale, 0);
                String str = "";
                if (dungeonRoom.getTotalSecrets() == -1) {
                    str += "? ";
                } else if (dungeonRoom.getTotalSecrets() != 0) {
                    str += dungeonRoom.getTotalSecrets() + " ";
                }

                DungeonRoom.RoomState currentState = dungeonRoom.getCurrentState();
                switch (currentState) {
                    case FINISHED:
                    case COMPLETE_WITHOUT_SECRETS:
                        str += "✔";
                        break;
                    case DISCOVERED:
                        str += "☐";
                        break;
                    case FAILED:
                        str += "❌";
                        break;
                }


                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                if (currentState == DungeonRoom.RoomState.FINISHED) {
                    fr.drawString(str, -(fr.getStringWidth(str) / 2), -(fr.FONT_HEIGHT / 2), 0xFF00FF00);
                } else {
                    if (dungeonRoom.getColor() == 74) {
                        fr.drawString(str, -(fr.getStringWidth(str) / 2), -(fr.FONT_HEIGHT / 2), 0xff000000);
                    } else {
                        fr.drawString(str, -(fr.getStringWidth(str) / 2), -(fr.FONT_HEIGHT / 2), 0xFFFFFFFF);
                    }
                }

                GlStateManager.popMatrix();
            }
        }

    }


    private final DynamicTexture mapTexture = new DynamicTexture(128, 128);
    private final ResourceLocation generatedMapTexture = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("dungeonmap/map", mapTexture);
    private final int[] mapTextureData = mapTexture.getTextureData();

    private void updateMapTexture(byte[] colors, MapProcessor mapProcessor, List<DungeonRoom> dungeonRooms) {

        if(shouldCacheMap){
            if(!didMapChange(dungeonRooms)){
                return;
            }
        }


        for (int i = 0; i < 16384; ++i) {
            int j = colors[i] & 255;

            if (j / 4 == 0) {
                this.mapTextureData[i] = 0x00000000;
            } else {
                this.mapTextureData[i] = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
            }
        }

        if (this.showSecretCount) {
            for (DungeonRoom dungeonRoom : dungeonRooms) {
                for (Point pt : dungeonRoom.getUnitPoints()) {
                    for (int y1 = 0; y1 < mapProcessor.getUnitRoomDimension().height; y1++) {
                        for (int x1 = 0; x1 < mapProcessor.getUnitRoomDimension().width; x1++) {
                            int x = MathHelper.clamp_int(pt.x * (mapProcessor.getUnitRoomDimension().width + mapProcessor.getDoorDimension().height) + x1 + mapProcessor.getTopLeftMapPoint().x, 0, 128);
                            int y = MathHelper.clamp_int(pt.y * (mapProcessor.getUnitRoomDimension().height + mapProcessor.getDoorDimension().height) + y1 + mapProcessor.getTopLeftMapPoint().y, 0, 128);
                            int i = y * 128 + x;
                            int j = dungeonRoom.getColor();

                            if (j / 4 == 0) {
                                this.mapTextureData[i] = 0x00000000;
                            } else {
                                this.mapTextureData[i] = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
                            }
                        }
                    }
                }
            }
        }


        this.mapTexture.updateDynamicTexture();
    }

    int[] lastRoomColors = new int[50];

    int[] lastRoomSecrets = new int[50];

    private boolean didMapChange(List<DungeonRoom> dungeonRooms) {
        int[] currentRoomColors = new int[50];
        int[] currentRoomSecrets = new int[50];

        for (int i = 0; i < dungeonRooms.size(); i++) {
            DungeonRoom dungeonRoom = dungeonRooms.get(i);

            currentRoomSecrets[i] = dungeonRoom.getTotalSecrets();
            currentRoomColors[i] = dungeonRoom.getColor();

        }

        for (int i = 0; i < 50; i++) {
            if(lastRoomColors[i] != currentRoomColors[i] || lastRoomSecrets[i] != currentRoomSecrets[i]) {
                lastRoomColors = currentRoomColors;
                lastRoomSecrets = currentRoomSecrets;
                return true;
            }
        }

        lastRoomColors = currentRoomColors;
        lastRoomSecrets = currentRoomSecrets;
        return false;
    }

    @Nullable
    public Tuple<String[], List<NetworkPlayerInfo>> loadPlayerList() {
        String[] names = new String[21];

        List<NetworkPlayerInfo> networkList = sorter.sortedCopy(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());
        if (networkList.size() < 40) return null;

        for (int i = 0; i < 20; i++) {
            names[i] = getPlayerNameWithChecks(networkList.get(i));
        }

        return new Tuple<>(names, networkList);
    }

    long nextRefresh;

    Tuple<String[], List<NetworkPlayerInfo>> playerListCached;

    public Tuple<String[], List<NetworkPlayerInfo>> getPlayerListCached(){
        if(playerListCached == null || nextRefresh <= System.currentTimeMillis()){
            DungeonsGuide.sendDebugChat("Refreshing players on map");
            playerListCached = loadPlayerList();
            nextRefresh = System.currentTimeMillis() + 10000;
        }
        return playerListCached;
    }


    private void renderHeads(MapProcessor mapProcessor, MapData mapData, float scale, float postScale, float partialTicks) {
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;

        Tuple<String[], List<NetworkPlayerInfo>> playerList = getPlayerListCached();

        List<NetworkPlayerInfo> networkList = playerList.getSecond();
        String[] names = playerList.getFirst();



        // 21 iterations bc we only want to scan the player part of tab list
        for (int i = 1; i < 20; i++) {
            NetworkPlayerInfo networkPlayerInfo = networkList.get(i);

            String name = names[i];
            if (name == null) continue;


            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);

            Vector2d pt2 = null;
            double yaw2 = 0;

            if (entityplayer != null && (!entityplayer.isInvisible() || entityplayer == thePlayer)) {
                // getting location from player entity
                pt2 = mapProcessor.worldPointToMapPointFLOAT(entityplayer.getPositionEyes(partialTicks));
                yaw2 = entityplayer.prevRotationYawHead + (entityplayer.rotationYawHead - entityplayer.prevRotationYawHead) * partialTicks;
                System.out.println("Got player location from entity");
            }
//            else {
//                // getting player location from map
//                String iconName = mapProcessor.getMapIconToPlayerMap().get(name);
//                if (iconName != null) {
//                    Vec4b vec = mapData.mapDecorations.get(iconName);
//                    if (vec != null) {
//                        System.out.println("Got player location from map");
//                        pt2 = new Vector2d(vec.func_176112_b() / 2d + 64, vec.func_176113_c() / 2d + 64);
//                        yaw2 = vec.func_176111_d() * 360 / 16.0f;
//                    } else {
//                        System.out.println("Failed getting location from map, vec is null");
//                    }
//                } else {
//                    System.out.println("Failed getting location from map, icon name is null");
//                }
//            }

            if(pt2 == null) return;

            if (entityplayer == thePlayer || shouldShowOtherPlayers) {
                drawHead(scale, postScale, networkPlayerInfo, entityplayer, pt2, (float) yaw2);
            }

        }
    }

    /**
     * @param scale Scale of the map
     * @param postScale
     * @param networkPlayerInfo
     * @param entityplayer
     * @param pt2
     * @param yaw2
     */
    private void drawHead(float scale, float postScale, NetworkPlayerInfo networkPlayerInfo, EntityPlayer entityplayer, Vector2d pt2, float yaw2) {
        GlStateManager.pushMatrix();
        boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE);
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin());
        int l2 = 8 + (flag1 ? 8 : 0);
        int i3 = 8 * (flag1 ? -1 : 1);

        GlStateManager.translate(pt2.x, pt2.y, 0);
        GlStateManager.rotate(yaw2, 0, 0, 1);

        GlStateManager.scale(1 / scale, 1 / scale, 0);
        GlStateManager.scale(1 / postScale, 1 / postScale, 0);

        GlStateManager.scale(playerHeadScale, playerHeadScale, 0);

        // cutting out the player head out of the skin texture
        Gui.drawScaledCustomSizeModalRect(-4, -4, 8.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
        GL11.glLineWidth(1);
        RenderUtils.drawUnfilledBox(-4, -4, 4, 4, this.playerColor);
        GlStateManager.popMatrix();
    }

    final static Pattern tabListRegex = Pattern.compile("\\*[a-zA-Z0-9_]{2,16}\\*", Pattern.MULTILINE);

    /**
     * We make sure that the player is alive and regex their name out
     * @param networkPlayerInfo the network player info of player
     * @return the username of player
     */
    @Nullable
    public static String getPlayerNameWithChecks(NetworkPlayerInfo networkPlayerInfo) {
        String name;
        if (networkPlayerInfo.getDisplayName() != null) {
            name = networkPlayerInfo.getDisplayName().getFormattedText();
        } else {
            name = ScorePlayerTeam.formatPlayerName(
                    networkPlayerInfo.getPlayerTeam(),
                    networkPlayerInfo.getGameProfile().getName()
            );
        }

        if (name.trim().equals("§r") || name.startsWith("§r ")) return null;

        name = TextUtils.stripColor(name);

        if(name.contains("(DEAD)")) {
            return null;
        }

        return getString(name, tabListRegex);
    }

    @Nullable
    public static String getString(String name, Pattern tabListRegex) {
        name = name.replace(" ", "*");

        Matcher matcher = tabListRegex.matcher(name);
        if (!matcher.find()) return null;

        name = matcher.group(0);
        name = name.substring(0, name.length() - 1);
        name = name.substring(1);
        return name;
    }


    private static final ResourceLocation mapIcons = new ResourceLocation("textures/map/map_icons.png");

    private void renderArrows(MapData mapData, float scale, float postScale) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int k = 0;
        Minecraft.getMinecraft().getTextureManager().bindTexture(mapIcons);
        for (Vec4b vec4b : mapData.mapDecorations.values()) {
            if (vec4b.func_176110_a() == 1 || this.shouldShowOtherPlayers) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(vec4b.func_176112_b() / 2.0F + 64.0F, vec4b.func_176113_c() / 2.0F + 64.0F, -0.02F);
                GlStateManager.rotate((vec4b.func_176111_d() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);

                GlStateManager.scale(1 / scale, 1 / scale, 0);
                GlStateManager.scale(1 / postScale, 1 / postScale, 0);
                GlStateManager.scale(this.playerHeadScale * 5, this.playerHeadScale * 5, 0);

                GlStateManager.translate(-0.125F, 0.125F, 0.0F);
                byte b0 = vec4b.func_176110_a();
                float f1 = (b0 % 4) / 4.0F;
                float f2 = (b0 / 4f) / 4.0F;
                float f3 = (b0 % 4 + 1) / 4.0F;
                float f4 = (b0 / 4f + 1) / 4.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(-1.0D, 1.0D, k * -0.001F).tex(f1, f2).endVertex();
                worldrenderer.pos(1.0D, 1.0D, k * -0.001F).tex(f3, f2).endVertex();
                worldrenderer.pos(1.0D, -1.0D, k * -0.001F).tex(f3, f4).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, k * -0.001F).tex(f1, f4).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
                ++k;
            }
        }
    }

    private void render() {
        int i = 0;
        int j = 0;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = 0.0F;
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.generatedMapTexture);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(1, 771, 0, 1);
        GlStateManager.disableAlpha();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((i) + f, (j + 128) - f, -0.009999999776482582D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos((i + 128) - f, (j + 128) - f, -0.009999999776482582D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos((i + 128) - f, (j) + f, -0.009999999776482582D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos((i) + f, (j) + f, -0.009999999776482582D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, -0.04F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

}
