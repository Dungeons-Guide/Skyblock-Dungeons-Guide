package kr.syeyoung.modapi.events;

public interface Cancelable {
    boolean isCanceled();
    void setCanceled(boolean canceled);
    default void cancel() {
        setCanceled(true);
    }
}
