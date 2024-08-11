/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod;

import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {

    public static void extractLibraryAndLoad(String name) throws IOException {
        String libName = System.mapLibraryName(name);
        String dir = Platform.ARCH;

        if (!(dir.equals("aarch64") || dir.equals("x86") || dir.equals("x86_64"))) {
            if (Platform.is64Bit()) {
                dir = "x86_64";
            } else {
                dir = "x86";
            }
        }

        String resourceLoc = "/native/"+dir+"/"+libName;

        System.out.println("Extracting "+name+" from "+resourceLoc);
        System.out.println("Arch: "+Platform.ARCH +" | OS: "+Platform.getOSType());

        File targetExtractionPath = new File("native/"+libName);
        targetExtractionPath.getParentFile().mkdirs();
        try (InputStream is = NativeLoader.class.getResourceAsStream(resourceLoc)) {
            Files.copy(is, targetExtractionPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            targetExtractionPath.deleteOnExit();
        }

        String extracted = targetExtractionPath.getAbsolutePath();

        System.load(extracted);
    }
}