package kr.syeyoung.dungeonsguide.launcher.util.cursor;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface XCursor extends Library {
    XCursor INSTANCE = (XCursor) Native.loadLibrary("Xcursor", XCursor.class);
    Pointer XcursorLibraryLoadCursor(Pointer display, String name);
}
