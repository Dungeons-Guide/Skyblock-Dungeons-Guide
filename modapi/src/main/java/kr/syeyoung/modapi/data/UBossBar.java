package kr.syeyoung.modapi.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;

@AllArgsConstructor @Getter
public class UBossBar {
    private final Component name;
    private final float percent;
}
