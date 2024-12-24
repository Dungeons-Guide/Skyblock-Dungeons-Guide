package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSetting;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MigrationUtils {
    public static PathfindPrecalculation migrate(File f, File f2, String migrationId) throws IOException, NoSuchAlgorithmException {
        PathfindCache cache = new PathfindCache(f);
        MessageDigest md = MessageDigest.getInstance("MD5");
        String hash = Hex.encodeHexString(md.digest(cache.getId().getBytes()));
        md = MessageDigest.getInstance("MD5");
        String id2 = Hex.encodeHexString(md.digest((f.getName()+"/"+cache.getId()+"/"+cache.getAlgorithmSetting().toString()).getBytes()));

        //

        try (FileOutputStream fos = new FileOutputStream(f2)) {
            DataOutputStream dataOutputStream = new DataOutputStream(fos);
            dataOutputStream.writeUTF("R2DGPF");
            dataOutputStream.writeInt(1);
            dataOutputStream.writeUTF(id2);
            dataOutputStream.writeUTF(hash);
            dataOutputStream.writeUTF(cache.getId());
            dataOutputStream.writeUTF(cache.getRoomId().toString());
            dataOutputStream.writeUTF(cache.getId().split(":")[1]);
            dataOutputStream.writeUTF("Migration "+migrationId);
            dataOutputStream.writeUTF("ALGO");
            AlgorithmSetting algorithmSetting = cache.getAlgorithmSetting();
            NBTTagCompound nbtTagCompound = algorithmSetting.serializeToNBT();
            CompressedStreamTools.write(nbtTagCompound, dataOutputStream);
            dataOutputStream.writeUTF("TRGT");
            dataOutputStream.writeInt(cache.getTargets().size());
            for (OffsetVec3 target : cache.getTargets()) {
                dataOutputStream.writeInt((int) (target.xCoord * 2));
                dataOutputStream.writeInt((int) (target.yCoord * 2 + 140));
                dataOutputStream.writeInt((int) (target.zCoord * 2));
            }
            dataOutputStream.writeUTF("NODE");
            dataOutputStream.writeBoolean(true);

            try (FileInputStream fileInputStream = new FileInputStream(f)) {
                fileInputStream.skip(cache.getGzipStart());
                IOUtils.copy(fileInputStream, dataOutputStream);
            }
            dataOutputStream.flush();
            dataOutputStream.close();
        }
        return new PathfindPrecalculation(f2);
    }
}
