package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class EntityEnterWorldEvent extends UEvent {
    private UEntity entity;
}
