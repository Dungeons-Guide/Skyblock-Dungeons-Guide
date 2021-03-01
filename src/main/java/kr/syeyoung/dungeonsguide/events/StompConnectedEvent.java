package kr.syeyoung.dungeonsguide.events;

import kr.syeyoung.dungeonsguide.stomp.StompInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraftforge.fml.common.eventhandler.Event;

@Data
@AllArgsConstructor
public class StompConnectedEvent extends Event {
    private StompInterface stompInterface;
}
