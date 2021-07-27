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

package kr.syeyoung.dungeonsguide.utils.cursor;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Cursor;
import sun.misc.Unsafe;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;


public class GLCursors {
    private static Unsafe unsafe;
    private static Class cursorElement;
    private static Constructor constructor;
    private static Field cursorField;

    private static Map<EnumCursor, Cursor> enumCursorCursorMap = new HashMap<>();


    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            cursorElement = Class.forName("org.lwjgl.input.Cursor$CursorElement");
            constructor = cursorElement.getDeclaredConstructor(Object.class, long.class, long.class);
            constructor.setAccessible(true);
            cursorField = Cursor.class.getDeclaredField("cursors");
            cursorField.setAccessible(true);
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    static {
        setupCursors();
    }

    public static void setupCursors() {
        if (enumCursorCursorMap.size() != 0) return;
        int platform = LWJGLUtil.getPlatform();
        for (EnumCursor value : EnumCursor.values()) {
            Cursor c = null;
            try {
                switch(platform) {
                    case LWJGLUtil.PLATFORM_WINDOWS:
                        if (value.getWindows() == -1) continue;
                        c = createCursorWindows(value.getWindows());
                        break;
                    case LWJGLUtil.PLATFORM_LINUX:
                        if (value.getLinux() == -1) continue;
                        c = createCursorLinux(value.getLinux());
                        break;
                    case LWJGLUtil.PLATFORM_MACOSX:
                        if (value.getMacos() == null) continue;
                        c = createCursorMac(value.getMacos());
                        break;
                }
            } catch (Throwable e) {
                System.out.println("Error occured while loading cursor: "+value);
                e.printStackTrace();
            }
            try {
                if (c == null) {
                    int hotspotX = 0, hotspotY = 0;
                    try {
                        ResourceLocation cursorinfo = new ResourceLocation("dungeonsguide:cursors/"+value.getAltFileName()+".curinfo");
                        String cursorinfoStr = IOUtils.toString(Minecraft.getMinecraft().getResourceManager().getResource(cursorinfo).getInputStream());
                        hotspotX = Integer.parseInt(cursorinfoStr.split(":")[0]);
                        hotspotY = Integer.parseInt(cursorinfoStr.split(":")[1]);
                    } catch (Throwable t) {t.printStackTrace();}

                    ResourceLocation cursor = new ResourceLocation("dungeonsguide:cursors/"+value.getAltFileName()+".png");

                    BufferedImage bufferedImage = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(cursor).getInputStream());


                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();
                    int effWidth = MathHelper.clamp_int(width, Cursor.getMinCursorSize(), Cursor.getMaxCursorSize());
                    int effHeight = MathHelper.clamp_int(height, Cursor.getMinCursorSize(), Cursor.getMaxCursorSize());
                    int length = effHeight * effWidth;
                    IntBuffer intBuffer = BufferUtils.createIntBuffer(length);
                    for (int i = 0; i < length; i++) {
                        int x = i % effWidth;
                        int y = i / effWidth;
                        if (x >= width || y >= height) {
                            intBuffer.put(0);
                        } else {
                            intBuffer.put(bufferedImage.getRGB(x, height - y - 1));
                        }
                    }
                    intBuffer.flip();
                    c = new Cursor(effWidth, effHeight, hotspotX, hotspotY,1,intBuffer, null);
                }
            } catch (Throwable e) {
                System.out.println("Error occured while loading cursor from resource:  "+value);
                e.printStackTrace();
            }
            if (c != null) enumCursorCursorMap.put(value, c);
        }
    }

    public static Cursor getCursor(EnumCursor enumCursor) {
        return enumCursorCursorMap.get(enumCursor);
    }

    private static Cursor createCursorWindows(int cursor) throws LWJGLException, InstantiationException, InvocationTargetException, IllegalAccessException {
        User32 user32 = User32.INSTANCE;
        Pointer hIcon = user32
                .LoadCursorW(Pointer.NULL, cursor);
        long iconPtr = Pointer.nativeValue(hIcon);

        return createCursor(iconPtr);
    }
    private static Cursor createCursorLinux(int cursor) throws LWJGLException, InstantiationException, InvocationTargetException, IllegalAccessException {
        X11.Display display = X11.INSTANCE.XOpenDisplay(null);
        Pointer fontCursor = X11.INSTANCE.XCreateFontCursor(display, cursor);
        long iconPtr = Pointer.nativeValue(fontCursor);

        return createCursor(iconPtr);
    }
    private static Cursor createCursorMac(String cursor) throws LWJGLException, InstantiationException, InvocationTargetException, IllegalAccessException {
        // trust me, it's horrible.
        Foundation foundation = Foundation.INSTANCE;
        Pointer nsCursor = foundation.objc_getClass("NSCursor");
        Pointer selector = foundation.sel_registerName(cursor);
        Structure structure = foundation.objc_msgSend_stret(nsCursor, selector);
        Pointer thePointer = structure.getPointer();
        long iconPtr = Pointer.nativeValue(thePointer);

        return createCursor(iconPtr);
    }


    private static Cursor createCursor(Object handle) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        // Yes. I had no way.
        Cursor ADANGEROUSOBJECT = (Cursor) unsafe.allocateInstance(Cursor.class);
        Object cursorElement = constructor.newInstance(handle, 0, LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX ? -1 : System.currentTimeMillis());
        Object array = Array.newInstance(GLCursors.cursorElement, 1);
        Array.set(array, 0, cursorElement);
        cursorField.set(ADANGEROUSOBJECT, array);
        return ADANGEROUSOBJECT;
    }
}
