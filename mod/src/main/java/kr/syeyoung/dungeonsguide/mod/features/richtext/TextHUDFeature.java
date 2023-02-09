/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.richtext;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.MarkerProvider;
import kr.syeyoung.dungeonsguide.mod.config.types.TCEnum;
import kr.syeyoung.dungeonsguide.mod.config.types.TCRTextStyleMap;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.richtext.config.WidgetTextStyleConfig;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
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

import java.util.*;

public abstract class TextHUDFeature extends AbstractHUDFeature {
    protected TextHUDFeature(String category, String name, String description, String key) {
        super(category, name, description, key);

        addParameter("alignment", new FeatureParameter<>("alignment", "Alignment", "Alignment", RichText.TextAlign.LEFT, new TCEnum<>(RichText.TextAlign.values()), richText::setAlign));
        addParameter("newstyle", new FeatureParameter<>("newstyle", "TextStyle", "", styleMap, new TCRTextStyleMap(), this::updateStyle)
                .setWidgetGenerator((param) -> new WidgetTextStyleConfig(getDummyText(), styleMap)));
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
    @DGEventHandler
    public void onTick0(DGTickEvent dgTickEvent) {
        try {
            checkVisibility();
            if (isHUDViewable()) {
                TextSpan asd = getText();
                richText.setRootSpan(asd);
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
            TextSpan textSpan = hudFeature.getDummyText();

            RichText richText = new RichText(new TextSpan(
                    ParentDelegatingTextStyle.ofDefault(),
                    ""
            ), BreakWord.WORD, false, hudFeature.<RichText.TextAlign>getParameter("alignment").getValue());

            richText.setRootSpan(textSpan);

            return Collections.singletonList(richText);
        }
    }

    @Override
    public Widget instantiateDemoWidget() {
        return new TextHUDDemo(this);
    }


    public abstract boolean isHUDViewable();

    public TextSpan getDummyText() {
        return getText();
    }
    public abstract TextSpan getText();


    private Map<String, DefaultingDelegatingTextStyle> defaultStyleMap = new HashMap<>();
    private Map<String, DefaultingDelegatingTextStyle> styleMap = new HashMap<>();
    public void registerDefaultStyle(String name, DefaultingDelegatingTextStyle style) {
        defaultStyleMap.put(name, style);
    }
    public DefaultingDelegatingTextStyle getStyle(String name) {
        return styleMap.get(name);
    }

    public void updateStyle(Map<String, DefaultingDelegatingTextStyle> map) {
        styleMap.clear();
        Set<String> wasIn = new HashSet<>(map.keySet());
        Set<String> needsToBeIn = new HashSet<>(defaultStyleMap.keySet());
        needsToBeIn.removeAll(wasIn);
        for (Map.Entry<String, DefaultingDelegatingTextStyle> stringDefaultingDelegatingTextStyleEntry : map.entrySet()) {
            if (!defaultStyleMap.containsKey(stringDefaultingDelegatingTextStyleEntry.getKey())) continue;
            DefaultingDelegatingTextStyle newStyle = stringDefaultingDelegatingTextStyleEntry.getValue();
            newStyle.setName("User Setting of "+defaultStyleMap.get(stringDefaultingDelegatingTextStyleEntry.getKey()).name);
            newStyle.setParent(() -> defaultStyleMap.get(stringDefaultingDelegatingTextStyleEntry.getKey()));
            styleMap.put(stringDefaultingDelegatingTextStyleEntry.getKey(), newStyle);
        }
        for (String s : needsToBeIn) {
            styleMap.put(s, DefaultingDelegatingTextStyle.derive("User Setting of "+defaultStyleMap.get(s).name, () -> defaultStyleMap.get(s)));
            map.put(s, styleMap.get(s));
        }
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

    }

}
