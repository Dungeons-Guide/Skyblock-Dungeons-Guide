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

package kr.syeyoung.dungeonsguide.utils;

import kr.syeyoung.dungeonsguide.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TimeScoreUtil {
    private static final TreeMap<Integer, Integer> min8 = new TreeMap<Integer, Integer>();
    private static final TreeMap<Integer, Integer> min10 = new TreeMap<Integer, Integer>();
    private static final TreeMap<Integer, Integer> min12 = new TreeMap<Integer, Integer>();
    public static void init() {
        try {
            load("8.csv", min8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            load("10.csv", min10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            load("12.csv", min12);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load(String name, TreeMap<Integer, Integer> minutes) throws IOException {
        minutes.clear();
        List<String> lines = IOUtils.readLines(Main.class.getResourceAsStream("/timescore/"+name));
        for (String line:lines) {
            String[] split = line.split(",");
            minutes.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
    }

    public static int estimate(int mills, int drop) {
        if (drop == 600) return estimate(mills, min10);
        if (drop == 480) return estimate(mills, min8);
        if (drop == 720) return estimate(mills, min12);
        return -1;
    }

    private static int estimate(int mills, TreeMap<Integer, Integer> lookup_table){
        Map.Entry<Integer, Integer> high = lookup_table.ceilingEntry(mills);
        Map.Entry<Integer, Integer> low = lookup_table.floorEntry(mills);


        if (low == null && high == null) return 0;
        if (low == null) return high.getValue();
        if (high == null) return low.getValue();

        int distHigh = high.getKey() - mills;
        int distLow = mills - low.getKey();

        if (distHigh > distLow) return high.getValue();
        else return low.getValue();
    }
}
