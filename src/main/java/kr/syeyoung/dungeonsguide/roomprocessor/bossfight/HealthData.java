package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthData {
    private String name;
    private int health;
    private int maxHealth;
}
