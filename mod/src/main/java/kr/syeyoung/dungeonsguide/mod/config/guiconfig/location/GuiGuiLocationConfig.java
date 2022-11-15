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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig.location;

import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.GuiFeature;
import kr.syeyoung.dungeonsguide.mod.gui.MGui;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GuiGuiLocationConfig extends MGui {

    @Getter
    private final GuiScreen before;

    @Getter
    private TreeMap<Integer, List<Marker>> markerTreeMapByX = new TreeMap<>();
    @Getter
    private TreeMap<Integer, List<Marker>> markerTreeMapByY = new TreeMap<>();
    @Getter
    private Set<Marker> markerSet = new HashSet<>();


    Marker[] markers = new Marker[4];


    public GuiGuiLocationConfig(final GuiScreen before, AbstractFeature featureWhitelist) {
        this.before = before;
        for (AbstractFeature feature : FeatureRegistry.getFeatureList()) {
            if (feature instanceof GuiFeature && feature.isEnabled()) {
                getMainPanel().add(new PanelDelegate((GuiFeature) feature, featureWhitelist == null || feature == featureWhitelist, this));
            }
        }

        getMainPanel().setBackgroundColor(new Color(0,0,0, 100));
    }

    public static final Vec3[] facing = new Vec3[] {
            new Vec3(0, 0.5, 2),
            new Vec3(0.5, 0, 1),
            new Vec3(0.5, 1, 3),
            new Vec3(1, 0.5, 4),
    };

    public void removeAndAddMarker(Marker prev, Marker newM) {
        if (prev != null) {
            markerTreeMapByX.computeIfPresent(prev.getX(),(k,v) -> {
                v.remove(prev);
                if (v.isEmpty()) return null;
                else return v;
            });
            markerTreeMapByY.computeIfPresent(prev.getY(),(k,v) -> {
                v.remove(prev);
                if (v.isEmpty()) return null;
                else return v;
            });
            markerSet.remove(prev);
        }
        if (newM != null) {
            markerTreeMapByX.compute(newM.getX(), (k,v) -> {
                if (v == null) {
                    return new ArrayList<>(Arrays.asList(newM));
                } else {
                    v.add(newM);
                    return v;
                }
            });
            markerTreeMapByY.compute(newM.getY(), (k,v) -> {
                if (v == null) {
                    return new ArrayList<>(Arrays.asList(newM));
                } else {
                    v.add(newM);
                    return v;
                }
            });
            markerSet.add(newM);
        }
    }

    public void setupMarkers() {
        for (int i1 = 0; i1 < markers.length; i1++) {
            Marker orig = markers[i1];
            Vec3 pt = facing[i1];
            markers[i1] = new Marker((int) (pt.xCoord  * getMainPanel().getBounds().width), (int) (pt.yCoord  * getMainPanel().getBounds().height), (int) pt.zCoord, this);

            removeAndAddMarker(orig, markers[i1]);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.translate(scaledResolution.getScaledWidth()/2, scaledResolution.getScaledHeight()/2, 0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
        fr.drawString("Right Click On Elements to Open Popup Menu",
                -fr.getStringWidth("Right Click On Elements to Open Popup Menu")/2
                ,-fr.FONT_HEIGHT/2, -1);
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        try {
            getMainPanel().keyPressed0(typedChar, keyCode);

            if (keyCode == 1) {
                Minecraft.getMinecraft().displayGuiScreen(before);
            }
        } catch (Throwable e) {
            if (!e.getMessage().contains("hack to stop"))
                e.printStackTrace();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        getMainPanel().setBounds(new Rectangle(0,0,Minecraft.getMinecraft().displayWidth,Minecraft.getMinecraft().displayHeight));
        markerTreeMapByX.clear();
        markerTreeMapByY.clear();
        markerSet.clear();
        setupMarkers();
        for (MPanel childComponent : getMainPanel().getChildComponents()) {
            if (childComponent instanceof PanelDelegate) {
                ((PanelDelegate) childComponent).rebuildMarker();
            }
        }

    }

}
