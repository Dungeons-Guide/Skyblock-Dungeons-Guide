package kr.syeyoung.modapi.v1_21_9.util;

import kr.syeyoung.modapi.util.USession;
import net.minecraft.client.session.Session;

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
        return delegate.getUuidOrNull();
    }
}
