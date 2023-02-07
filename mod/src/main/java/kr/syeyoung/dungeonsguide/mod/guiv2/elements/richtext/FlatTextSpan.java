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

import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.styles.ITextStyle;

public class FlatTextSpan {
    public final ITextStyle textStyle;

    public final char[] value;

    public FlatTextSpan(ITextStyle textStyle, char[] value) {
        this.textStyle = textStyle;
        this.value = value;
    }

    public double getWidth() {
        double sum  =0;
        for (char c : value) {
            sum += textStyle.getFontRenderer().getWidth(c, textStyle);
        }
        return sum;
    }
    public double getHeight() {
        return (1 + textStyle.getTopAscent() + textStyle.getBottomAscent()) * textStyle.getSize();
    }
    public double getBaseline() {
        return textStyle.getSize() * (textStyle.getFontRenderer().getBaselineHeight(textStyle) + textStyle.getTopAscent());
    }

    public BrokenWordData breakWord(double remainingWidth, double nextLineWidth, BreakWord breakWord) {
        if (breakWord == BreakWord.WORD) {
            // prefer to break on words
            double scaledRemainingWidth = remainingWidth;
            double scaledNextLineWidth = nextLineWidth;

            double totalWidth = 0;
            double wordWidth = 0;
            double charWidth = 0;
            int wordStart = 0;
            int potentialBreak = -1;

            int endIdx = value.length;
            boolean lineBroken = false;
            boolean wasNewLine = false;
            for (int i = 0; i < value.length; i++) {
                char character = value[i];
                charWidth = textStyle.getFontRenderer().getWidth(character, textStyle);

                totalWidth += charWidth;
                wordWidth += charWidth;

                if (character == ' ' || character == '\n') {
                    if (potentialBreak == -1) {
                    } else if (scaledNextLineWidth < wordWidth) {
                        // Break at potential, word is greater than next line
                        endIdx = potentialBreak;
                        lineBroken = true;
                        break;
                    } else {
                        // Delegate to next line.
                        endIdx = wordStart;
                        lineBroken = true;
                        break;
                    }

                    // Force break.
                    if (character == '\n') {
                        endIdx = i+1;
                        lineBroken = true;
                        wasNewLine = true;
                        break;
                    }

                    // Since adding space exceeded remaining width, break without adding space.
                    if (totalWidth > scaledRemainingWidth) {
                        endIdx = i;
                        lineBroken = true;
                        break;
                    }

                    // reset states
                    wordStart = i + 1;
                    potentialBreak = -1;
                    wordWidth = 0;
                }

                if (totalWidth > scaledRemainingWidth && potentialBreak == -1) {
                    potentialBreak = i;
                }
            }
            if (potentialBreak == -1) {
            } else if (scaledNextLineWidth < wordWidth) {
                // Break at potential, word is greater than next line
                endIdx = potentialBreak;
                lineBroken = true;
            } else {
                // Delegate to next line.
                endIdx = wordStart;
                lineBroken = true;
            }

            char[] first = new char[endIdx];
            System.arraycopy(value, 0, first, 0, endIdx);

            char[] second = null;
            if (lineBroken) {
                int startRealWord = -1;
                if (!wasNewLine) {
                    for (int i = endIdx; i < value.length; i++) {
                        if (value[i] == ' ') continue;
                        startRealWord = i;
                        break;
                    }
                } else {
                    startRealWord = endIdx;
                }
                if (startRealWord != -1) {
                    second = new char[value.length - startRealWord];
                    System.arraycopy(value, startRealWord, second, 0, second.length);
                }
            }

            FlatTextSpan flatTextSpan = new FlatTextSpan(textStyle, first);
            FlatTextSpan secondSpan = null;
            if (second != null)
                secondSpan = new FlatTextSpan(textStyle, second);


            double resultingWidth = 0;
            for (char c : first) {
                resultingWidth += textStyle.getFontRenderer().getWidth(c, textStyle);
            }

            return new BrokenWordData(flatTextSpan, secondSpan, lineBroken, resultingWidth);
        } else {
            // break anywhere
            double scaledRemainingWidth = remainingWidth;

            double totalWidth = 0;
            double effectiveWidth = 0;
            boolean lineBroken  =false;
            boolean wasNewLine = false;
            int endIdx = value.length;
            for (int i = 0; i < value.length; i++) {
                char character = value[i];
                double charWidth = textStyle.getFontRenderer().getWidth(character, textStyle);

                totalWidth += charWidth;

                if (character == '\n') {
                    // Force break.
                    endIdx = i + 1;
                    lineBroken = true;
                    wasNewLine = true;
                    break;
                }

                if (totalWidth > scaledRemainingWidth) {
                    // break here.
                    endIdx = i;
                    lineBroken = true;
                    break;
                }
                effectiveWidth += charWidth;
            }

            char[] first = new char[endIdx];
            System.arraycopy(value, 0, first, 0, endIdx);

            char[] second = null;
            if (lineBroken) {
                int startRealWord = -1;
                if (!wasNewLine) {
                    for (int i = endIdx; i < value.length; i++) {
                        if (value[i] == ' ') continue;
                        startRealWord = i;
                        break;
                    }
                } else {
                    startRealWord = endIdx;
                }
                if (startRealWord != -1) {
                    second = new char[value.length - startRealWord];
                    System.arraycopy(value, startRealWord, second, 0, second.length);
                }
            }

            FlatTextSpan flatTextSpan = new FlatTextSpan(textStyle, first);
            FlatTextSpan secondSpan = null;
            if (second != null)
                secondSpan = new FlatTextSpan(textStyle, second);

            return new BrokenWordData(flatTextSpan, secondSpan, lineBroken, effectiveWidth);
        }
    }
}
