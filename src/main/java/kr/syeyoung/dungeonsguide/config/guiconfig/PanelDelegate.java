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

package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class PanelDelegate extends MPanel {
    private final GuiFeature guiFeature;
    private boolean draggable = false;
    private GuiGuiLocationConfig guiGuiLocationConfig;

    private Set<Marker> markerSet = new HashSet<>();
    public PanelDelegate(GuiFeature guiFeature, boolean draggable, GuiGuiLocationConfig guiGuiLocationConfig) {
        this.guiFeature = guiFeature;
        this.draggable = draggable;
        this.guiGuiLocationConfig = guiGuiLocationConfig;
    }

    public void rebuildMarker() {
        internallyThinking = guiFeature.getFeatureRect().getRectangleNoScale();
        applyConstraint();
    }

    @Override
    public Rectangle getBounds() {
        Rectangle rectangle = guiFeature.getFeatureRect().getRectangle();
        return new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMouseX, int relMouseY, float partialTicks, Rectangle scissor) {

        GlStateManager.pushMatrix();
        guiFeature.drawDemo(partialTicks);
        GlStateManager.popMatrix();
        if (!draggable) return;
        Gui.drawRect(0,0, 4, 4, 0xFFBBBBBB);
        Gui.drawRect(0, getBounds().height - 4, 4, getBounds().height, 0xFFBBBBBB);
        Gui.drawRect(getBounds().width - 4,0, getBounds().width, 4, 0xFFBBBBBB);
        Gui.drawRect(getBounds().width - 4,getBounds().height - 4, getBounds().width, getBounds().height, 0xFFBBBBBB);
        if (lastAbsClip.contains(absMousex, absMousey)) {
            if (relMouseX < 4 && relMouseY < 4) {
                Gui.drawRect(0,0, 4, 4, 0x55FFFFFF);
            } else if (relMouseX < 4 && relMouseY > getBounds().height - 4) {
                Gui.drawRect(0, getBounds().height - 4, 4, getBounds().height, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 4 && relMouseY > getBounds().height - 4) {
                Gui.drawRect(getBounds().width - 4,getBounds().height - 4, getBounds().width, getBounds().height, 0x55FFFFFF);
            } else if (relMouseX > getBounds().width - 4 && relMouseY < 4) {
                Gui.drawRect(getBounds().width - 4,0, getBounds().width, 4, 0x55FFFFFF);
            } else if (selectedPart == -2){
                Gui.drawRect(0,0, getBounds().width, getBounds().height, 0x55FFFFFF);
            }
        }
        GlStateManager.enableBlend();
    }

    @Override
    public void render0(ScaledResolution resolution, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) {
        GlStateManager.pushMatrix();
        super.render0(resolution, parentPoint, parentClip, absMousex, absMousey, relMousex0, relMousey0, partialTicks);
        GlStateManager.popMatrix();

        if (snapped != null && selectedPart != -2) {
            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.disableTexture2D();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            GL11.glLineWidth(1);
            for (Tuple<Marker[], EnumFacing.Axis> markerAxisTuple : snapped) {
                if (markerAxisTuple.getSecond() == EnumFacing.Axis.X) {
                    worldRenderer.pos(markerAxisTuple.getFirst()[0].getX(), 0, 0).color(0,255,0,255).endVertex();
                    worldRenderer.pos(markerAxisTuple.getFirst()[0].getX(), Minecraft.getMinecraft().displayHeight, 0).color(0,255,0,255).endVertex();
                } else {
                    worldRenderer.pos(0, markerAxisTuple.getFirst()[0].getY(), 0).color(0,255,0,255).endVertex();
                    worldRenderer.pos(Minecraft.getMinecraft().displayWidth, markerAxisTuple.getFirst()[0].getY(), 0).color(0,255,0,255).endVertex();
                }
            }
            tessellator.draw();
            for (Marker marker : guiGuiLocationConfig.getMarkerSet()) {
                Gui.drawRect(marker.getX(),marker.getY(), marker.getX()+1, marker.getY()+1, 0xFFFF0000);
            }
        }
    }

    private int selectedPart = -2;

    private int lastX = 0;
    private int lastY = 0;

    private Rectangle internallyThinking;
    private Rectangle constraintApplied;

    private Set<Tuple<Marker[], EnumFacing.Axis>> snapped = new HashSet<>();

    public void applyConstraint() {
        constraintApplied = internallyThinking.getBounds();

        // SNAP Moving Point.
        snapped.clear();
        int scailingThreshold = 5;
        if (selectedPart == 0){
            Point snapPt = new Point(constraintApplied.x +constraintApplied.width, constraintApplied.y + constraintApplied.height);
            Optional<Marker> snapX, snapY;
            SortedMap<Integer, List<Marker>> markerSortedMap = guiGuiLocationConfig.getMarkerTreeMapByX().subMap(snapPt.x-scailingThreshold, snapPt.x +scailingThreshold);
            snapX = markerSortedMap.values().stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .filter(a -> a.getParent() != this)
                    .min(Comparator.comparingInt(a -> (int) snapPt.distanceSq(a.getX(), a.getY())));
            markerSortedMap = guiGuiLocationConfig.getMarkerTreeMapByY().subMap(snapPt.y-scailingThreshold, snapPt.y +scailingThreshold);
            snapY = markerSortedMap.values().stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .filter(a -> a.getParent() != this)
                    .min(Comparator.comparingInt(a -> (int) snapPt.distanceSq(a.getX(), a.getY())));
            snapX.ifPresent(a -> {
                snapPt.x = a.getX();
            });
            snapY.ifPresent(a -> {
                snapPt.y = a.getY();
            });

            constraintApplied = new Rectangle(constraintApplied.x, constraintApplied.y, snapPt.x - constraintApplied.x, snapPt.y - constraintApplied.y);



            int minWidth;
            int minHeight;
            if (guiFeature.isKeepRatio()) {
                if (guiFeature.getDefaultRatio() >= 1) {
                    minHeight = constraintApplied.height < 0 ? -8 : 8;
                    minWidth = (int) (guiFeature.getDefaultRatio() * minHeight);
                } else {
                    minWidth = constraintApplied.width < 0 ? -8 : 8;
                    minHeight = (int) (minWidth / guiFeature.getDefaultRatio());
                }
            } else {
                minWidth = constraintApplied.width < 0 ? -8 : 8;
                minHeight = constraintApplied.height < 0 ? -8 : 8;
            }


            constraintApplied.width = Math.abs(constraintApplied.width) > Math.abs(minWidth) ? constraintApplied.width :
                    Math.abs(internallyThinking.width) > Math.abs(minWidth) ? internallyThinking.width : minWidth;
            constraintApplied.height = Math.abs(constraintApplied.height) > Math.abs(minHeight) ? constraintApplied.height :
                    Math.abs(internallyThinking.height) > Math.abs(minHeight) ? internallyThinking.height : minHeight;

            if (guiFeature.isKeepRatio()) {
                double ratio = guiFeature.getDefaultRatio();

                int heightWhenWidthFix = (int) Math.abs(constraintApplied.width / ratio);
                int widthWhenHeightFix = (int) Math.abs(ratio * constraintApplied.height);
                if (Math.abs(heightWhenWidthFix) <= Math.abs(constraintApplied.height)) {
                    constraintApplied.height = constraintApplied.height < 0 ? -heightWhenWidthFix : heightWhenWidthFix;
                } else if (Math.abs(widthWhenHeightFix) <= Math.abs(constraintApplied.width)) {
                    constraintApplied.width =constraintApplied.width < 0 ? - widthWhenHeightFix : widthWhenHeightFix;
                }
            }


            snapX.ifPresent(a -> {
                if (snapPt.x - constraintApplied.x == constraintApplied.width) {
                    Marker m = new Marker((int) (GuiGuiLocationConfig.facing[3].xCoord * constraintApplied.width) + constraintApplied.x, (int) (GuiGuiLocationConfig.facing[3].yCoord * constraintApplied.height) + constraintApplied.y, (int) GuiGuiLocationConfig.facing[3].zCoord, this);
                    snapped.add(new Tuple<>(new Marker[]{a, m}, EnumFacing.Axis.X));
                }
            });
            snapY.ifPresent(a -> {
                if (snapPt.y - constraintApplied.y == constraintApplied.height) {
                    Marker m = new Marker((int) (GuiGuiLocationConfig.facing[2].xCoord * constraintApplied.width) + constraintApplied.x, (int) (GuiGuiLocationConfig.facing[2].yCoord * constraintApplied.height) + constraintApplied.y, (int) GuiGuiLocationConfig.facing[2].zCoord, this);
                    snapped.add(new Tuple<>(new Marker[]{a, m}, EnumFacing.Axis.Y));
                }
            });

            if (constraintApplied.height < 0) {
                constraintApplied.height = -constraintApplied.height;
                constraintApplied.y -= constraintApplied.height;
            }

            if (constraintApplied.width < 0) {
                constraintApplied.width = -constraintApplied.width;
                constraintApplied.x -= constraintApplied.width;
            }
        } else if (selectedPart == -1) {
            for (int i : Arrays.asList(0,3,1,2)) {
                Vec3 pt = GuiGuiLocationConfig.facing[i];
                Marker m = new Marker((int) (pt.xCoord * constraintApplied.width) + constraintApplied.x, (int) (pt.yCoord * constraintApplied.height) + constraintApplied.y, (int) pt.zCoord, this);
                    Optional<Marker> result = guiGuiLocationConfig.getMarkerTreeMapByX().subMap(m.getX()-scailingThreshold, m.getX() +scailingThreshold).values().stream()
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .filter(a -> a.getParent() != this)
                            .filter(a -> Math.abs(a.getX() - m.getX()) < scailingThreshold)
                            .filter(a -> ((a.getX() - pt.xCoord * constraintApplied.width) >= 0
                                && (a.getX() - pt.xCoord * constraintApplied.width + constraintApplied.width) <= Minecraft.getMinecraft().displayWidth))
                            .min(Comparator.comparingInt(a -> a.distanceSQ(m)));
                    if (result.isPresent()) {
                        int x = result.get().getX();
                        constraintApplied.x = (int) (x - pt.xCoord * constraintApplied.width);

                        snapped.add(new Tuple<>(new Marker[] {result.get(), m}, EnumFacing.Axis.X));
                        break;
                    }
            }
            for (int i : Arrays.asList(1,2,0,3)) {
                Vec3 pt = GuiGuiLocationConfig.facing[i];
                Marker m = new Marker((int) (pt.xCoord * constraintApplied.width) + constraintApplied.x, (int) (pt.yCoord * constraintApplied.height) + constraintApplied.y, (int) pt.zCoord, this);
                    Optional<Marker> result = guiGuiLocationConfig.getMarkerTreeMapByY().subMap(m.getY()-scailingThreshold, m.getY() +scailingThreshold).values().stream()
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .filter(a -> a.getParent() != this)
                            .filter(a -> Math.abs(a.getY() - m.getY()) < scailingThreshold)
                            .filter(a -> ((a.getY() - pt.yCoord * constraintApplied.height) >= 0
                                    && (a.getY() - pt.yCoord * constraintApplied.height+ constraintApplied.height) <= Minecraft.getMinecraft().displayHeight))
                            .min(Comparator.comparingInt(a -> a.distanceSQ(m)));
                    if (result.isPresent()) {
                        int y = result.get().getY();
                        constraintApplied.y = (int) (y - pt.yCoord * constraintApplied.height);
                        snapped.add(new Tuple<>(new Marker[] {result.get(), m}, EnumFacing.Axis.Y));
                        break;
                    }
            }
        }

        if (constraintApplied.x < 0) constraintApplied.x = 0;
        if (constraintApplied.y < 0) constraintApplied.y = 0;
        if (constraintApplied.x + constraintApplied.width + 1 >=Minecraft.getMinecraft().displayWidth) constraintApplied.x = Minecraft.getMinecraft().displayWidth - constraintApplied.width - 1;
        if (constraintApplied.y + constraintApplied.height  + 1>= Minecraft.getMinecraft().displayHeight) constraintApplied.y = Minecraft.getMinecraft().displayHeight - constraintApplied.height - 1;


        setupMarkers();
    }

    Marker[] markers = new Marker[4];
    public void setupMarkers() {
        for (int i1 = 0; i1 < markers.length; i1++) {
            Marker orig = markers[i1];

            Vec3 pt = GuiGuiLocationConfig.facing[i1];
            markers[i1] = new Marker((int) (pt.xCoord * constraintApplied.width) + constraintApplied.x, (int) (pt.yCoord * constraintApplied.height) + constraintApplied.y, (int) pt.zCoord, this);

            guiGuiLocationConfig.removeAndAddMarker(orig, markers[i1]);
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        if (!draggable) return;
        if (!lastAbsClip.contains(absMouseX, absMouseY)) return;
        if (mouseButton == 0) {
            internallyThinking = guiFeature.getFeatureRect().getRectangleNoScale();
            if (relMouseX < 4 && relMouseY < 4) { // TL
                selectedPart = 0;
                internallyThinking.y += internallyThinking.height;
                internallyThinking.height = -internallyThinking.height;
                internallyThinking.x += internallyThinking.width;
                internallyThinking.width = -internallyThinking.width;
            } else if (relMouseX < 4 && relMouseY > getBounds().height - 4) { // BL
                selectedPart = 0;
                internallyThinking.x += internallyThinking.width;
                internallyThinking.width = -internallyThinking.width;
            } else if (relMouseX > getBounds().width - 4 && relMouseY > getBounds().height - 4) { // BR
                selectedPart = 0;
            } else if (relMouseX > getBounds().width - 4 && relMouseY < 4) { // TR
                selectedPart = 0;
                internallyThinking.y += internallyThinking.height;
                internallyThinking.height = -internallyThinking.height;
            } else {
                selectedPart = -1;
            }
            lastX = absMouseX;
            lastY = absMouseY;
            applyConstraint();

        }
        throw new IllegalArgumentException("bruh, a hack to stop event progress");
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
        if (!draggable) return;
        if (selectedPart >= -1) {
            guiFeature.setFeatureRect(new GUIRectangle(constraintApplied));
        }

        selectedPart = -2;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!draggable) return;
        int dx = (absMouseX - lastX);
        int dy = (absMouseY - lastY);
        if (selectedPart >= 0) {
            Rectangle rectangle = internallyThinking;

            int prevWidth = rectangle.width;
            int prevHeight= rectangle.height;

            rectangle.width = prevWidth + dx;
            rectangle.height = prevHeight + dy;

            if (rectangle.height * prevHeight <= 0 && prevHeight != rectangle.height) {
                rectangle.height += prevHeight < 0 ? 4 : -4;
            }
            if (rectangle.width * prevWidth <= 0 && prevWidth != rectangle.width) {
                rectangle.width += prevWidth < 0 ? 4 : -4;
            }


            applyConstraint();
            guiFeature.setFeatureRect(new GUIRectangle(constraintApplied));
            lastX = absMouseX;
            lastY = absMouseY;
            throw new IllegalArgumentException("bruh, a hack to stop event progress");
        } else if (selectedPart == -1){
            Rectangle rectangle = internallyThinking;
            rectangle.translate(dx, dy);
            applyConstraint();
            guiFeature.setFeatureRect(new GUIRectangle(constraintApplied));
            lastX = absMouseX;
            lastY = absMouseY;
        }
    }
}
