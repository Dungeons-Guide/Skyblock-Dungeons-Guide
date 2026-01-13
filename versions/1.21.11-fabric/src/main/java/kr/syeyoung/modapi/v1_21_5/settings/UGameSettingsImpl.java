package kr.syeyoung.modapi.v1_21_5.settings;

import kr.syeyoung.modapi.settings.UGameSettings;
import net.minecraft.client.option.GameOptions;

public class UGameSettingsImpl implements UGameSettings {
    private GameOptions delegate;

    public UGameSettingsImpl(GameOptions delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setGamma(float value) {
        delegate.getGamma().setValue((double) value);
    }
}
