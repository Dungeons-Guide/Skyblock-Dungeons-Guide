package kr.syeyoung.dungeonsguide.dungeon.roomfinder;

import kr.syeyoung.dungeonsguide.Authenticator;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.DungeonsGuideMain;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DungeonRoomInfoRegistry {
    private static List<DungeonRoomInfo> registered = new ArrayList<DungeonRoomInfo>();
    private static Map<Short, List<DungeonRoomInfo>> shapeMap = new HashMap<Short, List<DungeonRoomInfo>>();
    private static Map<UUID, DungeonRoomInfo> uuidMap = new HashMap<UUID, DungeonRoomInfo>();

    public static void register(DungeonRoomInfo dungeonRoomInfo) {
        if (dungeonRoomInfo == null) throw new NullPointerException("what the fak parameter is noll?");
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
        return dungeonRoomInfos == null ? Collections.<DungeonRoomInfo>emptyList() : dungeonRoomInfos;
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
        if (!DungeonsGuide.DEBUG) return;
        dir.mkdirs();
        for (DungeonRoomInfo dungeonRoomInfo : registered) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(dir, dungeonRoomInfo.getUuid().toString() + ".roomdata"));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(dungeonRoomInfo);
                oos.flush();
                oos.close();
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    public static void loadAll() throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException {
        registered.clear();
        shapeMap.clear();
        uuidMap.clear();
        Authenticator authenticator = DungeonsGuideMain.getDungeonsGuideMain().getAuthenticator();
        InputStream inputStream = authenticator.getInputStream("roomdata/datas.txt");
        byte[] bytes = new byte[4];
        inputStream.read(bytes);

        int length = ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8 ) |
                ((bytes[3] & 0xFF));
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int totalLen = 0;
        try {
            byte[] buffer = new byte[128];
            int read = 0;
            while ( (read = inputStream.read(buffer)) != -1 ) {
                totalLen += read;
                byteStream.write(buffer, 0, read);
                if (totalLen >= length) break;;
            }
        } catch (Exception ignored) {}
        byte[] byte1 = byteStream.toByteArray();
        byte[] byte2 = new byte[(int) length];
        System.arraycopy(byte1, 0, byte2, 0, byte2.length);
        inputStream.close();

        String names = new String(byte2);

        for (String name  : names.split("\n")) {
            if (name.endsWith(".roomdata")) continue;
            try {
                InputStream fis = authenticator.getInputStream(name);
                fis.read(new byte[4]);
                ObjectInputStream ois = new ObjectInputStream(fis);
                DungeonRoomInfo dri = (DungeonRoomInfo) ois.readObject();
                ois.close();
                fis.close();
                register(dri);
            } catch (Exception e) {e.printStackTrace();}
        }
    }
}
