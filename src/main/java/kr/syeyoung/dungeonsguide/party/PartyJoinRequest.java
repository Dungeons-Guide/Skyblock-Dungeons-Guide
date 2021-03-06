package kr.syeyoung.dungeonsguide.party;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordUser;

import java.awt.*;
import java.time.Instant;

@Data
public class PartyJoinRequest {
    private DiscordUser discordUser;
    private long expire;

    private Rectangle wholeRect = new Rectangle();
    private Rectangle acceptRect = new Rectangle();
    private Rectangle denyRect = new Rectangle();
    private Rectangle ignoreRect = new Rectangle();

    private int ttl = -1;
    private Reply reply;

    @AllArgsConstructor
    public static enum Reply {
        ACCEPT("Accepted"), DENY("Denied"), IGNORE("Ignored");

        @Getter
        private String past;
    }
}
