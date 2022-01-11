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

package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.cursor.EnumCursor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class MTextField extends MPanel {
    private final Color foreground = Color.white;
    private final Color placeHolderColor = Color.lightGray;

    private String text = "";
    @Setter
    private String placeHolder = "";
    private int cursorBlickTicker = 0;

    private int selectionStart = 0;
    private int selectionEnd = 0;

    private int cursor = 0;

    private int xOffset = 0;
    
    public void edit(String str) {
        
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(-1,15);
    }
    
    public void setText(String text) {
        this.text = text;
    }
    private void setText0(String text) {
        this.text = text;
        edit(text);
    }

    private void setCursor0(int cursor) {
        if (cursor > text.length()) cursor = text.length();
        if (cursor < 0) cursor = 0;
        this.cursor = cursor;


        int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text.substring(0, cursor));
        int cursorX = width + 3- xOffset;
        cursorX = MathHelper.clamp_int(cursorX,10, getBounds().width - 10);
        xOffset = width+ 3 - cursorX;
        xOffset = MathHelper.clamp_int(xOffset, 0,Math.max(0, Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) - getBounds().width+10));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle clip) {
        clip(clip.x - 1, clip.y - 1, clip.width +2, clip.height + 2);
        Gui.drawRect(0,0,getBounds().width, getBounds().height, isFocused ? Color.white.getRGB() : Color.gray.getRGB());
        Gui.drawRect(1,1,getBounds().width - 1, getBounds().height - 1, Color.black.getRGB());

        Minecraft mc = Minecraft.getMinecraft();
        clip(clip.x + 1, clip.y + 1, clip.width - 2, clip.height - 2);
        FontRenderer fr = mc.fontRendererObj;
        int y = (getBounds().height - fr.FONT_HEIGHT) / 2;
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString(text, 3 - xOffset, y, foreground.getRGB());
        if (text.isEmpty())
            fr.drawString(placeHolder, 3, y, placeHolderColor.getRGB());
        // draw selection
        if (isFocused) {
            if (selectionStart != -1) {
                int startX = fr.getStringWidth(text.substring(0, selectionStart)) - xOffset;
                int endX = fr.getStringWidth(text.substring(0, selectionEnd)) - xOffset;
                Gui.drawRect(3 + startX, y, 3 + endX, y + fr.FONT_HEIGHT, 0xFF00FF00);
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                fr.drawString(text.substring(selectionStart, selectionEnd), 3 + startX, y, foreground.getRGB());
            }

            // draw cursor
            if (cursor != -1) {
                if (cursor > text.length()) setCursor0(text.length());
                int x = fr.getStringWidth(text.substring(0, cursor)) - xOffset;
                cursorBlickTicker++;
                if (cursorBlickTicker < 10)
                    Gui.drawRect(3 + x, y, 4 + x, y + fr.FONT_HEIGHT, 0xFFFFFFFF);
                if (cursorBlickTicker == 20) cursorBlickTicker = 0;
            }
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        Rectangle actualField = new Rectangle(1, 3,getBounds().width - 2, getBounds().height - 6);
        if (!actualField.contains(relMouseX, relMouseY)) return;
        if (!lastAbsClip.contains(absMouseX, absMouseY)) return;
        if (getTooltipsOpen() > 0) return;



        int relStartT = relMouseX-3;
        int offseted = relStartT + xOffset;

        selectionStart = -1;


        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        for (int i = 0; i < text.length(); i++) {
            int totalWidth = fr.getStringWidth(text.substring(0, i));
            if (offseted < totalWidth) {
                setCursor0(i);
                return;
            }
        }
        setCursor0(text.length());
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!isFocused) return;
        selectionStart = cursor;
        selectionEnd = cursor;

        int relStartT = relMouseX-3;
        int offseted = relStartT + xOffset;

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        for (int i = 0; i < text.length(); i++) {
            int totalWidth = fr.getStringWidth(text.substring(0, i));
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
        selectionEnd = text.length();
        if (selectionStart == selectionEnd) {
            selectionStart = -1;
        }
    }

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (!isFocused) return;
        if (scrollAmount > 0) {
            xOffset += 5;
        } else if (scrollAmount < 0){
            xOffset -= 5;
        }
        if (xOffset < 0) {
            xOffset = 0;
        }
        int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        int overflow = getBounds().width - 3 - width;
        if (overflow >= 0) {
            xOffset = 0;
        } else if (width - xOffset + 10 < getBounds().width) {
            xOffset = width - getBounds().width+10;
        }
    }

    @Override
    public void keyHeld(char typedChar, int keyCode) {
        if (!isFocused) return;
        this.keyPressed(typedChar, keyCode);
    }

    @Override
    public void keyPressed(char typedChar, int keycode) {
        if (!isFocused) return;

        if (selectionStart == -1) {
            if (keycode == 199) { // home
                setCursor0(0);
                xOffset = 0;
                return;
            }

            if (keycode == 207) { // end
                setCursor0(text.length());

                int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
                xOffset = Integer.max(0, width - getBounds().width+10);
                return;
            }

            if (keycode == 203) { // left
                setCursor0(this.cursor-1);;
                if (cursor < 0) setCursor0(0);
                return;
            }

            if (keycode == 205) { // right
                setCursor0(this.cursor+1);
                if (cursor > text.length()) setCursor0(text.length());
                return;
            }

            // backspace
            if (keycode == 14 && cursor > 0) {
                setText0(this.text.substring(0, cursor-1) + this.text.substring(cursor));
                setCursor0(this.cursor-1);
                return;
            }

            //del
            if (keycode == 211 && cursor < text.length()) {
                setText0(this.text.substring(0, cursor) + this.text.substring(cursor+1));
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
                        setText0(
                                this.text.substring(0, this.cursor)
                                        + theText
                                        + this.text.substring(this.cursor));

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
                setText0(
                        this.text.substring(0, this.cursor)
                                + typedChar
                                + this.text.substring(this.cursor));
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
                setCursor0(text.length());
                int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
                xOffset = Integer.max(0, width - getBounds().width+10);
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
                setText0(this.text.substring(0, selectionStart) + this.text.substring(selectionEnd));
                setCursor0(selectionStart);
                selectionStart = -1;
                return;
            }

            //del
            if (keycode == 211 && cursor < text.length()) {
                setText0(this.text.substring(0, selectionStart) + this.text.substring(selectionEnd));
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
                        setText0(
                                this.text.substring(0, this.selectionStart)
                                        + theText
                                        + this.text.substring(this.selectionEnd));
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
                StringSelection selection = new StringSelection(text.substring(selectionStart, selectionEnd));
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                return;
            }

            // text
            if (isPrintableChar(typedChar)) {
                setText0(
                        this.text.substring(0, this.selectionStart)
                                + typedChar
                                + this.text.substring(this.selectionEnd));
                this.setCursor0(this.selectionStart + 1);
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
        if (lastAbsClip.contains(absMouseX, absMouseY))
            setCursor(EnumCursor.BEAM_CURSOR);
    }

}
