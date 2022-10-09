package kr.syeyoung.dungeonsguide.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraftforge.fml.common.eventhandler.Event;

@Data
@AllArgsConstructor
public class StompDiedEvent extends Event {
    int code;
    String reason;
    boolean remote;
}
