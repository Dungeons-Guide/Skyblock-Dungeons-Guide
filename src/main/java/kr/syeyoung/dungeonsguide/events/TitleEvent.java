package kr.syeyoung.dungeonsguide.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraftforge.fml.common.eventhandler.Event;

@Data
@AllArgsConstructor
public class TitleEvent extends Event {
    S45PacketTitle packetTitle;
}
