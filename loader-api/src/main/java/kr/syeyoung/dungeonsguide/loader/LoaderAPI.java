package kr.syeyoung.dungeonsguide.loader;

public interface LoaderAPI {
    default void requestUnload() {
        throw new UnsupportedOperationException("");
    }
}
