package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.events.listener.DungeonListener;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.common.MinecraftForge;

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
        DungeonListener dgEventListener = new DungeonListener();
        MinecraftForge.EVENT_BUS.register(dgEventListener);

        try {
            DungeonRoomInfoRegistry.loadAll(Main.getConfigDir());
        } catch (BadPaddingException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException |
                 IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
}
