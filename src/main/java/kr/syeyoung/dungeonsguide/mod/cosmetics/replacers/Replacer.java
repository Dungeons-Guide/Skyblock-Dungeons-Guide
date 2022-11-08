package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers;

import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class Replacer {
    protected final CosmeticsManager cosmeticsManager;

    protected Replacer(CosmeticsManager cosmeticsManager) {
        this.cosmeticsManager = cosmeticsManager;
    }

    public abstract void consumeEvent(Event e);

}
