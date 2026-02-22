package kr.syeyoung.modapi.v1_8_9.util.cursor;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface XCursor extends Library {
    Pointer XcursorLibraryLoadCursor(Pointer display, String name);
}
