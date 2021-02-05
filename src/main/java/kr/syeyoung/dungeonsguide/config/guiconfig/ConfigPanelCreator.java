package kr.syeyoung.dungeonsguide.config.guiconfig;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ConfigPanelCreator implements Function<String, MPanel> {
    public static final ConfigPanelCreator INSTANCE = new ConfigPanelCreator();

    public static final Map<String, Supplier<MPanel>> map = new HashMap<String, Supplier<MPanel>>();

    @Nullable
    @Override
    public MPanel apply(@Nullable String input) {
        if (!map.containsKey(input)) return null;
        return map.get(input).get();
    }
}
