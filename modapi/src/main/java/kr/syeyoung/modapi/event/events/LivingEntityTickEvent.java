package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class LivingEntityTickEvent extends UEvent {
    private UEntityLiving entityLiving;
}
