package kr.syeyoung.dungeonsguide.mod.dungeon;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.events.listener.DungeonListener;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.ThreadContext;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DungeonFacade {

    @Getter
    @Setter
    private DungeonContext context;

    public void init() {
        try {
            DungeonRoomInfoRegistry.loadAll(Main.getConfigDir());
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException |
                 IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
}
