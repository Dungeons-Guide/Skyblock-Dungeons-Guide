package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.item.UItemStack;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor @Getter
public class ItemTooltipEvent extends UEvent {
    public final boolean showAdvancedItemTooltips;
    public final UItemStack itemStack;
    public final List<String> toolTip;
}
