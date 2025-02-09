package kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSettingRegistry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.*;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class PathfindPreset implements Cloneable {
    private String presetName;
    @Setter
    private String presetId;
    private Instant generatedAt;
    private String origin;
    private boolean editable;

    @Setter
    private File file;
    private boolean dirty = false;

    private AlgorithmSetting algorithmSetting;
    private Map<UUID, RoomPreset> presets = new HashMap<>();


    public PathfindPreset() {
        this.presetId = UUID.randomUUID().toString();
        this.presetName = presetId;
        this.generatedAt = Instant.now();
        this.origin = "Manually Generated";
        this.file = new File(Main.getConfigDir(), "presets/"+presetId+".json");
        this.dirty = true;
        this.editable = true;

        this.algorithmSetting = AlgorithmSettingRegistry.STANDARD_DEFAULT_ALGORITHM_SETTING;

        for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
            presets.put(dungeonRoomInfo.getUuid(), new RoomPreset(this, dungeonRoomInfo.getUuid()));
        }
    }

    public RoomPreset getRoomPreset(UUID uuid) {
        if (presets.containsKey(uuid)) return presets.get(uuid);
        return new RoomPreset(this, uuid);
    }

    private PathfindPreset(String dummy) {}


    public void setPresetName(String presetName) {
        this.presetName = presetName;
        markDirty();
    }

    public void setAlgorithmSetting(AlgorithmSetting algorithmSetting) {
        this.algorithmSetting = algorithmSetting;
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
    public static PathfindPreset loadFromStream(InputStream in) throws IOException {
        try (InputStream inputStream = in) {
            JsonObject jsonObject = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(inputStream)), JsonObject.class);
            return loadFromJson(jsonObject);
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

        String algoSettings = jsonObject.get("algorithmSetting").getAsString();
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(algoSettings));
        DataInputStream dataInputStream = new DataInputStream(bais);
        try {
            preset.algorithmSetting = AlgorithmSetting.deserialize(dataInputStream);
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
            CompressedStreamTools.write(algorithmSetting.serializeToNBT(), dataOutputStream);
            dataOutputStream.flush();
            String algoSettings = Base64.getEncoder().encodeToString(baos.toByteArray());

            jsonObject.addProperty("algorithmSetting", algoSettings);
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
            preset.algorithmSetting = algorithmSetting;
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
