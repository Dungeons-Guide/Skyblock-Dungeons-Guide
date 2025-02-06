package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.abilitysetting.AlgorithmSetting;
import lombok.Getter;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.*;
import java.util.*;

@Getter
public class RoomPreset implements Cloneable {
    private PathfindPreset parent;
    private UUID roomId;

    private AlgorithmSetting algorithmSetting;
    private Set<String> precalculations = new HashSet<>();
    private String tspCache;

    public RoomPreset(PathfindPreset parent, UUID roomId) {
        this.parent = parent;
        this.roomId = roomId;

        this.algorithmSetting = null;
    }

    public void addPrecalculation(String precalculation) {
        this.precalculations.add(precalculation);
        parent.markDirty();
    }
    public void removePrecalculation(String precalculation) {
        this.precalculations.remove(precalculation);
        parent.markDirty();
    }

    public Set<String> getPrecalculations() {
        return Collections.unmodifiableSet(precalculations);
    }

    public AlgorithmSetting getAlgorithmSetting() {
        return algorithmSetting == null ? parent.getAlgorithmSetting() : algorithmSetting;
    }

    public AlgorithmSetting getEffectiveAlgorithmSetting(DungeonRoomInfo info) {
        if (info.getColor() == 62)
            return getAlgorithmSetting()
                    .withRouteEtherwarp(false)
                    .withStonkTeleport(false);
        else
            return getAlgorithmSetting();
    }

    public AlgorithmSetting getAlgorithmSettingOverride() {
        return algorithmSetting;
    }

    public void setAlgorithmSettingOverride(AlgorithmSetting algorithmSetting) {
        this.algorithmSetting = algorithmSetting;
    }

    public boolean isOverridingParentAlgorithmSetting() {
        return algorithmSetting != null;
    }

    protected void setParent(PathfindPreset parent) {
        this.parent = parent;
    }

    public static RoomPreset loadFromJson(PathfindPreset parent, JsonObject jsonObject) {
        RoomPreset roomPreset = new RoomPreset(parent, UUID.fromString(jsonObject.get("id").getAsString()));
        for (JsonElement element : jsonObject.get("precalculations").getAsJsonArray()) {
            roomPreset.precalculations.add(element.getAsString());
        }
        if (jsonObject.has("tspCache"))
            roomPreset.tspCache = jsonObject.get("tspCache").isJsonNull() ? null : jsonObject.get("tspCache").getAsString();

        if (jsonObject.has("algorithmSetting")) {

            String algoSettings = jsonObject.get("algorithmSetting").getAsString();
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(algoSettings));
            DataInputStream dataInputStream = new DataInputStream(bais);
            try {
                roomPreset.algorithmSetting = AlgorithmSetting.deserialize(dataInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            roomPreset.algorithmSetting = null;
        }
        return roomPreset;
    }

    public JsonObject saveToJson() {
        JsonObject res = new JsonObject();
        JsonArray array = new JsonArray();
        for (String precalculation : precalculations) {
            array.add(new JsonPrimitive(precalculation));
        }
        res.addProperty("id", roomId.toString());
        res.add("precalculations", array);
        res.addProperty("tspCache", tspCache);

        try {
            if (algorithmSetting != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(baos);
                CompressedStreamTools.write(algorithmSetting.serializeToNBT(), dataOutputStream);
                dataOutputStream.flush();
                String algoSettings = Base64.getEncoder().encodeToString(baos.toByteArray());
                res.addProperty("algorithmSetting", algoSettings);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    @Override
    public RoomPreset clone() {
        try {
            RoomPreset roomPreset = (RoomPreset) super.clone();
            roomPreset.precalculations = new HashSet<>(this.precalculations);
//            roomPreset.tspCache = null;
            roomPreset.parent = null;
            roomPreset.algorithmSetting = this.algorithmSetting;
            return roomPreset;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
