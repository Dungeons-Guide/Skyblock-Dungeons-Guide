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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext;

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RichText extends Widget implements Layouter, Renderer {
    private TextSpan rootSpan;
    private List<FlatTextSpan> flattenSpan = new LinkedList<>();
    private BreakWord breakWord;
    private boolean takeAllSpace = false;
    @Setter
    private TextAlign align;


    private List<RichLine> richLines = new LinkedList<>();
    public static enum TextAlign {
        LEFT, CENTER, RIGHT
    }

    public RichText(TextSpan textSpan, BreakWord breakWord, boolean takeAllSpace, TextAlign align) {
        this.rootSpan = textSpan;
        rootSpan.flattenTextSpan(flattenSpan::add);
        this.breakWord = breakWord;
        this.takeAllSpace = takeAllSpace;
        this.align = align;
    }

    public void setRootSpan(TextSpan rootSpan) {
        this.rootSpan = rootSpan;
        flattenSpan.clear();
        rootSpan.flattenTextSpan(flattenSpan::add);
        this.getDomElement().requestRelayout();
    }

    public void setBreakWord(BreakWord breakWord) {
        this.breakWord = breakWord;
        this.getDomElement().requestRelayout();
    }

    public void setTakeAllSpace(boolean takeAllSpace) {
        this.takeAllSpace = takeAllSpace;
        this.getDomElement().requestRelayout();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        LinkedList<FlatTextSpan> flatTextSpans = new LinkedList<>(flattenSpan); // shallow copy it. for faster layout
        LinkedList<RichLine> lines = new LinkedList<>();
        LinkedList<FlatTextSpan> line = new LinkedList<>();
        double remaining = constraintBox.getMaxWidth();
        double usedUp = 0;
        double maxHeight = 0;
        double maxBaseline = 0;

        double sumHeight = 0;
        double maxWidth = 0;

        while (!flatTextSpans.isEmpty()) {
            FlatTextSpan first = flatTextSpans.pollFirst();

            BrokenWordData brokenWordData = first.breakWord(remaining, constraintBox.getMaxWidth(), breakWord);
            remaining -= brokenWordData.getFirstWidth();
            usedUp += brokenWordData.getFirstWidth();
            line.add(brokenWordData.getFirst());

            if (brokenWordData.getFirst().value.length == 0 && first.value.length != 0 && remaining == constraintBox.getMaxWidth()) {
                try {
                    throw new IllegalStateException("Can not fit stuff into this");
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            maxHeight = Math.max(maxHeight, first.getHeight());
            maxBaseline = Math.max(maxBaseline, first.getBaseline());

            if (brokenWordData.getSecond() != null) {
                flatTextSpans.addFirst(brokenWordData.getSecond());
            }

            if (brokenWordData.isBroken()) {
                lines.add(new RichLine(line, usedUp, maxHeight, maxBaseline));
                line = new LinkedList<>();
                maxWidth = Math.max(maxWidth, usedUp);
                sumHeight += maxHeight;

                remaining = constraintBox.getMaxWidth();
                usedUp = 0;
                maxHeight = 0;
                maxBaseline = 0;
            }
        }
        if (!line.isEmpty()) {
            lines.add(new RichLine(line, usedUp, maxHeight, maxBaseline));
            maxWidth = Math.max(maxWidth, usedUp);
            sumHeight += maxHeight;
        }

        richLines = lines;

        return new Size(takeAllSpace ? constraintBox.getMaxWidth() :
                Layouter.clamp(maxWidth, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                Layouter.clamp(sumHeight, constraintBox.getMinHeight(), constraintBox.getMaxHeight()));
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        LinkedList<FlatTextSpan> flatTextSpans = new LinkedList<>();
        rootSpan.flattenTextSpan(flatTextSpans::add);

        LinkedList<FlatTextSpan> line = new LinkedList<>();
        double usedUp = 0;

        double maxWidth = 0;
        while (!flatTextSpans.isEmpty()) {
            FlatTextSpan first = flatTextSpans.pollFirst();
            BrokenWordData brokenWordData = first.breakWord(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, breakWord);
            usedUp += brokenWordData.getFirstWidth();
            line.add(brokenWordData.getFirst());

            if (brokenWordData.getSecond() != null) {
                flatTextSpans.addFirst(brokenWordData.getSecond());
            }

            if (brokenWordData.isBroken()) {
                maxWidth = Math.max(maxWidth, usedUp);
                usedUp = 0;
            }
        }
        if (!line.isEmpty()) {
            maxWidth = Math.max(maxWidth, usedUp);
        }

        return maxWidth;
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        LinkedList<FlatTextSpan> flatTextSpans = new LinkedList<>();
        rootSpan.flattenTextSpan(flatTextSpans::add);
        double remaining = width == 0 ? Double.POSITIVE_INFINITY : width;
        double maxHeight = 0;
        double sumHeight = 0;
        while (!flatTextSpans.isEmpty()) {
            FlatTextSpan first = flatTextSpans.pollFirst();

            BrokenWordData brokenWordData = first.breakWord(remaining, width == 0 ? Double.POSITIVE_INFINITY : width, breakWord);
            remaining -= brokenWordData.getFirstWidth();

            if (brokenWordData.getFirst().value.length == 0 && first.value.length != 0 && remaining == width) {
                return 0;
            }

            maxHeight = Math.max(maxHeight, first.getHeight());

            if (brokenWordData.getSecond() != null) {
                flatTextSpans.addFirst(brokenWordData.getSecond());
            }

            if (brokenWordData.isBroken()) {
                remaining = width == 0 ? Double.POSITIVE_INFINITY : width;
                sumHeight += maxHeight;
                maxHeight = 0;
            }
        }
        sumHeight += maxHeight;
        return sumHeight;
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        double x = 0;
        double y = 0;
        double width = buildContext.getSize().getWidth();
        double currentScale = buildContext.getAbsBounds().getWidth() / width;
        for (RichLine richLine : richLines) {
            if (align == TextAlign.RIGHT)
                x = width - richLine.getWidth();
            else if (align == TextAlign.CENTER)
                x = (width - richLine.getWidth()) / 2;
            else
                x = 0;
            for (FlatTextSpan lineElement : richLine.getLineElements()) {
                lineElement.textStyle.getFontRenderer()
                                .render(lineElement, x, y + richLine.getBaseline() - lineElement.getBaseline()
                                        + lineElement.textStyle.getTopAscent() * lineElement.textStyle.getSize(), currentScale);
                x += lineElement.getWidth();
            }
            y += richLine.getHeight();
        }
    }
}
