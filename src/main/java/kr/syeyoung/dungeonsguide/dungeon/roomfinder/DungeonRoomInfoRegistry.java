package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import com.google.common.io.Files;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DungeonRoomInfoRegistry {
    private static final List<DungeonRoomInfo> registered = new ArrayList<DungeonRoomInfo>();
    private static final Map<Short, List<DungeonRoomInfo>> shapeMap = new HashMap<Short, List<DungeonRoomInfo>>();
    private static final Map<UUID, DungeonRoomInfo> uuidMap = new HashMap<UUID, DungeonRoomInfo>();

    public static void register(DungeonRoomInfo dungeonRoomInfo) {
        if (dungeonRoomInfo == null) throw new NullPointerException("what the fak parameter is noll?");
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
        if (roomInfos == null) roomInfos = new ArrayList<DungeonRoomInfo>();
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
        String nameidstring = "name,uuid,processsor,secrets";
        String ids = "";
        for (DungeonRoomInfo dungeonRoomInfo : registered) {
            try {
                if (!dungeonRoomInfo.isUserMade() && !isDev) continue;
                FileOutputStream fos = new FileOutputStream(new File(dir, dungeonRoomInfo.getUuid().toString() + ".roomdata"));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(dungeonRoomInfo);
                oos.flush();
                oos.close();

                nameidstring += "\n"+dungeonRoomInfo.getName()+","+dungeonRoomInfo.getUuid() +","+dungeonRoomInfo.getProcessorId()+","+dungeonRoomInfo.getTotalSecrets();
                ids += "roomdata/"+dungeonRoomInfo.getUuid() +".roomdata\n";
            } catch (Exception e) {e.printStackTrace();}
        }

        try {
            Files.write(nameidstring, new File(dir, "roomidmapping.csv"), Charset.defaultCharset());
            Files.write(ids, new File(dir, "datas.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadAll(File dir) throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {
        registered.clear();
        shapeMap.clear();
        uuidMap.clear();
        URL url = new URL("z:///roomdata/datas.txt");
        List<String> lines = IOUtils.readLines(url.openConnection().getInputStream());
        for (String name : lines) {
            if (!name.endsWith(".roomdata")) continue;
            try {
                InputStream fis = new URL("z:///"+name).openStream();
                ObjectInputStream ois = new ObjectInputStream(fis);
                DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                ois.close();
                fis.close();
                register(dri);
            } catch (Exception e) {e.printStackTrace();}
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
            } catch (Exception e) {e.printStackTrace();}
        }
    }

}
