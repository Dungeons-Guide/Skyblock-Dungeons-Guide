package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.UEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @AllArgsConstructor
public class PlayerNameFormatEvent extends UEvent  {
    public final UEntityPlayer player;
    public String displayName;
    public final String username;
    public final List<Component> prefix = new ArrayList<>();
}
