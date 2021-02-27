package kr.syeyoung.dungeonsguide.features.listener;

import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraftforge.client.event.RenderLivingEvent;
import scala.tools.nsc.doc.base.comment.Title;

public interface TitleListener {
    void onTitle(S45PacketTitle renderPlayerEvent);
}
