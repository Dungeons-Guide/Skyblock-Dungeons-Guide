package kr.syeyoung.dungeonsguide.launcher.branch;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.DGResponse;
import kr.syeyoung.dungeonsguide.launcher.exceptions.AssetNotFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.NoVersionFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ResponseParsingException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UpdateRetrieverUtil {
    private static String getResponse(HttpsURLConnection connection) throws IOException {
        connection.getResponseCode();
        InputStream toRead = connection.getErrorStream();
        if (toRead == null)
            toRead = connection.getInputStream();
        String payload = IOUtils.readLines(toRead).stream().collect(Collectors.joining("\n"));
        return payload;
    }

    public static List<UpdateBranch> getUpdateBranches() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(Main.DOMAIN + "/updates/").openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        JSONArray jsonArray = new JSONArray(getResponse(connection));
        return jsonArray.toList()
                .stream()
                    .map(a -> (JSONObject)a)
                .map(a -> {
                    UpdateBranch updateBranch = new UpdateBranch();
                    updateBranch.setId(a.getLong("id"));
                    updateBranch.setName(a.getString("name"));
                    updateBranch.setMetadata(a.getJSONObject("metadata"));
                    return updateBranch;
                }).collect(Collectors.toList());
    }

    public static List<Update> getLatestUpdates(long branchId, int page) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(Main.DOMAIN + "/updates/"+branchId+"/?page="+page).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        JSONArray jsonArray = new JSONArray(getResponse(connection));
        return jsonArray.toList()
                .stream()
                .map(a -> (JSONObject)a)
                .map(a -> {
                    Update update = new Update();
                    update.setId(a.getLong("id"));
                    update.setBranchId(a.getLong("branchId"));
                    update.setName(a.getString("name"));
                    update.setUpdateLog(a.getString("updateLog"));
                    update.setMetadata(a.getJSONObject("metadata"));
                    update.setAssets(a.getJSONObject("assets").getJSONArray("assets").toList().stream().map(b -> (JSONObject)b)
                            .map(b -> {
                                Update.Asset asset = new Update.Asset();
                                asset.setName(b.getString("name"));
                                asset.setAssetId(UUID.fromString(b.getString("assetId")));
                                asset.setSize(b.getLong("size"));
                                asset.setObjectId(b.getString("objectId"));
                                return asset;
                            }).collect(Collectors.toList()));
                    update.setReleaseDate(Instant.parse(a.getString("releaseDate")));
                    return update;
                }).collect(Collectors.toList());
    }

    public static Update getUpdate(long branchId, long updateId) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(Main.DOMAIN + "/updates/"+branchId+"/"+updateId).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        JSONObject a = new JSONObject(getResponse(connection));

        Update update = new Update();
        update.setId(a.getLong("id"));
        update.setBranchId(a.getLong("branchId"));
        update.setName(a.getString("name"));
        update.setUpdateLog(a.getString("updateLog"));
        update.setMetadata(a.getJSONObject("metadata"));
        update.setAssets(a.getJSONObject("assets").getJSONArray("assets").toList().stream().map(b -> (JSONObject)b)
                .map(b -> {
                    Update.Asset asset = new Update.Asset();
                    asset.setName(b.getString("name"));
                    asset.setAssetId(UUID.fromString(b.getString("assetId")));
                    asset.setSize(b.getLong("size"));
                    asset.setObjectId(b.getString("objectId"));
                    return asset;
                }).collect(Collectors.toList()));
        update.setReleaseDate(Instant.parse(a.getString("releaseDate")));
        return update;
    }

    public static InputStream downloadFile(Update update, String assetName) throws IOException {
        Update.Asset asset = update.getAssets().stream().filter(a -> a.getName().equals(assetName))
                .findFirst().orElseThrow(() -> new AssetNotFoundException(update.getBranchId()+"", update.getId()+"("+update.getName()+")", assetName));


        HttpsURLConnection connection = (HttpsURLConnection) new URL(Main.DOMAIN + "/updates/"+update.getBranchId()+"/"+update.getId()+"/"+asset.getAssetId()).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        JSONObject result = new JSONObject(getResponse(connection));
        String url = result.getString("url");
        String method = result.getString("method");

        connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        connection.setRequestMethod(method);
        return connection.getInputStream();
    }

    @Data @Builder
    public static class VersionInfo {
        String friendlyBranchName = "";
        long branchId;
        String friendlyVersionName = "";
        long updateId;
    }
    public static VersionInfo getIds(String branch, String version) throws IOException {
        long branchId = -1, updateId = -1;
        UpdateBranch branch1 = null;
            for (UpdateBranch updateBranch : UpdateRetrieverUtil.getUpdateBranches()) {
                if (updateBranch.getName().equals(branch) || (branch.equals("$default") &&
                        Optional.ofNullable(updateBranch.getMetadata())
                                .map(a -> a.getJSONObject("additionalMeta"))
                                .map(a -> a.getBoolean("defaultMod")).orElse(false))) {
                    branchId = updateBranch.getId();
                    branch1 = updateBranch;
                    break;
                }
            }
        if (branchId == -1) return null;

        Update target = null;
        int page = 0;
        while (updateId == -1) {
            List<Update> updateList = UpdateRetrieverUtil.getLatestUpdates(branchId, page++);
            if (updateList == null || updateList.isEmpty()) return null;
            for (Update update : updateList) {
                if (update.getName().equals(version) || version.equals("latest")) { // if latest, get the first one.
                    updateId = update.getId();
                    target = update;
                    break;
                }
            }
        }


        return VersionInfo.builder()
                .branchId(branchId)
                .updateId(updateId)
                .friendlyBranchName(branch1.getName())
                .friendlyVersionName(target.getName())
                .build();
    }
}
