package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSettings;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Data;
import lombok.Getter;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.*;
import java.time.Instant;
import java.util.*;

@Getter
public class PathfindPreset implements Cloneable {
    private String presetName;
    private String presetId;
    private Instant generatedAt;
    private String origin;
    private boolean editable;

    private File file;
    private boolean dirty = false;

    private AlgorithmSettings algorithmSettings;
    private Map<UUID, RoomPreset> presets = new HashMap<>();


    public PathfindPreset() {
        this.presetId = UUID.randomUUID().toString();
        this.presetName = presetId;
        this.generatedAt = Instant.now();
        this.origin = "Manually Generated";
        this.file = new File(Main.getConfigDir(), "presets/"+presetId+".json");
        this.dirty = true;
        this.editable = true;

        this.algorithmSettings = FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings();

        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
            presets.put(dungeonRoomInfo.getUuid(), new RoomPreset(this, dungeonRoomInfo.getUuid()));
        }
    }

    private PathfindPreset(String dummy) {}


    public void setPresetName(String presetName) {
        this.presetName = presetName;
        markDirty();
    }

    public void setAlgorithmSettings(AlgorithmSettings algorithmSettings) {
        this.algorithmSettings = algorithmSettings;
        markDirty();
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        markDirty();
    }

    public void setOrigin(String origin) {
        this.origin = origin;
        markDirty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void save() throws IOException {
        if (!dirty) return;

        try (OutputStream outputStream = new FileOutputStream(file)) {
            JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(new BufferedOutputStream(outputStream)));
            new Gson().toJson(saveToJson(), jsonWriter);
            jsonWriter.flush();
            dirty = false;
        }
    }

    public static PathfindPreset loadFromFile(File f) throws IOException {
        try (InputStream inputStream = new FileInputStream(f)) {
            JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(inputStream)), JsonObject.class);
            PathfindPreset preset = loadFromJson(jsonObject);
            preset.file = f;
            return preset;
        }
    }

    public static PathfindPreset loadFromJson(JsonObject jsonObject) {
        PathfindPreset preset = new PathfindPreset("");
        preset.presetName = jsonObject.get("presetName").getAsString();
        preset.presetId = jsonObject.get("presetId").getAsString();
        preset.generatedAt = Instant.ofEpochMilli(jsonObject.get("generatedAt").getAsLong());
        preset.origin = jsonObject.get("origin").getAsString();
        preset.dirty = false;
        preset.editable = jsonObject.get("editable").getAsBoolean();

        String algoSettings = jsonObject.get("algorithmSettings").getAsString();
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(algoSettings));
        DataInputStream dataInputStream = new DataInputStream(bais);
        try {
            preset.algorithmSettings = AlgorithmSettings.deserialize(dataInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (JsonElement rooms : jsonObject.getAsJsonArray("rooms")) {
            RoomPreset roomPreset =  RoomPreset.loadFromJson(preset, rooms.getAsJsonObject());
            preset.presets.put(roomPreset.getRoomId(), roomPreset);
        }
        return preset;
    }

    public JsonObject saveToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("presetName", presetName);
        jsonObject.addProperty("presetId", presetId);
        jsonObject.addProperty("generatedAt", generatedAt.toEpochMilli());
        jsonObject.addProperty("origin", origin);
        jsonObject.addProperty("editable", editable);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(baos);
            CompressedStreamTools.write(algorithmSettings.serializeToNBT(), dataOutputStream);
            dataOutputStream.flush();
            String algoSettings = Base64.getEncoder().encodeToString(baos.toByteArray());

            jsonObject.addProperty("algorithmSettings", algoSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonArray rooms = new JsonArray();
        for (Map.Entry<UUID, RoomPreset> entry : presets.entrySet()) {
            rooms.add(entry.getValue().saveToJson());
        }
        jsonObject.add("rooms", rooms);
        return jsonObject;
    }

    @Override
    public PathfindPreset clone() {
        try {
            PathfindPreset preset = (PathfindPreset) super.clone();
            preset.algorithmSettings = algorithmSettings;
            preset.presets = new HashMap<>();
            preset.presetId = UUID.randomUUID().toString();
            preset.presetName = "Clone of " +presetName;
            preset.generatedAt = Instant.now();
            preset.dirty = true;
            preset.editable = true;
            preset.origin = "Clone of "+presetName+"("+presetId+")";

            preset.file = new File(Main.getConfigDir(), "presets/"+preset.presetId.toString()+".json");

            for (Map.Entry<UUID, RoomPreset> uuidRoomPresetEntry : presets.entrySet()) {
                RoomPreset roomPreset = uuidRoomPresetEntry.getValue().clone();
                roomPreset.setParent(preset);
                preset.presets.put(uuidRoomPresetEntry.getKey(), roomPreset);
            }
            return preset;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


}
