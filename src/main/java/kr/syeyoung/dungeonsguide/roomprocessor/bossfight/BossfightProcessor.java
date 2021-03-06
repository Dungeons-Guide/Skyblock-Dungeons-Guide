package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;

import java.util.List;

public interface BossfightProcessor extends RoomProcessor {
    List<String> getPhases();
    String getCurrentPhase();
    List<String> getNextPhases();

    List<HealthData> getHealths();

    String getBossName();
}