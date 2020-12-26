package kr.syeyoung.dungeonsguide.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class Config {
    public static JsonObject configuration;


    public static void loadConfig(File f) throws FileNotFoundException {
        configuration = (JsonObject) new JsonParser().parse(new InputStreamReader(new FileInputStream(f)));


    }

}
