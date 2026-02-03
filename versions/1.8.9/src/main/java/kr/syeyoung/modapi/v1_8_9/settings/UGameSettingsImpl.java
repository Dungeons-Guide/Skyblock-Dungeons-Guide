package kr.syeyoung.modapi.v1_8_9.settings;

import kr.syeyoung.modapi.settings.UGameSettings;
import net.minecraft.client.settings.GameSettings;

public class UGameSettingsImpl implements UGameSettings {
    private GameSettings delegate;

    public UGameSettingsImpl(GameSettings delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setGamma(float value) {
        delegate.setOptionFloatValue(GameSettings.Options.GAMMA, value);
    }

    @Override
    public int getRenderDistanceChunks() {
        return delegate.renderDistanceChunks;
    }
}
