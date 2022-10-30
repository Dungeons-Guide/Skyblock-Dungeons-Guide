/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder;

import com.google.common.io.Files;
import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DungeonRoomInfoRegistry {
    @Getter
    private static final List<DungeonRoomInfo> registered = new ArrayList<DungeonRoomInfo>();
    private static final Map<Short, List<DungeonRoomInfo>> shapeMap = new HashMap<Short, List<DungeonRoomInfo>>();
    private static final Map<UUID, DungeonRoomInfo> uuidMap = new HashMap<UUID, DungeonRoomInfo>();

    static Gson gson = new Gson();

    public static void register(@NotNull DungeonRoomInfo dungeonRoomInfo) {

//        System.out.println("Loading room: " + dungeonRoomInfo.getUuid());
//
//        File file = new File(Main.getConfigDir() + "/" + "rooms" + "/" + dungeonRoomInfo.getUuid() + ".json");
//        if(!file.exists()){
//            try {
//                FileUtils.writeStringToFile(file, gson.toJson(dungeonRoomInfo));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }



        if (uuidMap.containsKey(dungeonRoomInfo.getUuid())) {
            DungeonRoomInfo dri1 = uuidMap.get(dungeonRoomInfo.getUuid());
            registered.remove(dri1);
            shapeMap.get(dri1.getShape()).remove(dri1);
            uuidMap.remove(dri1.getUuid());
        }
        dungeonRoomInfo.setRegistered(true);
        registered.add(dungeonRoomInfo);
        uuidMap.put(dungeonRoomInfo.getUuid(), dungeonRoomInfo);
        List<DungeonRoomInfo> roomInfos = shapeMap.get(dungeonRoomInfo.getShape());
        if (roomInfos == null) {
            roomInfos = new ArrayList<>();
        }
        roomInfos.add(dungeonRoomInfo);
        shapeMap.put(dungeonRoomInfo.getShape(), roomInfos);
    }


    public static List<DungeonRoomInfo> getByShape(Short shape) {
        List<DungeonRoomInfo> dungeonRoomInfos = shapeMap.get(shape);
        return dungeonRoomInfos == null ? Collections.emptyList() : dungeonRoomInfos;
    }

    public static DungeonRoomInfo getByUUID(UUID uid) {
        return uuidMap.get(uid);
    }

    public static void unregister(DungeonRoomInfo dungeonRoomInfo) {
        if (!dungeonRoomInfo.isRegistered()) throw new IllegalStateException("what tha fak? that is not registered one");
        if (!uuidMap.containsKey(dungeonRoomInfo.getUuid())) throw new IllegalStateException("what tha fak? that is not registered one, but you desperately wanted to trick this program");
        dungeonRoomInfo.setRegistered(false);
        registered.remove(dungeonRoomInfo);
        shapeMap.get(dungeonRoomInfo.getShape()).remove(dungeonRoomInfo);
        uuidMap.remove(dungeonRoomInfo.getUuid());
    }

    public static void saveAll(File dir) {
        dir.mkdirs();
        boolean isDev = Minecraft.getMinecraft().getSession().getPlayerID().replace("-","").equals("e686fe0aab804a71ac7011dc8c2b534c");
        StringBuilder nameidstring = new StringBuilder("name,uuid,processsor,secrets");
        StringBuilder ids = new StringBuilder();
        for (DungeonRoomInfo dungeonRoomInfo : registered) {
            try {
                if (!dungeonRoomInfo.isUserMade() && !isDev) continue;
                FileOutputStream fos = new FileOutputStream(new File(dir, dungeonRoomInfo.getUuid().toString() + ".roomdata"));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(dungeonRoomInfo);
                oos.flush();
                oos.close();

                nameidstring.append("\n").append(dungeonRoomInfo.getName()).append(",").append(dungeonRoomInfo.getUuid()).append(",").append(dungeonRoomInfo.getProcessorId()).append(",").append(dungeonRoomInfo.getTotalSecrets());
                ids.append("roomdata/").append(dungeonRoomInfo.getUuid()).append(".roomdata\n");
            } catch (Exception e) {e.printStackTrace();}
        }

        try {
            Files.write(nameidstring.toString(), new File(dir, "roomidmapping.csv"), Charset.defaultCharset());
            Files.write(ids.toString(), new File(dir, "datas.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAll(File dir) throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {
        registered.clear();
        shapeMap.clear();
        uuidMap.clear();
        try {
            List<String> lines = IOUtils.readLines(Main.class.getResourceAsStream("/roomdata/datas.txt"));
            for (String name : lines) {
                if (!name.endsWith(".roomdata")) continue;
                try {
                    InputStream fis = Main.class.getResourceAsStream("/"+name);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                    ois.close();
                    fis.close();
                    register(dri);
                } catch (Exception e) {
                    System.out.println(name);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".roomdata")) continue;
            try {
                InputStream fis = new FileInputStream(f);
                ObjectInputStream ois = new ObjectInputStream(fis);
                DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                ois.close();
                fis.close();
                register(dri);
            } catch (Exception e) {
                System.out.println(f.getName());e.printStackTrace();}
        }
    }

}
