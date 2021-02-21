package kr.syeyoung.dungeonsguide.dungeon.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DungeonPuzzleFailureEvent implements DungeonEventData {
    private String playerName;
    private String message;
    @Override
    public String getEventName() {
        return "PUZZLE_FAILURE";
    }
}
