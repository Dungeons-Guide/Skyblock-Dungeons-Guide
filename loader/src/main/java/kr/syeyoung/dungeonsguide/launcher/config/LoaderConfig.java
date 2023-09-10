package kr.syeyoung.dungeonsguide.launcher.config;

import kr.syeyoung.dungeonsguide.launcher.Main;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class LoaderConfig {

    public static boolean authErrorsDisabled = true;
    public static void init(FMLPreInitializationEvent event) {
        System.out.println("Handling Config");

        File f = new File(Main.getConfigDir(), "loader.cfg");
        Configuration configuration = new Configuration(f);

        configuration.addCustomCategoryComment("Errors", "Allows you to disable certain error notifications");
        authErrorsDisabled = configuration.getBoolean("Auth", "Errors", true, "True for disabled auth errors");

        // Save config because... well to generate it
        try {
            configuration.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
