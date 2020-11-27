package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WaterCondition {
    private String blockId;
    private boolean requiredState;

    public WaterCondition invert() {
        return new WaterCondition(blockId, !requiredState);
    }
}
