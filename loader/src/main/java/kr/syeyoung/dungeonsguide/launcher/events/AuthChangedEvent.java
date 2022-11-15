package kr.syeyoung.dungeonsguide.launcher.events;


import kr.syeyoung.dungeonsguide.launcher.auth.token.AuthToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.Event;

@AllArgsConstructor @Getter
public class AuthChangedEvent extends Event {
    private AuthToken authToken;
}
