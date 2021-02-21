package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeverState {
    private String blockId;
    private boolean requiredState;

    public LeverState invert() {
        return new LeverState(blockId, !requiredState);
    }
}
