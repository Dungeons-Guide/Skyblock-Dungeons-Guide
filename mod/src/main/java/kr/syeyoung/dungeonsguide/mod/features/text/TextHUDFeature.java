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

package kr.syeyoung.dungeonsguide.mod.features.text;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.MarkerProvider;
import kr.syeyoung.dungeonsguide.mod.config.types.*;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.CompatLayer;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.BreakWord;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.RichText;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ParentDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.overlay.GUIRectPositioner;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.*;

public abstract class TextHUDFeature extends AbstractHUDFeature implements StyledTextProvider {
    protected TextHUDFeature(String category, String name, String description, String key) {
        super(category, name, description, key);
        addParameter("textStylesNEW", new FeatureParameter<List<TextStyle>>("textStylesNEW", "", "", new ArrayList<TextStyle>(), TCTextStyleList.INSTANCE)
                .setWidgetGenerator((param) -> new CompatLayer(new PanelTextParameterConfig(TextHUDFeature.this))));
        addParameter("alignment", new FeatureParameter<RichText.TextAlign>("alignment", "Alignment", "Alignment", RichText.TextAlign.LEFT, new TCEnum<>(RichText.TextAlign.values()), richText::setAlign));
        addParameter("scale", new FeatureParameter<Double>("scale", "Scale", "Scale", 1.0, TCDouble.INSTANCE)
                .setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0.1, Double.POSITIVE_INFINITY))));
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && isHUDViewable();
    }

    private final RichText richText = new RichText(new TextSpan(
            ParentDelegatingTextStyle.ofDefault(),
            ""
    ), BreakWord.WORD, false, RichText.TextAlign.LEFT);

    @Override
    public OverlayWidget instantiateWidget() {
        return new OverlayWidget(richText, OverlayType.UNDER_CHAT, new GUIRectPositioner(this::getFeatureRect));
    }

    private Map<String, ParentDelegatingTextStyle> builtTextStyles = new HashMap<>();

    @DGEventHandler
    public void onTick0(DGTickEvent dgTickEvent) {
        try {
            checkVisibility();
            if (isHUDViewable()) {
                List<StyledText> asd = getText();

                ParentDelegatingTextStyle defaultStyle = ParentDelegatingTextStyle.ofDefault();
                defaultStyle.setSize((double) (this.<Double>getParameter("scale").getValue() * 8));

                TextSpan span = new TextSpan(defaultStyle, "");

                for (StyledText styledText : asd) {
                    TextStyle style = getStylesMap().get(styledText.getGroup());
                    TextSpan textSpan = new TextSpan(style.getLinked(), styledText.getText());
                    span.addChild(textSpan);
                }
                richText.setRootSpan(span);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FontRenderer getFontRenderer() {
        return Minecraft.getMinecraft().fontRendererObj;
    }

    @RequiredArgsConstructor
    public static class TextHUDDemo extends Widget implements MarkerProvider {
        public final TextHUDFeature hudFeature;
        @Override
        public List<Position> getMarkers() {
            RichText.TextAlign change = hudFeature.<RichText.TextAlign>getParameter("alignment").getValue();
            Rect relBound = getDomElement().getRelativeBound();
            if (change == RichText.TextAlign.LEFT) {
                return Arrays.asList(
                        new Position(0, 0),
                        new Position(0, relBound.getHeight())
                );
            } else if (change == RichText.TextAlign.CENTER) {
                return Arrays.asList(
                        new Position(relBound.getWidth() /2, 0),
                        new Position(relBound.getWidth() /2, relBound.getHeight())
                );
            } else if (change == RichText.TextAlign.RIGHT) {
                return Arrays.asList(
                        new Position(relBound.getWidth(), 0),
                        new Position(relBound.getWidth(), relBound.getHeight())
                );
            }
            return null;
        }

        @Override
        public List<Widget> build(DomElement buildContext) {
            List<StyledText> asd = hudFeature.getDummyText();

            RichText richText = new RichText(new TextSpan(
                    ParentDelegatingTextStyle.ofDefault(),
                    ""
            ), BreakWord.WORD, false, RichText.TextAlign.LEFT);
            richText.setAlign(
                    hudFeature.<RichText.TextAlign>getParameter("alignment").getValue()
            );

            ParentDelegatingTextStyle defaultStyle = ParentDelegatingTextStyle.ofDefault();
            defaultStyle.setSize((double) (hudFeature.<Double>getParameter("scale").getValue() * 8));

            TextSpan span = new TextSpan(defaultStyle, "");

            for (StyledText styledText : asd) {
                TextStyle style = hudFeature.getStylesMap().get(styledText.getGroup());
                TextSpan textSpan = new TextSpan(style.getLinked(), styledText.getText());
                span.addChild(textSpan);
            }
            richText.setRootSpan(span);

            return Collections.singletonList(richText);
        }
    }

    @Override
    public Widget instantiateDemoWidget() {
        return new TextHUDDemo(this);
    }

    public int countLines(List<StyledText> texts) {
        StringBuilder things = new StringBuilder();
        for (StyledText text : texts) {
            things.append(text.getText());
        }
        String things2 = things.toString().trim();
        int lines = 1;
        for (char c : things2.toCharArray()) {
            if (c == '\n') lines++;
        }
        return  lines;
    }

    public abstract boolean isHUDViewable();

    public abstract List<String> getUsedTextStyle();
    public List<StyledText> getDummyText() {
        return getText();
    }
    public abstract List<StyledText> getText();

    public List<TextStyle> getStyles() {
        return this.<List<TextStyle>>getParameter("textStylesNEW").getValue();
    }


    private Map<String, TextStyle> stylesMap;
    public Map<String, TextStyle> getStylesMap() {
        if (stylesMap == null) {
            List<TextStyle> styles = getStyles();
            Map<String, TextStyle> res = new HashMap<String, TextStyle>();
            for (TextStyle ts : styles) {
                res.put(ts.getGroupName(), ts);
            }
            for (String str : getUsedTextStyle()) {
                if (!res.containsKey(str))
                    res.put(str, new TextStyle(str, new AColor(0xffffffff, true), new AColor(0x00777777, true), false, new ParentDelegatingTextStyle()));
            }
            stylesMap = res;
        }
        return stylesMap;
    }


    @Override
    public void getTooltipForEditor(List<Widget> widgets) {
        super.getTooltipForEditor(widgets);
//        StyledTextRenderer.Alignment alignment = StyledTextRenderer.Alignment.valueOf(this.<String>getParameter("alignment").getValue());
//        MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(Arrays.asList("LEFT", "CENTER", "RIGHT"), alignment.name());
//        mStringSelectionButton.setOnUpdate(() -> {
//            TextHUDFeature.this.<String>getParameter("alignment").setValue(mStringSelectionButton.getSelected());
//        });
//
//        mPanels.add(new MPassiveLabelAndElement("Alignment", mStringSelectionButton));
//        mPanels.add(new MPassiveLabelAndElement("Scale", new MFloatSelectionButton(TextHUDFeature.this.<Double>getParameter("scale").getValue()) {{
//            setOnUpdate(() ->{
//                TextHUDFeature.this.<Double>getParameter("scale").setValue(this.getData());
//            }); }
//        }));

//        return mPanels;
    }

    @Override
    public void onParameterReset() {
        stylesMap = null;
    }

}
