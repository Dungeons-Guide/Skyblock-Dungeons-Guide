package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.EDungeonDoorType;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlay;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2d;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class WidgetDungeonMap extends Widget implements Renderer {

    private MapConfiguration mapConfiguration;
    private Supplier<List<MapOverlay>> getOverlays;
    public WidgetDungeonMap(MapConfiguration mapConfiguration, Supplier<List<MapOverlay>> getOverlays) {
        this.mapConfiguration = mapConfiguration;
        this.getOverlays = getOverlays;
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    private double mouseX;
    private double mouseY;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        this.mouseX = relMouseX0;
        this.mouseY = relMouseY0;
        return true;
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null || context.getScaffoldParser() == null) return false;
        DungeonRoomScaffoldParser mapProcessor = context.getScaffoldParser();


        Size featureRect = getDomElement().getSize();

        int width2 = (int) featureRect.getWidth();
        float scale = width2 / 128.0f;

        relMouseX /= scale;
        relMouseY /= scale;

        relMouseX -= 64;
        relMouseY -= 64;


        if (context.getBossfightProcessor() != null) {
            BossfightRenderSettings bossfightRenderSettings = context.getBossfightProcessor().getMapRenderSettings();

            double x, y, width, height;
            if (bossfightRenderSettings.getTextureWidth() > bossfightRenderSettings.getTextureHeight()) {
                double rHeight = bossfightRenderSettings.getTextureHeight() * 128.0 / bossfightRenderSettings.getTextureWidth();
                x = 0; y = (128 -rHeight) / 2; width = 128; height = rHeight;
            } else {
                double rWidth = bossfightRenderSettings.getTextureWidth() * 128.0 / bossfightRenderSettings.getTextureHeight();
                x = (128 - rWidth) / 2; y = 0; width = rWidth; height = 128;
            }

            for (MapOverlay mapOverlay : getOverlays.get()) {
                double xCoord = mapOverlay.getX(0);
                double zCoord = mapOverlay.getZ(0);
                double px = width * (xCoord - bossfightRenderSettings.getMinX()) / (bossfightRenderSettings.getMaxX() - bossfightRenderSettings.getMinX()) + x;
                double pz = height * (zCoord - bossfightRenderSettings.getMinZ()) / (bossfightRenderSettings.getMaxZ() - bossfightRenderSettings.getMinZ()) + y;


                if (mapOverlay.onClick(relMouseX + 64 - px, relMouseY + 64 - pz, getDomElement())) return true;
            }
        } else {
            double yaw = ((Minecraft.getInstance().player.getYHeadRot()) % 360 + 360) % 360;

            Vector2d pt = mapProcessor.getDungeonMapLayout().worldPointToMapPointFLOAT(Minecraft.getInstance().player.getEyePosition(0));

            relMouseX /= mapConfiguration.getMapScale();
            relMouseY /= mapConfiguration.getMapScale();
            if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.VERTICAL) {
                if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.CENTER) {
                    float angle = (float) ((yaw - 180) * Math.PI / 180);

                    double ncalcMouseX = Math.cos(angle) * relMouseX + Math.sin(angle) * -relMouseY;
                    double ncalcMouseY = Math.cos(angle) * relMouseY + Math.sin(angle) * relMouseX;

                    relMouseX = ncalcMouseX;
                    relMouseY = ncalcMouseY;
                }
                if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.ROTATE) {
                    relMouseX += pt.x;
                    relMouseY += pt.y;
                } else {
                    relMouseX += 64;
                    relMouseY += 64;
                }
            } else {
                relMouseX += 64;
                relMouseY += 64;
            }

            for (MapOverlay marker : getOverlays.get()) {
                double xCoord = marker.getX(0);
                double zCoord = marker.getZ(0);
                Vector2d loc = mapProcessor.getDungeonMapLayout().worldPointToMapPointFLOAT(new Vec3(xCoord, 0, zCoord));
                double px = loc.x;
                double pz = loc.y;


                // well mouse pos is incorrect. :/
                // rotate MouseX
                if (marker.onClick(relMouseX - px, relMouseY - pz, getDomElement())) return true;
            }

        }

        return false;
    }

    @Override
    public void doRender(float partialTicks, RenderingContext renderingContext, DomElement buildContext) {
        if (!SkyblockStatus.isOnDungeon()) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (context == null || context.getScaffoldParser() == null) return;
        DungeonRoomScaffoldParser mapProcessor = context.getScaffoldParser();

        MapItemSavedData mapData = mapProcessor.getLatestMapData();
        Size featureSize = getDomElement().getSize();
        // TODO: redo chroma
        renderingContext.drawRect(0, 0, (int)featureSize.getWidth(), (int)featureSize.getHeight(), RenderUtils.getColorAt(0,0, mapConfiguration.getBackgroundColor()));
        renderingContext.pose().pushMatrix();
//        if (mapData == null) {
//            Gui.drawRect(0, 0, (int)featureSize.getWidth(), (int)featureSize.getHeight(), 0xFFFF0000);
//        } else {
            renderMap(renderingContext, partialTicks, context);
//        }
        renderingContext.pose().popMatrix();
        renderingContext.drawUnfilledBox(0, 0, (int)featureSize.getWidth(), (int)featureSize.getHeight(), mapConfiguration.getBorderWidth(), mapConfiguration.getBorder());
    }



    public void renderMap(RenderingContext ctx, float partialTicks, DungeonContext dungeonContext) {
        DungeonRoomScaffoldParser mapProcessor = dungeonContext.getScaffoldParser();

        Player p = Minecraft.getInstance().player;


        Size featureRect = getDomElement().getSize();
        int width = (int) featureRect.getWidth();
        float scale = width / 128.0f;
        double calcMouseX = mouseX, calcMouseY = mouseY;

        ctx.pose().translate((float) (width / 2.0), (float) (width / 2.0));
        ctx.pose().scale((float) scale, (float) scale);
        calcMouseX /= scale;
        calcMouseY /= scale;

        calcMouseX -= 64;
        calcMouseY -= 64;

        Vector2d pt = mapProcessor.getDungeonMapLayout().worldPointToMapPointFLOAT(p.getEyePosition(partialTicks));

        if (dungeonContext.getBossfightProcessor() != null) {
            ctx.pose().translate(-64, -64);
            BossfightProcessor bossfightProcessor = dungeonContext.getBossfightProcessor();
            BossfightRenderSettings settings = bossfightProcessor.getMapRenderSettings();
            if (settings != null) {
                renderBossfight(ctx, partialTicks, scale, settings, getOverlays.get());
            }


        } else {

            double yaw = ((p.yHeadRotO + (p.getYHeadRot() - p.yHeadRotO) * partialTicks) % 360 + 360) % 360;

            boolean rotated = false;
            ctx.pose().scale((float) mapConfiguration.getMapScale(), (float) mapConfiguration.getMapScale());
            calcMouseX /= mapConfiguration.getMapScale();
            calcMouseY /= mapConfiguration.getMapScale();
            if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.VERTICAL) {
                if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.CENTER) {
                    ctx.pose().rotate((float) Math.toRadians(180.0 - yaw));


                    float angle = (float) ((yaw - 180) * Math.PI / 180);

                    double ncalcMouseX = Math.cos(angle) * calcMouseX + Math.sin(angle) * -calcMouseY;
                    double ncalcMouseY = Math.cos(angle) * calcMouseY + Math.sin(angle) * calcMouseX;

                    calcMouseX = ncalcMouseX;
                    calcMouseY = ncalcMouseY;

                    rotated = true;
                }
                if (mapConfiguration.getMapRotation() != MapConfiguration.MapRotation.ROTATE) {
                    ctx.pose().translate((float) -pt.x, (float) -pt.y);
                    calcMouseX += pt.x;
                    calcMouseY += pt.y;
                } else {
                    ctx.pose().translate(-64, -64);
                    calcMouseX += 64;
                    calcMouseY += 64;
                }
            } else {
                ctx.pose().translate(-64, -64);
                calcMouseX += 64;
                calcMouseY += 64;
            }


            double snapRotation = 0;
            if (rotated) {
                snapRotation =  (yaw - 180) % 360;
            }
            renderRooms(ctx, mapProcessor);


            renderIcons(ctx, mapProcessor, scale * mapConfiguration.getMapScale(), snapRotation + 360);
            for (MapOverlay marker : getOverlays.get()) {
                double xCoord = marker.getX(partialTicks);
                double zCoord = marker.getZ(partialTicks);
                Vector2d loc = mapProcessor.getDungeonMapLayout().worldPointToMapPointFLOAT(new Vec3(xCoord, 0, zCoord));
                double px = loc.x;
                double pz = loc.y;

                ctx.pose().pushMatrix();
                ctx.pose().translate((float) px, (float) pz);

                // well mouse pos is incorrect. :/
                // rotate MouseX

                marker.doRender(ctx, 0, partialTicks, scale * mapConfiguration.getMapScale(), calcMouseX - px, calcMouseY - pz);

                ctx.pose().popMatrix();
            }
        }
    }


    private void renderBossfight(RenderingContext ctx, float partialTicks, float scale, BossfightRenderSettings bossfightRenderSettings, List<MapOverlay> overlays) {
        double x, y, width, height;
        if (bossfightRenderSettings.getTextureWidth() > bossfightRenderSettings.getTextureHeight()) {
            double rHeight = bossfightRenderSettings.getTextureHeight() * 128.0 / bossfightRenderSettings.getTextureWidth();
            x = 0; y = (128 -rHeight) / 2; width = 128; height = rHeight;
            ctx.drawScaledCustomSizeModalRect(bossfightRenderSettings.getResourceLocation(),
                    0,(128 -rHeight) / 2, 0, 0, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight(), 128, rHeight, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight()
            );
        } else {
            double rWidth = bossfightRenderSettings.getTextureWidth() * 128.0 / bossfightRenderSettings.getTextureHeight();
            x = (128 - rWidth) / 2; y = 0; width = rWidth; height = 128;
            ctx.drawScaledCustomSizeModalRect(bossfightRenderSettings.getResourceLocation(),
                    (128 - rWidth) / 2,0, 0, 0, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight(), rWidth, 128, bossfightRenderSettings.getTextureWidth(), bossfightRenderSettings.getTextureHeight()
            );
        }

        for (MapOverlay marker : overlays) {
            double xCoord = marker.getX(partialTicks);
            double zCoord = marker.getZ(partialTicks);
            double px = width * (xCoord - bossfightRenderSettings.getMinX()) / (bossfightRenderSettings.getMaxX() - bossfightRenderSettings.getMinX()) + x;
            double pz = height * (zCoord - bossfightRenderSettings.getMinZ()) / (bossfightRenderSettings.getMaxZ() - bossfightRenderSettings.getMinZ()) + y;


            ctx.pose().pushMatrix();
            ctx.pose().translate((float) px, (float) pz);

            marker.doRender(ctx, 0, partialTicks, scale, mouseX / scale - px, mouseY / scale - pz);

            ctx.pose().popMatrix();
        }
    }

    private final Identifier resourceLocation = Identifier.parse("dungeonsguide:map/maptexture.png");

    private Rectangle maxFit(int rot, short shape) {
        int[] patterns = new int[]{
                0xF, 0xF0, 0x0F00, 0xF000,
                0x1111, 0x2222, 0x4444, 0x8888
        };
        int[] cnts = new int[8];

        for (int i = 0; i < patterns.length; i++) {
            cnts[i] = patterns[i] & shape;
            if (i < 4) cnts[i] >>= i * 4;
            else cnts[i] >>= (i-4);
        }

        if (rot % 2 == 0) {
            int minY = 0;
            int maxY = 0;
            int currVal = 0;
//            System.out.println("---"+shape);
            for (int i = 0; i < 4; i++) {
                int bits = Integer.bitCount(cnts[i]);
                if (bits > currVal) {
                    minY = i;
                    maxY = i + 1;
                    currVal = bits;
                } else if (bits == currVal && cnts[i] == cnts[minY]) {
                    maxY = i + 1;
                }
            }
            int minX = Integer.numberOfTrailingZeros(cnts[minY]);
            int maxX = minX + currVal;

            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        } else {
            int minX = 0;
            int maxX = 0;
            int currVal = 0;
            for (int i = 4; i < 8; i++) {
                int bits = Integer.bitCount(cnts[i]);
                if (bits > currVal) {
                    minX = i - 4;
                    maxX = i - 3;
                    currVal = bits;
                } else if (bits == currVal && cnts[i] == cnts[minX + 4]) {
                    maxX = i - 3;
                }
            }
            int minY = Integer.numberOfTrailingZeros(cnts[minX]) / 4;
            int maxY = minY + currVal;

            return new Rectangle(minX, minY, maxX - minX, maxY - minY);
        }

    }

    private void renderIcons(RenderingContext ctx, DungeonRoomScaffoldParser scaffoldParser, double scale, double snapRotation) {

        DungeonMapLayout layout = scaffoldParser.getDungeonMapLayout();

        int unitRoomBigWidth = layout.getUnitRoomSize().width + layout.getMapRoomGap();
        int unitRoomBigHeight = layout.getUnitRoomSize().height + layout.getMapRoomGap();
        int unitRoomWidth = layout.getUnitRoomSize().width;
        int unitRoomHeight = layout.getUnitRoomSize().height;
        int gap = layout.getMapRoomGap();

        double chkmark = mapConfiguration.getCheckmarkSettings().getScale();
        int pad = (int) mapConfiguration.getNameSettings().getPadding();
        MapConfiguration.NameSettings.NameRotation nameRotation = mapConfiguration.getNameSettings().getNameRotation();
        for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
            DungeonRoomInfo dungeonRoomInfo = dungeonRoom.getDungeonRoomInfo();
            MapConfiguration.RoomOverride override = dungeonRoomInfo == null ? null : mapConfiguration.getRoomOverrides().get(dungeonRoomInfo.getUuid());
//            MapConfiguration.RoomOverride override = mapConfiguration.getRoomOverrides().values().stream().findFirst().orElse(null);


            int rot = (int) ((Math.round(snapRotation  / 90) % 4 + 4) % 4);
            double firstSnap = rot * 90;

            if (nameRotation == MapConfiguration.NameSettings.NameRotation.FIX) {
                firstSnap = 0;
                rot = 0;
            } else if (nameRotation == MapConfiguration.NameSettings.NameRotation.ROTATE) {
                firstSnap = snapRotation;
            }

            int offX = 0, offY = 0, width = 0;
            if (nameRotation != MapConfiguration.NameSettings.NameRotation.ROTATE) {
                Rectangle fit = maxFit(rot, dungeonRoom.getRoomBounds().getShape());
                if ((fit.height - fit.width) * (rot % 2 == 0 ? 1 : -1) > 0 && nameRotation == MapConfiguration.NameSettings.NameRotation.SNAP_LONG) {
                    if (fit.height - fit.width > 0) {
                        rot = (((int) (snapRotation / 90) % 4 + 4) % 4);
                        if (rot < 2) {
                            firstSnap = 90;
                        } else {
                            firstSnap = 270;
                        }
                    } else {
                        rot = (((int) (snapRotation / 90) % 4 + 4) % 4);
                        if (rot == 0 || rot == 3) {
                            firstSnap = 0;
                        } else {
                            firstSnap = 180;
                        }
                    }
                    rot = (int) ((firstSnap) / 90 % 4);
                    rot = rot % 4;
                    fit = maxFit(rot, dungeonRoom.getRoomBounds().getShape());
                }
                Point mapPt = layout.roomPointToMapPoint(dungeonRoom.getMinRoomPt());

                if (rot == 0) {
                    offX = fit.x * unitRoomBigWidth + pad;
                    offY = fit.y * unitRoomBigHeight + pad;
                    width = fit.width;
                } else if (rot == 1) {
                    offX = (fit.x + fit.width) * unitRoomBigWidth - gap - pad;
                    offY = (fit.y) * unitRoomBigHeight + pad;
                    width = fit.height;
                } else if (rot == 2) {
                    offX = (fit.x + fit.width) * unitRoomBigWidth - gap - pad;
                    offY = (fit.y + fit.height) * unitRoomBigHeight - gap - pad;
                    width = fit.width;
                } else if (rot == 3) {
                    offX = (fit.x) * unitRoomBigWidth + pad;
                    offY = (fit.y + fit.height) * unitRoomBigHeight - gap - pad;
                    width = fit.height;
                }
                offX += mapPt.x;
                offY += mapPt.y;
            } else {
                Point mapPt = layout.roomPointToMapPoint(dungeonRoom.getMinRoomPt());
                Rectangle fit = maxFit(0, dungeonRoom.getRoomBounds().getShape());
                offX = mapPt.x + (fit.width * unitRoomBigWidth - gap) / 2;
                offY = mapPt.y + (fit.height * unitRoomBigHeight - gap) / 2;
                width = fit.width;
            }

            ctx.pose().pushMatrix();
            ctx.pose().translate((float) offX, (float) offY);
            ctx.pose().scale((float) (1/scale), (float) (1/scale));
            double size = mapConfiguration.getNameSettings().getSize();
            ctx.pose().scale((float) size, (float) size);
            ctx.pose().rotate((float) Math.toRadians(firstSnap));
            int renderWidth = (int) ((width * unitRoomBigWidth - gap - 2 *pad) * scale / size);

            if (renderWidth < 10) {
                renderWidth = 10;
            }

            boolean drawNameSetting = override != null ? override.isDrawName() : mapConfiguration.getNameSettings().isDrawName();

            if (drawNameSetting && dungeonRoomInfo != null) {
                String name = override != null && !override.getNameOverride().isEmpty() ? override.getNameOverride() : dungeonRoomInfo.getName();
                if (dungeonRoomInfo.isRegistered()) {
                    if (nameRotation == MapConfiguration.NameSettings.NameRotation.ROTATE) {
                        ctx.drawString(name, -Minecraft.getInstance().font.width(name) / 2,
                                -Minecraft.getInstance().font.lineHeight / 2,
                                RenderUtils.getColorAt(0, 0, mapConfiguration.getNameSettings().getTextColor()), false);
                    } else {
                        ctx.drawSplitString(name, 0, 0, renderWidth,
                                RenderUtils.getColorAt(0, 0, mapConfiguration.getNameSettings().getTextColor()));
                    }
                }
            }
            ctx.pose().popMatrix();
        }

        for (Point point : scaffoldParser.getPotential()) {
            Point mapPt = layout.roomPointToMapPoint(point);
            int offX = mapPt.x + unitRoomWidth / 2;
            int offY = mapPt.y + unitRoomHeight / 2;
            ctx.pose().pushMatrix();
            ctx.pose().translate((float) offX, (float) offY);
            ctx.pose().scale((float) (1/scale), (float) (1/scale));
            ctx.pose().scale((float) chkmark, (float) chkmark);
            ctx.pose().rotate((float) Math.toRadians(snapRotation));

            ctx.drawScaledCustomSizeModalRect(resourceLocation,
                    -8, -8, 128 - 16, 64, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
            );
            ctx.pose().popMatrix();
        }
        for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
            DungeonRoomInfo dungeonRoomInfo = dungeonRoom.getDungeonRoomInfo();
            MapConfiguration.RoomOverride override = dungeonRoomInfo == null ? null : mapConfiguration.getRoomOverrides().get(dungeonRoomInfo.getUuid());
//            MapConfiguration.RoomOverride override = mapConfiguration.getRoomOverrides().values().stream().findFirst().orElse(null);


            Point pt = dungeonRoom.getMinRoomPt();
            Point mapPt = layout.roomPointToMapPoint(pt);
            int offX = mapPt.x + unitRoomWidth / 2;
            int offY = mapPt.y + unitRoomHeight / 2;
            if (mapConfiguration.getCheckmarkSettings().isCenter()) {
                Rectangle fit = maxFit(0, dungeonRoom.getRoomBounds().getShape());
                offX = mapPt.x + fit.x * unitRoomBigWidth + (fit.width * unitRoomBigWidth - gap) / 2;
                offY = mapPt.y + fit.y * unitRoomBigHeight + (fit.height * unitRoomBigHeight - gap) / 2;
//                System.out.println(dungeonRoom.getShape() + " / "+ fit);
            }

            ctx.pose().pushMatrix();
            ctx.pose().translate((float) offX, (float) offY);
            ctx.pose().scale((float) (1/scale), (float) (1/scale));

            ctx.pose().scale((float) chkmark, (float) chkmark);


            double iconRotation;
            MapConfiguration.RoomInfoSettings.IconRotation eiconrotation = override != null ? override.getIconRotation() : mapConfiguration.getCheckmarkSettings().getIconRotation();
            if (eiconrotation == MapConfiguration.RoomInfoSettings.IconRotation.ROTATE) {
                iconRotation = snapRotation;
            } else if (eiconrotation == MapConfiguration.RoomInfoSettings.IconRotation.SNAP){
                iconRotation = snapRotation - ((snapRotation - 45) % 90 - 90) % 90 - 45;
            } else {
                iconRotation = 0;
            }

            ctx.pose().rotate((float) Math.toRadians(iconRotation));

            int u = 0;
            int v = 0;
            if (override == null || override.getIconLocation().isEmpty()) {
                switch (dungeonRoom.getCurrentState()) {
                    case FINISHED:
                        u = 256 - 16;
                        v = 16;
                        break;
                    case FAILED:
                        u = 256 - 16;
                        v = 0;
                        break;
                    case COMPLETE_WITHOUT_SECRETS:
                        u = 256 - 16;
                        v = 48;
                        break;
                    case DISCOVERED:
                        u = 256 - 16;
                        v = 32;
                        break;
                }
            } else {
                switch (dungeonRoom.getCurrentState()) {
                    case FINISHED:
                        u = 0;
                        v = 16;
                        break;
                    case FAILED:
                        u = 0;
                        v = 0;
                        break;
                    case COMPLETE_WITHOUT_SECRETS:
                        u = 16;
                        v = 16;
                        break;
                    case DISCOVERED:
                        u = 16;
                        v = 0;
                        break;
                }
            }

            MapConfiguration.RoomInfoSettings.Style style = override != null ? override.getStyle() : mapConfiguration.getCheckmarkSettings().getStyle();

            if (style == MapConfiguration.RoomInfoSettings.Style.CHECKMARK_AND_COUNT && dungeonRoom.getTotalSecrets() != 0 &&
                    (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.COMPLETE_WITHOUT_SECRETS || dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED)
                    || style == MapConfiguration.RoomInfoSettings.Style.SECRET_COUNT) {
                String toDraw;
                if (dungeonRoom.getDungeonRoomInfo() != null) {
                    int cnt = 0;
                    for (DungeonMechanicState value : dungeonRoom.getMechanics().values()) {
                        if (value instanceof ISecret) {
                            if (((ISecret) value).isFound(dungeonRoom)) {
                                cnt += 1;
                            }
                        }
                    }
                    toDraw = cnt + "/" + dungeonRoom.getTotalSecrets();
                } else {
                    toDraw = "?/"+(dungeonRoom.getTotalSecrets() == -1 ? "?" : dungeonRoom.getTotalSecrets());
                }

                int color = 0xFFFFFFFF;
                if (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.FAILED)
                    color = 0xFFFF0000;
                else if (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.FINISHED)
                    color = 0xFF00FF00;
                else if (dungeonRoom.getCurrentState() == DungeonRoom.RoomState.DISCOVERED)
                    color = 0xFF777777;

                ctx.drawString(
                        toDraw, -Minecraft.getInstance().font.width(toDraw)/2, -4, color, true
                );
            } else {
                if (override == null || override.getIconLocation().isEmpty()) {
                    ctx.drawScaledCustomSizeModalRect(resourceLocation,
                            -8, -8, u, v, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
                    );
                } else {
                    ctx.drawScaledCustomSizeModalRect(Identifier.parse(override.getIconLocation()),
                            -8, -8, u, v, 16, 16, unitRoomWidth, unitRoomHeight, 32, 32
                    );
                }
            }

            ctx.pose().popMatrix();
        }
    }


    private void renderRooms(RenderingContext ctx, DungeonRoomScaffoldParser scaffoldParser) {

        DungeonMapLayout layout = scaffoldParser.getDungeonMapLayout();

        int unitRoomBigWidth = layout.getUnitRoomSize().width + layout.getMapRoomGap();
        int unitRoomBigHeight = layout.getUnitRoomSize().height + layout.getMapRoomGap();
        int unitRoomWidth = layout.getUnitRoomSize().width;
        int unitRoomHeight = layout.getUnitRoomSize().height;
        int gap = layout.getMapRoomGap();


        for (Point point : scaffoldParser.getPotential()) {
            Point mapPt = layout.roomPointToMapPoint(point);
            int offX = mapPt.x;
            int offY = mapPt.y;

            ctx.drawScaledCustomSizeModalRect(resourceLocation,
                    offX, offY, 24, 24*3, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
            );
        }
        for (DungeonRoom dungeonRoom : scaffoldParser.getDungeonRoomList()) {
            DungeonRoomInfo dungeonRoomInfo = dungeonRoom.getDungeonRoomInfo();
            MapConfiguration.RoomOverride override = dungeonRoomInfo == null ? null : mapConfiguration.getRoomOverrides().get(dungeonRoomInfo.getUuid());
//            MapConfiguration.RoomOverride override = mapConfiguration.getRoomOverrides().values().stream().findFirst().orElse(null);

            int offsetX = 0;
            int offsetY = 0;

            if (dungeonRoom.getColor() == 63) {
                // normal
//                offsetY = 24;
            } else if (dungeonRoom.getColor() == 18) {
                // blood
                offsetY = 24 * 1;
            } else if (dungeonRoom.getColor() == 66) {
                // puzzle
                offsetY = 24 * 2;
            } else if (dungeonRoom.getColor() == 30)  {
                // entrance
                offsetY = 24 * 3;
            } else if (dungeonRoom.getColor() == 82) {
                // fairy
                offsetX = 24;
            } else if (dungeonRoom.getColor() == 62) {
                // trap
                offsetX = 24;
                offsetY = 24;
            } else if (dungeonRoom.getColor() == 74) {
                // miniboss
                offsetX = 24;
                offsetY = 24 * 2;
            } else {
                offsetX = 24;
                offsetY = 24 * 3;
            }
            Point mapPt = layout.roomPointToMapPoint(dungeonRoom.getMinRoomPt());
            if (override != null && !override.getTextureLocation().isEmpty() && dungeonRoomInfo != null) {
                int rotation = dungeonRoom.getRoomMatcher().getRotation();
                short shape = dungeonRoomInfo.getShape();

                int[] patterns = new int[]{
                        0xF, 0xF0, 0x0F00, 0xF000,
                        0x1111, 0x2222, 0x4444, 0x8888
                };
                int maxWidth = 0, maxHeight = 0;

                for (int i = 0; i < 4; i++) {
                    int cnts = Integer.bitCount(patterns[i] & shape);
                    if (cnts > maxWidth) maxWidth = cnts;
                }
                for (int i = 0; i < 4; i++) {
                    int cnts = Integer.bitCount(patterns[i + 4] & shape);
                    if (cnts > maxHeight) maxHeight = cnts;
                }

                int widthPixels = maxWidth * unitRoomBigWidth - gap;
                int heightPixels = maxHeight * unitRoomBigHeight - gap;
                int widthTexturePixels = maxWidth * 20 - 4;
                int heightTexturePixels = maxHeight * 20 - 4;
                int rWidthPixels = dungeonRoom.getRoomBounds().getUnitLenX() * unitRoomBigWidth - gap;
                int rHeightPixels = dungeonRoom.getRoomBounds().getUnitLenZ() * unitRoomBigHeight - gap;

                ctx.pose().pushMatrix();
                ctx.pose().translate((float) (mapPt.x + rWidthPixels / 2.0), (float) (mapPt.y + rHeightPixels / 2.0));
                ctx.pose().rotate((float) Math.toRadians(-rotation * 90));
                ctx.drawScaledCustomSizeModalRect(Identifier.parse(override.getTextureLocation()),
                        -widthPixels / 2.0, -heightPixels / 2.0, 0, 0, widthTexturePixels, heightTexturePixels, widthPixels, heightPixels, 128, 128
                );
                ctx.pose().popMatrix();
            } else {
                for (int y = 0; y < 4; y++) {
                    for (int x = 0; x < 4; x++) {
                        boolean isIn = ((dungeonRoom.getRoomBounds().getShape() >> ((y * 4) + x)) & 0x1) > 0;
                        boolean isRightIn = ((dungeonRoom.getRoomBounds().getShape() >> ((y * 4) + x +1)) & 0x1) > 0 && x < 3;
                        boolean isBottomIn = ((dungeonRoom.getRoomBounds().getShape() >> ((y * 4) + x + 4)) & 0x1) > 0 && y < 3;
                        boolean isBottomRightIn = ((dungeonRoom.getRoomBounds().getShape() >> ((y * 4) + x  + 5)) & 0x1) > 0 && y < 3 && x < 3;

                        int offX = mapPt.x + x * unitRoomBigWidth;
                        int offY = mapPt.y + y * unitRoomBigHeight;

                        if (isIn) {
                            ctx.drawScaledCustomSizeModalRect(resourceLocation,
                                    offX, offY, offsetX, offsetY, 16, 16, unitRoomWidth, unitRoomHeight, 128, 128
                            );
                            if (isRightIn) {
                                ctx.drawScaledCustomSizeModalRect(resourceLocation,
                                        offX+unitRoomWidth, offY, offsetX+16, offsetY, 4, 16, gap, unitRoomHeight, 128, 128
                                );
                            }
                            if (isBottomIn) {
                                ctx.drawScaledCustomSizeModalRect(resourceLocation,
                                        offX, offY+unitRoomHeight, offsetX, offsetY + 16, 16, 4, unitRoomWidth, gap, 128, 128
                                );
                            }
                            if (isBottomRightIn && isRightIn && isBottomIn) {
                                ctx.drawScaledCustomSizeModalRect(resourceLocation,
                                        offX+unitRoomWidth, offY+unitRoomHeight, offsetX+16, offsetY+16, 4, 4, gap, gap, 128, 128
                                );
                            }
                        }
                    }
                }
            }

            for (Tuple<Vector2d, EDungeonDoorType> doorsAndState : dungeonRoom.getDoorsAndStates()) {
                double x = doorsAndState.getA().x;
                double y = doorsAndState.getA().y;
                if (doorsAndState.getB() == EDungeonDoorType.NONE) continue;

                int offsetXX = offsetX;
                int offsetYY = offsetY;

                if (doorsAndState.getB() == EDungeonDoorType.WITHER) {
                    offsetXX = 48;
                    offsetYY = 24;
                }

                if (doorsAndState.getB() == EDungeonDoorType.BLOOD) {
                    offsetXX = 48;
                    offsetYY = 0;
                }

                if (doorsAndState.getB() == EDungeonDoorType.UNOPEN) {
                    offsetXX = 24;
                    offsetYY = 24 * 3;
                }

                if (x % 1 != 0) {
                    ctx.drawScaledCustomSizeModalRect(resourceLocation,
                            mapPt.x + (int) (Math.ceil(x) * unitRoomBigWidth) - gap, mapPt.y + (int) (Math.ceil(y) * unitRoomBigHeight), offsetXX + 20, offsetYY, 4, 16, gap, unitRoomHeight, 128, 128
                    );
                } else {
                    ctx.drawScaledCustomSizeModalRect(resourceLocation,
                            mapPt.x + (int) (Math.ceil(x) * unitRoomBigWidth), mapPt.y +(int) (Math.ceil(y) * unitRoomBigHeight) - gap, offsetXX, offsetYY + 20, 16, 4, unitRoomWidth, gap, 128, 128
                    );
                }
            }

        }
    }

//    long nextRefresh;
//
//    Set<TabListEntry> playerListCached;
//
//    public Set<TabListEntry> getPlayerListCached(){
//        if(playerListCached == null || nextRefresh <= System.currentTimeMillis()){
//            ChatTransmitter.sendDebugChat("Refreshing players on map");
//            playerListCached = TabList.INSTANCE.getTabListEntries();
//            nextRefresh = System.currentTimeMillis() + 10000;
//        }
//        return playerListCached;
//    }

}
