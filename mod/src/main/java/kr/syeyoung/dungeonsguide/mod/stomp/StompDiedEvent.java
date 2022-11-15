package kr.syeyoung.dungeonsguide.mod.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraftforge.fml.common.eventhandler.Event;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class StompDiedEvent extends Event {
    int code;
    String reason;
    boolean remote;
}
