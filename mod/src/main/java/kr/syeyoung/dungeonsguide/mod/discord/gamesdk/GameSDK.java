/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.discord.gamesdk;

import com.sun.jna.*;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.GameSDKTypeMapper;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.NativeGameSDK;
import lombok.Getter;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;

public class GameSDK {
    private static final Logger logger = LogManager.getLogger("DG-GameSdkLoader");
    @Getter
    private static NativeGameSDK nativeGameSDK;

    static {
        try {
            if (System.getProperty("dg.safe") == null) extractLibrary();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extracted;
    public static void extractLibrary() throws IOException {
        String libName = System.mapLibraryName("discord_game_sdk");
        String dir = Platform.ARCH;

        if (!(dir.equals("aarch64") || dir.equals("x86") || dir.equals("x86_64"))) {
            if (Platform.is64Bit()) {
                dir = "x86_64";
            } else {
                dir = "x86";
            }
        }

        String resourceLoc = "/gamesdk/"+dir+"/"+libName;

        logger.info("Extracting GameSdk from "+resourceLoc);
        logger.info("Arch: "+Platform.ARCH +" | OS: "+Platform.getOSType());

        File targetExtractionPath = new File("native/"+libName);
        targetExtractionPath.getParentFile().mkdirs();
        try (InputStream is = GameSDK.class.getResourceAsStream(resourceLoc)) {
            Files.copy(is, targetExtractionPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            targetExtractionPath.deleteOnExit();
        }

        extracted = targetExtractionPath.getAbsolutePath();

        nativeGameSDK = (NativeGameSDK) Native.loadLibrary(targetExtractionPath.getAbsolutePath(), NativeGameSDK.class,
                Collections.singletonMap(Library.OPTION_TYPE_MAPPER, GameSDKTypeMapper.INSTANCE));
    }

    public static void cleanup() {
        nativeGameSDK = null;
        NativeLibrary.getInstance(extracted).close(); // <3 new jna
    }

    public static void writeString(byte[] bts, String str) {
        System.arraycopy(str.getBytes(), 0, bts, 0, str.getBytes().length);
        bts[str.getBytes().length] = 0;
    }
    public static String readString(byte[] bts) {
        int i;
        for (i = 0; i < bts.length && bts[i] != 0; i++);
        byte[] asdasd = new byte[i];
        System.arraycopy(bts, 0, asdasd, 0, i);
        return new String(asdasd);
    }
}
