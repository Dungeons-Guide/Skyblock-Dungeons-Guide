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

package kr.syeyoung.dungeonsguide.mod.features;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.config.types.GUIPosition;
import kr.syeyoung.dungeonsguide.mod.config.types.TypeConverterRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class AbstractHUDFeature extends AbstractGuiFeature {
    private GUIPosition featureRect;

    public void setFeatureRect(GUIPosition featureRect) {
        if (requiresWidthBound() && featureRect.getWidth() == null) featureRect.setWidth(100.0);
        if (requiresHeightBound() && featureRect.getHeight() == null) featureRect.setHeight(100.0);
        this.featureRect = featureRect;
        updatePosition();
    }

    public void setWidth(double width) {
        if (!requiresWidthBound()) throw new UnsupportedOperationException("Width unsettable");
        if (width < 10) width = 10;
        featureRect.setWidth(width);
        updatePosition();
    }
    public void setHeight(double height) {
        if (!requiresHeightBound() && (getKeepRatio() == null)) throw new UnsupportedOperationException("Height unsettable");
        if (height < 10) height = 10;
        if (getKeepRatio() != null)
            featureRect.setWidth(height / getKeepRatio());
        else
            featureRect.setHeight(height);
        updatePosition();
    }

    protected AbstractHUDFeature(String category, String name, String description, String key) {
        super(category, name, description, key);
        this.featureRect = new GUIPosition(GUIPosition.OffsetType.START, 0, GUIPosition.OffsetType.START, 0,
                requiresWidthBound() ? 0.0 : null,
                requiresHeightBound() ? 0.0 : null);
    }



    public boolean requiresWidthBound() {return false;}
    public boolean requiresHeightBound() {return false;}
    public Double getKeepRatio() {return null;}


    public abstract void drawDemo(float partialTicks);

    public class WidgetFeatureWrapper extends Widget implements Renderer, Layouter {
        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
            drawDemo(partialTicks);
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
        }
    }

    public Widget instantiateDemoWidget() {
        return new WidgetFeatureWrapper();
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        GUIPosition position = TypeConverterRegistry.getTypeConverter("guipos", GUIPosition.class).deserialize(jsonObject.get("$pos"));
        if (position != null) setFeatureRect(position);
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject object = super.saveConfig();
        object.add("$pos", TypeConverterRegistry.getTypeConverter("guipos", GUIPosition.class).serialize(featureRect));
        return object;
    }

    public List<Widget> getTooltipForEditor() {
        ArrayList<Widget> mPanels = new ArrayList<>();
        mPanels.add(new Text(getName(), 0xFFFFFFFF, Text.TextAlign.CENTER, Text.WordBreak.WORD, 1.0, 8.0));
//        mPanels.add(new MButton() {
//            {
//                setText("Edit");
//                setOnActionPerformed(() -> {
//                    GuiScreen guiScreen = guiGuiLocationConfig.getBefore();
//                    if (guiScreen == null) {
//                        guiScreen = new GuiConfigV2();
//                    }
//                    Minecraft.getMinecraft().displayGuiScreen(guiScreen);
//                    if (guiScreen instanceof GuiConfigV2) {
//                        ((GuiConfigV2) guiScreen).getRootConfigPanel().setCurrentPageAndPushHistory(getEditRoute(((GuiConfigV2) guiScreen).getRootConfigPanel()));
//                    }
//                });
//            }
//
//            @Override
//            public Dimension getPreferredSize() {
//                return new Dimension(100,20);
//            }
//        });
//        mPanels.add(new MPassiveLabelAndElement("Enabled", new MToggleButton() {{
//            setEnabled(AbstractHUDFeature.this.isEnabled());
//            setOnToggle(() ->{
//                AbstractHUDFeature.this.setEnabled(isEnabled());
//            }); }
//        }));
        return mPanels;
    }
}
