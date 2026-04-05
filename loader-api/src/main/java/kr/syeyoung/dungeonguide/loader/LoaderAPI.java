package kr.syeyoung.dungeonguide.loader;

public interface LoaderAPI {
    default void requestUnload() {
        throw new UnsupportedOperationException("");
    }
}
