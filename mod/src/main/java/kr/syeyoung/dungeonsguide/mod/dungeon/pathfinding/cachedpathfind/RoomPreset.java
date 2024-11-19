package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSettings;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Data;
import lombok.Getter;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.*;
import java.util.*;

@Getter
public class RoomPreset implements Cloneable {
    private PathfindPreset parent;
    private UUID roomId;

    private AlgorithmSettings algorithmSettings;
    private List<String> precalculations = new ArrayList<>();

    public RoomPreset(PathfindPreset parent, UUID roomId) {
        this.parent = parent;
        this.roomId = roomId;

        this.algorithmSettings = null;
    }

    public void addPrecalculation(String precalculation) {
        this.precalculations.add(precalculation);
        parent.markDirty();
    }
    public void removePrecalculation(String precalculation) {
        this.precalculations.remove(precalculation);
        parent.markDirty();
    }

    public List<String> getPrecalculations() {
        return Collections.unmodifiableList(precalculations);
    }

    public AlgorithmSettings getAlgorithmSettings() {
        return algorithmSettings == null ? parent.getAlgorithmSettings() : algorithmSettings;
    }

    public boolean isOverridingParentAlgorithmSettings() {
        return algorithmSettings != null;
    }

    protected void setParent(PathfindPreset parent) {
        this.parent = parent;
    }

    public static RoomPreset loadFromJson(PathfindPreset parent, JsonObject jsonObject) {
        RoomPreset roomPreset = new RoomPreset(parent, UUID.fromString(jsonObject.get("id").getAsString()));
        for (JsonElement element : jsonObject.get("precalculations").getAsJsonArray()) {
            roomPreset.precalculations.add(element.getAsString());
        }
//        roomPreset.tspCache = jsonObject.get("tspCache").getAsString();
        String algoSettings = jsonObject.get("algorithmSettings").getAsString();
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(algoSettings));
        DataInputStream dataInputStream = new DataInputStream(bais);
        try {
            roomPreset.algorithmSettings = AlgorithmSettings.deserialize(dataInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
//        res.addProperty("tspCache", tspCache);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(baos);
            CompressedStreamTools.write(algorithmSettings.serializeToNBT(), dataOutputStream);
            dataOutputStream.flush();
            String algoSettings = Base64.getEncoder().encodeToString(baos.toByteArray());

            res.addProperty("algorithmSettings", algoSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    @Override
    public RoomPreset clone() {
        try {
            RoomPreset roomPreset = (RoomPreset) super.clone();
            roomPreset.precalculations = new ArrayList<>(this.precalculations);
//            roomPreset.tspCache = null;
            roomPreset.parent = null;
            roomPreset.algorithmSettings = this.algorithmSettings == null ? null : this.algorithmSettings.clone();
            return roomPreset;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
