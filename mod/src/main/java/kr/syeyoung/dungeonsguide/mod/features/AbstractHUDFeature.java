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
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.MainConfigWidget;
import kr.syeyoung.dungeonsguide.mod.config.types.GUIPosition;
import kr.syeyoung.dungeonsguide.mod.config.types.TCGUIPosition;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

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

    public double minWidth() {return 10;}

    public double minHeight() {return 10;}
    public void setWidth(double width) {
        if (!requiresWidthBound()) throw new UnsupportedOperationException("Width unsettable");
        if (width < minWidth()) width = minWidth();
        featureRect.setWidth(width);
        updatePosition();
    }
    public void setHeight(double height) {
        if (!requiresHeightBound() && (getKeepRatio() == null)) throw new UnsupportedOperationException("Height unsettable");
        if (height < minHeight()) height = minHeight();
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


    public abstract Widget instantiateDemoWidget();

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        GUIPosition position = TCGUIPosition.INSTANCE.deserialize(jsonObject.get("$pos"));
        if (position != null) setFeatureRect(position);
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject object = super.saveConfig();
        object.add("$pos", TCGUIPosition.INSTANCE.serialize(featureRect));
        return object;
    }

    public void getTooltipForEditor(List<Widget> widgets) {
        widgets.add(new Text(getName(), 0xFFFFFFFF, Text.TextAlign.CENTER, Text.WordBreak.WORD, 1.0, 8.0));
        widgets.add(new QuickEnable(this));
        if (getConfigureWidget() != null)
            widgets.add(new QuickConfigure(this));
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
    }

    public static class QuickEnable extends AnnotatedImportOnlyWidget {
        @Bind(
                variableName = "enabled"
        )
        public final BindableAttribute<Boolean> enabled = new BindableAttribute<Boolean>(Boolean.class);
        public QuickEnable(AbstractHUDFeature abstractHUDFeature) {
            super(new ResourceLocation("dungeonsguide:gui/config/popup/quickEnable.gui"));
            enabled.setValue(abstractHUDFeature.isEnabled());
            enabled.addOnUpdate((old, neu) -> {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                abstractHUDFeature.setEnabled(neu);
            });
        }
    }
    public static class QuickConfigure extends AnnotatedImportOnlyWidget {
        private AbstractHUDFeature abstractHUDFeature;
        public QuickConfigure(AbstractHUDFeature abstractHUDFeature) {
            super(new ResourceLocation("dungeonsguide:gui/config/popup/quickEdit.gui"));
            this.abstractHUDFeature = abstractHUDFeature;
        }

        @On(functionName = "configure")
        public void configure() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            MainConfigWidget mainConfigWidget = new MainConfigWidget();
            GuiScreenAdapter adapter = new GuiScreenAdapter(new GlobalHUDScale(mainConfigWidget), Minecraft.getMinecraft().currentScreen);
            Minecraft.getMinecraft().displayGuiScreen(adapter);

            Navigator.getNavigator(mainConfigWidget.getDomElement()).openPage(
                    abstractHUDFeature.getConfigureWidget()
            );
        }
    }
}
