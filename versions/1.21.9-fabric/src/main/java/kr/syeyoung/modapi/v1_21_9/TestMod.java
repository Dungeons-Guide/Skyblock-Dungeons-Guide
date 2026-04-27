package kr.syeyoung.modapi.v1_21_9;

import kr.syeyoung.dungeonsguide.loader.DGInterface;
import kr.syeyoung.dungeonsguide.loader.LoaderAPI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class TestMod  implements ClientModInitializer, LoaderAPI {

    @Override
    public void onInitializeClient() {


        ClientLifecycleEvents.CLIENT_STARTED.register((a) -> {
            try {
                DGInterface dgInterface = (DGInterface) Class.forName("kr.syeyoung.dungeonsguide.mod.DungeonsGuide").newInstance();
                Path p = FabricLoader.getInstance().getConfigDir().resolve("dungeonsguide");
                dgInterface.init(p.toFile(), this);
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });


    }
}
