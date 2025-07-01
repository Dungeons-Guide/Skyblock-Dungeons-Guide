package kr.syeyoung.modapi.paralleluniverse.tablist;

import java.util.Collections;
import java.util.SortedSet;

public interface UTabList {
    SortedSet<? extends UTabListEntry> getTabListEntries();
}
