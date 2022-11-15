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

package kr.syeyoung.dungeonsguide.mod.utils.cursor;


import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Cursor;
import sun.misc.Unsafe;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@SuppressWarnings("unsafe")
public class GLCursors {

    static Logger logger = LogManager.getLogger("DG-GlCursors");

    @SuppressWarnings("unsafe")
    static boolean verbose = false;

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

    public static void setupCursors() {
        if (enumCursorCursorMap.size() != 0) return;
        int platform = LWJGLUtil.getPlatform();
        for (EnumCursor value : EnumCursor.values()) {
            Cursor c = null;
            try {
                switch(platform) {
                    case LWJGLUtil.PLATFORM_WINDOWS:
                        if (value.getWindows() != -1)
                            c = createCursorWindows(value.getWindows());
                        break;
                    case LWJGLUtil.PLATFORM_LINUX:
                        if (value.getLinux() != -1)
                            c = createCursorLinux(value.getLinux());
                        break;
                    case LWJGLUtil.PLATFORM_MACOSX:
                        if (value.getMacos() != null)
                            c = createCursorMac(value.getMacos());
                        break;
                }
            } catch (Throwable e) {
                if(verbose) logger.error("Error occured while loading cursor: {}", value);
                e.printStackTrace();
            }
            try {
                if (c == null) {
                    int hotspotX = 0, hotspotY = 0;
                    BufferedImage bufferedImage = null;
                    int minC = Cursor.getMinCursorSize(), maxC = Cursor.getMaxCursorSize();
                    try {
                        ResourceLocation cursorinfo = new ResourceLocation("dungeonsguide:cursors/"+value.getAltFileName());
                        List<CursorReader.CursorData> cursorDataList = CursorReader.readFromInputStream(Minecraft.getMinecraft().getResourceManager().getResource(cursorinfo).getInputStream());
                        List<CursorReader.CursorData> cursorDataList2 = cursorDataList.stream()
                                .filter(cdata -> cdata.getBufferedImage()  != null)
                                .filter(cdata -> minC <= cdata.getHeight() && cdata.getHeight() <= maxC && minC <= cdata.getWidth() && cdata.getWidth() <= maxC)
                                .sorted(Comparator.comparingInt(CursorReader.CursorData::getWidth)).collect(Collectors.toList());

                        CursorReader.CursorData cursorData =
                                cursorDataList2.size() == 0 ? cursorDataList.get(0) : cursorDataList2.get(0);
                        if(verbose) logger.info(cursorData);
                        bufferedImage = cursorData.getBufferedImage();
                        hotspotX = cursorData.getXHotSpot();
                        hotspotY = cursorData.getYHotSpot();
                    } catch (Throwable t) {
                        if(verbose) logger.error("loading currsor failed with message, {}", String.valueOf(Throwables.getRootCause(t)));
                    }


                    int width = bufferedImage == null ? 16 : bufferedImage.getWidth();
                    int height = bufferedImage == null ? 16 : bufferedImage.getHeight();
                    int effWidth = MathHelper.clamp_int(width, Cursor.getMinCursorSize(), Cursor.getMaxCursorSize());
                    int effHeight = MathHelper.clamp_int(height, Cursor.getMinCursorSize(), Cursor.getMaxCursorSize());
                    int length = effHeight * effWidth;
                    IntBuffer intBuffer = BufferUtils.createIntBuffer(length);
                    for (int i = 0; i < length; i++) {
                        int x = i % effWidth;
                        int y = i / effWidth;
                        if (bufferedImage == null) {
                            intBuffer.put(RenderUtils.getChromaColorAt(x,y,1.0f, 1.0f, 1.0f, 1.0f));
                        } else if (x >= width || y >= height) {
                            intBuffer.put(0);
                        } else {
                            intBuffer.put(bufferedImage.getRGB(x, height - y - 1));
                        }
                    }
                    intBuffer.flip();
                    c = new Cursor(effWidth, effHeight, hotspotX, height - hotspotY - 1,1,intBuffer, null);
                }
            } catch (Throwable e) {
                if(verbose) logger.error("Error occured while loading cursor from resource:  "+value);
                e.printStackTrace();
            }
            if (c != null) {
                try {
                    Object arr = cursorField.get(c);
                    Object cursor = Array.get(arr, 0);
                    for (Field declaredField : cursor.getClass().getDeclaredFields()) {
                        declaredField.setAccessible(true);
                        Object obj = declaredField.get(cursor);
                        if(verbose) logger.info(declaredField.getName()+": "+obj+" - "+(obj instanceof ByteBuffer));
                        if (obj instanceof ByteBuffer) {
                            ByteBuffer b = (ByteBuffer) declaredField.get(cursor);
                            StringBuilder sb = new StringBuilder("Contents: ");
                            for (int i = 0; i < b.limit(); i++) {
                                sb.append(Integer.toHexString(b.get(i) & 0xFF)).append(" ");
                            }
                            if(verbose) logger.info(sb.toString());
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                enumCursorCursorMap.put(value, c);
            }
        }
    }

    public static Cursor getCursor(EnumCursor enumCursor) {
        return enumCursorCursorMap.get(enumCursor);
    }

    private static Cursor createCursorWindows(int cursor) throws LWJGLException, InstantiationException, InvocationTargetException, IllegalAccessException {
        User32 user32 = User32.INSTANCE;
        Pointer hIcon = user32
                .LoadCursorW(Pointer.NULL, cursor);
        long ptrVal = Pointer.nativeValue(hIcon);
        ByteBuffer handle = BufferUtils.createByteBuffer(Pointer.SIZE); // Why does it have to be direct? well it crashes without it.
        if (handle.order() == ByteOrder.LITTLE_ENDIAN) {
            for (int i = 0; i < Pointer.SIZE; i++) {
                byte value = (byte) ((ptrVal >> i * 8) & 0xFF);
                handle.put(value);
            }
        } else {
            for (int i = Pointer.SIZE; i >= 0; i++) {
                byte value = (byte) ((ptrVal >> i * 8) & 0xFF);
                handle.put(value);
            }
        }
        handle.position(0);
        return createCursor(handle);
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
        Pointer thePointer = foundation.objc_msgSend(nsCursor, selector);
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
