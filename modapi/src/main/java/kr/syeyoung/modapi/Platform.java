package kr.syeyoung.modapi;

public interface Platform {
    String getName();
    String getMinecraftVersion();
    String getPlatformVersion();

    boolean isOldChat();
}
