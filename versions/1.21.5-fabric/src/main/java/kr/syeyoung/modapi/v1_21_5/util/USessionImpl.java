package kr.syeyoung.modapi.v1_21_5.util;

import kr.syeyoung.modapi.util.USession;
import net.minecraft.util.Session;

import java.util.UUID;

public class USessionImpl implements USession {
    private Session delegate;

    public USessionImpl(Session delegate) {
        this.delegate = delegate;
    }

    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public UUID getUUID() {
        return delegate.getProfile().getId();
    }
}
