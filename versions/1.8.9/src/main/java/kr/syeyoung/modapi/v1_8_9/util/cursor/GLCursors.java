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

package kr.syeyoung.modapi.v1_8_9.util.cursor;


import com.google.common.base.Throwables;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import kr.syeyoung.dungeonsguide.launcher.util.cursor.XCursor;
import kr.syeyoung.dungeonsguide.mod.utils.MathUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Cursor;
import sun.misc.Unsafe;

import java.awt.image.BufferedImage;
import java.lang.reflect.*;
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

    private static Class cursorElement;
    private static Constructor constructor;
    private static Field cursorField;

    private static Class linuxDisplay;
    private static Method linuxDisplayGetDisplay;


    private static Map<kr.syeyoung.modapi.util.EnumCursor, Cursor> enumCursorCursorMap = new HashMap<>();


    private static Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            cursorElement = Class.forName("org.lwjgl.input.Cursor$CursorElement");
            constructor = cursorElement.getDeclaredConstructor(Object.class, long.class, long.class);
            constructor.setAccessible(true);
            cursorField = Cursor.class.getDeclaredField("cursors");
            cursorField.setAccessible(true);

            linuxDisplay = Class.forName("org.lwjgl.opengl.LinuxDisplay");
            linuxDisplayGetDisplay = linuxDisplay.getDeclaredMethod("getDisplay");
            linuxDisplayGetDisplay.setAccessible(true);
        } catch (NoSuchFieldException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void setupCursors() {
        if (enumCursorCursorMap.size() != 0) return;
        int platform = LWJGLUtil.getPlatform();
        for (kr.syeyoung.modapi.util.EnumCursor value : kr.syeyoung.modapi.util.EnumCursor.values()) {
            Cursor c = null;
            try {
                switch(platform) {
                    case LWJGLUtil.PLATFORM_WINDOWS:
                        if (value.getWindows() != -1)
                            c = createCursorWindows(value.getWindows());
                        break;
                    case LWJGLUtil.PLATFORM_LINUX:
                        if (value.getLinux() != -1)
                            c = createCursorLinux(value.getLinux(), value.getXcursor());
                        break;
                    case LWJGLUtil.PLATFORM_MACOSX:
                        if (value.getMacos() != null)
                            c = createCursorMac(value.getMacos());
                        break;
                }
            } catch (Throwable e) {
                if(verbose) logger.error("Error occurred while loading cursor: {}", value);
                e.printStackTrace();
            }
            try {
                if (c == null) {
                    int hotspotX = 0, hotspotY = 0;
                    BufferedImage bufferedImage = null;
                    int minC = Cursor.getMinCursorSize(), maxC = Cursor.getMaxCursorSize();
                    try {
                        ResourceIdentifier cursorInfo = new ResourceIdentifier("dungeonsguide:cursors/"+value.getAltFileName());
                        List<CursorReader.CursorData> cursorDataList = CursorReader.readFromInputStream(ModAPI.getAPI().getResourceManager().getResource(cursorInfo).getInputStream());
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
                        if(verbose) logger.error("loading cursor failed with message, {}", String.valueOf(Throwables.getRootCause(t)));
                    }


                    int width = bufferedImage == null ? 16 : bufferedImage.getWidth();
                    int height = bufferedImage == null ? 16 : bufferedImage.getHeight();
                    int effWidth = MathUtils.clamp_int(width, Cursor.getMinCursorSize(), Cursor.getMaxCursorSize());
                    int effHeight = MathUtils.clamp_int(height, Cursor.getMinCursorSize(), Cursor.getMaxCursorSize());
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
                if(verbose) logger.error("Error occurred while loading cursor from resource:  "+value);
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

    public static Cursor getCursor(kr.syeyoung.modapi.util.EnumCursor enumCursor) {
        return enumCursorCursorMap.get(enumCursor);
    }

    public static void cleanup() {
        F_INSTANCE = null; U_INSTANCE = null; X_INSTANCE = null;

//        Map<Class, ?> blah = ReflectionHelper.getPrivateValue(Native.class, null, "typeOptions");
//        blah.remove(Foundation.class);
//        blah.remove(User32.class);
//        blah.remove(X11.class);
    }

    private static Foundation F_INSTANCE;
    private static User32 U_INSTANCE;
    private static X11 X_INSTANCE;
    private static XCursor X_CURSOR_INSTANCE;
    private static Cursor createCursorWindows(int cursor) throws LWJGLException, InstantiationException, InvocationTargetException, IllegalAccessException {
        if (U_INSTANCE == null) U_INSTANCE= (User32) Native.loadLibrary("User32", User32.class);
        User32 user32 = U_INSTANCE;
        Pointer hIcon = user32
                .LoadCursorW(Pointer.NULL, cursor);
        long ptrVal = Pointer.nativeValue(hIcon);
        ByteBuffer handle = BufferUtils.createByteBuffer(Native.POINTER_SIZE); // Why does it have to be direct? well it crashes without it.
        if (handle.order() == ByteOrder.LITTLE_ENDIAN) {
            for (int i = 0; i < Native.POINTER_SIZE; i++) {
                byte value = (byte) ((ptrVal >> i * 8) & 0xFF);
                handle.put(value);
            }
        } else {
            for (int i = Native.POINTER_SIZE; i >= 0; i++) {
                byte value = (byte) ((ptrVal >> i * 8) & 0xFF);
                handle.put(value);
            }
        }
        handle.position(0);
        return createCursor(handle);
    }

    private static Cursor createCursorLinux(int cursor, String xCursor) throws LWJGLException, InstantiationException, InvocationTargetException, IllegalAccessException {
        if (X_INSTANCE == null) X_INSTANCE = (X11) Native.loadLibrary("X11", X11.class);
        if (X_CURSOR_INSTANCE == null) X_CURSOR_INSTANCE = (XCursor) Native.loadLibrary("Xcursor", XCursor.class);
        long display = (long) linuxDisplayGetDisplay.invoke(null);
        Pointer fontCursor = X_CURSOR_INSTANCE.XcursorLibraryLoadCursor(new Pointer(display), xCursor);
        if (fontCursor == null || Pointer.nativeValue(fontCursor) == 0)
            fontCursor = X_INSTANCE.XCreateFontCursor(new Pointer(display), cursor);
        long iconPtr = Pointer.nativeValue(fontCursor);

        return createCursor(iconPtr);
    }
    private static Cursor createCursorMac(String cursor) throws LWJGLException, InstantiationException, InvocationTargetException, IllegalAccessException {
        // trust me, it's horrible.
        if (F_INSTANCE == null) F_INSTANCE = (Foundation) Native.loadLibrary("Foundation", Foundation.class);
        Foundation foundation = F_INSTANCE;
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
