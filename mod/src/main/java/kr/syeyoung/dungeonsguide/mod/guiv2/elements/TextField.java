/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.DrawNothingRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class TextField {
    public static class TLayouter extends Layouter {

        public TLayouter(DomElement element) {
            super(element);
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            return new Dimension(constraintBox.getMaxWidth(), clamp(15, constraintBox.getMinHeight(), constraintBox.getMaxHeight()));
        }
    }

    public static class TRenderer extends DrawNothingRenderer {
        private TWidget controller;
        public TRenderer(DomElement domElement) {
            super(domElement);
            this.controller = (TWidget) domElement.getWidget();
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
            Rectangle bounds = getDomElement().getRelativeBound();

            Gui.drawRect(0,0,bounds.width, bounds.height, getDomElement().isFocused() ? Color.white.getRGB() : Color.gray.getRGB());
            Gui.drawRect(1,1,bounds.width - 1, bounds.height - 1, Color.black.getRGB());

            Minecraft mc = Minecraft.getMinecraft();
            clip(1, 1, bounds.width -2, bounds.height-2);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);

            String text = controller.value.getValue();
            FontRenderer fr = mc.fontRendererObj;
            int y = (bounds.height - fr.FONT_HEIGHT) / 2;
            GlStateManager.enableTexture2D();
            fr.drawString(controller.value.getValue(), 3 - controller.xOffset, y, controller.color.getValue());
            if (text.isEmpty())
                fr.drawString(controller.placeholder.getValue(), 3, y, controller.placeholderColor.getValue());
            // draw selection
            if (getDomElement().isFocused()) {
                if (controller.selectionStart != -1) {
                    int startX = fr.getStringWidth(text.substring(0, controller.selectionStart)) - controller.xOffset;
                    int endX = fr.getStringWidth(text.substring(0, controller.selectionEnd)) - controller.xOffset;
                    Gui.drawRect(3 + startX, y, 3 + endX, y + fr.FONT_HEIGHT, 0xFF00FF00);
                    GlStateManager.enableTexture2D();
                    fr.drawString(text.substring(controller.selectionStart, controller.selectionEnd), 3 + startX, y, controller.color.getValue());
                }

                // draw cursor
                if (controller.cursor != -1) {
                    if (controller.cursor > text.length()) controller.setCursor0(text.length());
                    int x = fr.getStringWidth(text.substring(0, controller.cursor)) - controller.xOffset;

                    if (System.currentTimeMillis() % 1500 < 750)
                        Gui.drawRect(3 + x, y, 4 + x, y + fr.FONT_HEIGHT, 0xFFFFFFFF);
                }
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public static class TWidget extends Widget {

        @Export(
                attributeName = "value"
        )
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class, "");

        @Export(
                attributeName = "placeholder"
        )
        public final BindableAttribute<String> placeholder = new BindableAttribute<>(String.class, "");
        
        @Export(
                attributeName = "color"
        )
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0xFFFFFFFF);


        @Export(
                attributeName = "placeholderColor"
        )
        public final BindableAttribute<Integer> placeholderColor = new BindableAttribute<>(Integer.class, 0xFFAAAAAA);


        private int selectionStart = 0;
        private int selectionEnd = 0;

        private int cursor = 0;

        private int xOffset = 0;
        
        
        
        public TWidget(DomElement element) {
            super(element);
            loadAttributes();
        }



        private void setCursor0(int cursor) {
            if (cursor > value.getValue().length()) cursor = value.getValue().length();
            if (cursor < 0) cursor = 0;
            this.cursor = cursor;


            int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(value.getValue().substring(0, cursor));
            int cursorX = width + 3- xOffset;
            cursorX = MathHelper.clamp_int(cursorX,10, getElement().getAbsBounds().width - 10);
            xOffset = width+ 3 - cursorX;
            xOffset = MathHelper.clamp_int(xOffset, 0,Math.max(0, Minecraft.getMinecraft().fontRendererObj.getStringWidth(value.getValue()) - getElement().getRelativeBound().width+10));
        }

        @Override
        public boolean mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
            getElement().obtainFocus();
            Rectangle actualField = new Rectangle(1, 3,getElement().getAbsBounds().width - 2,
                    getElement().getAbsBounds().height - 6);
            if (!actualField.contains(relMouseX, relMouseY)) return false;



            int relStartT = relMouseX-3;
            int offseted = relStartT + xOffset;

            selectionStart = -1;


            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

            for (int i = 0; i < value.getValue().length(); i++) {
                int totalWidth = fr.getStringWidth(value.getValue().substring(0, i));
                if (offseted < totalWidth) {
                    setCursor0(i);
                    return true;
                }
            }
            setCursor0(value.getValue().length());
            return true;
        }

        @Override
        public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (!getElement().isFocused()) return;
            selectionStart = cursor;
            selectionEnd = cursor;

            int relStartT = relMouseX-3;
            int offseted = relStartT + xOffset;

            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

            for (int i = 0; i < value.getValue().length(); i++) {
                int totalWidth = fr.getStringWidth(value.getValue().substring(0, i));
                if (offseted < totalWidth) {
                    if (i < cursor) {
                        selectionStart = i;
                        selectionEnd = cursor;
                    } else {
                        selectionStart = cursor;
                        selectionEnd = i;
                    }
                    return;
                }
            }
            selectionEnd = value.getValue().length();
            if (selectionStart == selectionEnd) {
                selectionStart = -1;
            }
        }

        @Override
        public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
            if (!getElement().isFocused()) return;
            if (scrollAmount > 0) {
                xOffset += 5;
            } else if (scrollAmount < 0){
                xOffset -= 5;
            }
            if (xOffset < 0) {
                xOffset = 0;
            }
            int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(value.getValue());
            int overflow = getElement().getRelativeBound().width - 3 - width;
            if (overflow >= 0) {
                xOffset = 0;
            } else if (width - xOffset + 10 < getElement().getRelativeBound().width) {
                xOffset = width - getElement().getRelativeBound().width+10;
            }
        }

        @Override
        public void keyHeld(char typedChar, int keyCode) {
            this.keyPressed(typedChar, keyCode);
        }

        @Override
        public void keyPressed(char typedChar, int keycode) {
            if (selectionStart == -1) {
                if (keycode == 199) { // home
                    setCursor0(0);
                    xOffset = 0;
                    return;
                }

                if (keycode == 207) { // end
                    setCursor0(value.getValue().length());

                    int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(value.getValue());
                    xOffset = Integer.max(0, width - getElement().getRelativeBound().width+10);
                    return;
                }

                if (keycode == 203) { // left
                    setCursor0(this.cursor-1);;
                    if (cursor < 0) setCursor0(0);
                    return;
                }

                if (keycode == 205) { // right
                    setCursor0(this.cursor+1);
                    if (cursor > value.getValue().length()) setCursor0(value.getValue().length());
                    return;
                }

                // backspace
                if (keycode == 14 && cursor > 0) {
                    value.setValue(this.value.getValue().substring(0, cursor-1) + this.value.getValue().substring(cursor));
                    setCursor0(this.cursor-1);
                    return;
                }

                //del
                if (keycode == 211 && cursor < value.getValue().length()) {
                    value.setValue(this.value.getValue().substring(0, cursor) + this.value.getValue().substring(cursor+1));
                    return;
                }

                // paste
                boolean shouldPaste = false;
                if (keycode == 47) {
                    if (Minecraft.isRunningOnMac) {  // mac
                        if (Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220)) {
                            shouldPaste = true;
                        }
                    } else { // literally everything else
                        if (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157)) {
                            shouldPaste = true;
                        }
                    }
                }
                if (shouldPaste) {
                    Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                    if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            Object theText = transferable.getTransferData(DataFlavor.stringFlavor);
                            value.setValue(
                                    this.value.getValue().substring(0, this.cursor)
                                            + theText
                                            + this.value.getValue().substring(this.cursor));

                            cursor += theText.toString().length();
                        } catch (UnsupportedFlavorException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }

                // text
                if (isPrintableChar(typedChar)) {
                    value.setValue(
                            this.value.getValue().substring(0, this.cursor)
                                    + typedChar
                                    + this.value.getValue().substring(this.cursor));
                    this.setCursor0(this.cursor+1);;
                    return;
                }
            } else {
                if (keycode == 199) { // home
                    setCursor0(0);
                    selectionStart = -1;
                    xOffset =0;
                    return;
                }

                if (keycode == 207) { // end
                    selectionStart = -1;
                    setCursor0(value.getValue().length());
                    int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(value.getValue());
                    xOffset = Integer.max(0, width - getElement().getRelativeBound().width+10);
                    return;
                }

                if (keycode == 203) { // left
                    setCursor0(selectionStart);
                    selectionStart = -1;
                    return;
                }

                if (keycode == 205) { // right
                    setCursor0(selectionEnd);
                    selectionStart = -1;
                    return;
                }

                // backspace
                if (keycode == 14 && cursor > 0) {
                    value.setValue(this.value.getValue().substring(0, selectionStart) + this.value.getValue().substring(selectionEnd));
                    setCursor0(selectionStart);
                    selectionStart = -1;
                    return;
                }

                //del
                if (keycode == 211 && cursor < value.getValue().length()) {
                    value.setValue(this.value.getValue().substring(0, selectionStart) + this.value.getValue().substring(selectionEnd));
                    setCursor0(selectionStart);
                    selectionStart = -1;
                    return;
                }

                // paste
                boolean shouldPaste = false;
                if (keycode == 47) {
                    if (Minecraft.isRunningOnMac) {  // mac
                        if (Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220)) {
                            shouldPaste = true;
                        }
                    } else { // literally everything else
                        if (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157)) {
                            shouldPaste = true;
                        }
                    }
                }
                if (shouldPaste) {
                    Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                    if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        try {
                            Object theText = transferable.getTransferData(DataFlavor.stringFlavor);
                            value.setValue(
                                    this.value.getValue().substring(0, this.selectionStart)
                                            + theText
                                            + this.value.getValue().substring(this.selectionEnd));
                            setCursor0(this.selectionStart + theText.toString().length());
                        } catch (UnsupportedFlavorException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        selectionStart = -1;
                    }
                    return;
                }
                boolean shouldCopy = false;
                if (keycode == 46) {
                    if (Minecraft.isRunningOnMac) {  // mac
                        if (Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220)) {
                            shouldCopy = true;
                        }
                    } else { // literally everything else
                        if (Keyboard.isKeyDown(29) || Keyboard.isKeyDown(157)) {
                            shouldCopy = true;
                        }
                    }
                }
                if (shouldCopy) {
                    StringSelection selection = new StringSelection(value.getValue().substring(selectionStart, selectionEnd));
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                    return;
                }

                // text
                if (isPrintableChar(typedChar)) {
                    value.setValue(
                            this.value.getValue().substring(0, this.selectionStart)
                                    + typedChar
                                    + this.value.getValue().substring(this.selectionEnd));
                    setCursor0(this.selectionStart + 1);
                    selectionStart = -1;
                    return;
                }
            }
        }
        public boolean isPrintableChar( char c ) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
            return (!Character.isISOControl(c)) &&
                    c != KeyEvent.CHAR_UNDEFINED &&
                    block != null &&
                    block != Character.UnicodeBlock.SPECIALS;
        }

        @Override
        public void mouseMoved(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
            if (getElement().getAbsBounds().contains(absMouseX, absMouseY))
                getElement().setCursor(EnumCursor.BEAM_CURSOR);
        }
    }
    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            TLayouter::new, TRenderer::new, TWidget::new
    );
}
