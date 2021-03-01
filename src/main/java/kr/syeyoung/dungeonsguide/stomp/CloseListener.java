package kr.syeyoung.dungeonsguide.stomp;

public interface CloseListener {
    void onClose(int code, String reason, boolean remote);
}
