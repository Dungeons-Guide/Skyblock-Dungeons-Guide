package kr.syeyoung.dungeonsguide.mod.utils.cursor;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface XCursor extends Library {
    Pointer XcursorLibraryLoadCursor(Pointer display, String name);
}
