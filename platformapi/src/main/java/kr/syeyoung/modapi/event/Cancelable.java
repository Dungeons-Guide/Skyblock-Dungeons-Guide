package kr.syeyoung.modapi.event;

public interface Cancelable {
    boolean isCanceled();
    void setCanceled(boolean canceled);
    default void cancel() {
        setCanceled(true);
    }
}
