package kr.syeyoung.dungeonsguide.launcher.branch;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.exceptions.AssetNotFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.NoVersionFoundException;
import kr.syeyoung.dungeonsguide.launcher.exceptions.http.ResponseParsingException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class UpdateRetrieverUtil {
    private static String getResponse(HttpURLConnection connection) throws IOException {
        connection.getResponseCode();
        InputStream toRead = connection.getErrorStream();
        if (toRead == null)
            toRead = connection.getInputStream();
        return IOUtils.readLines(toRead).stream().collect(Collectors.joining("\n"));
    }

    public static List<UpdateBranch> getUpdateBranches() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(Main.DOMAIN + "/updates/").openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
        connection.setRequestProperty("Authorization", "Bearer "+ AuthManager.getInstance().getWorkingTokenOrThrow());
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(3000);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        String payload = getResponse(connection);
        try {
            JSONArray jsonArray = new JSONArray(payload);
            List<UpdateBranch> branches = new ArrayList<>();
            for (Object a_ : jsonArray) {
                JSONObject a = (JSONObject) a_;
                UpdateBranch updateBranch = new UpdateBranch();
                updateBranch.setId(a.getLong("id"));
                updateBranch.setName(a.getString("name"));
                updateBranch.setMetadata(a.getJSONObject("metadata"));
                branches.add(updateBranch);
            }
            return branches;
        } catch (Exception e) {
            throw new ResponseParsingException(payload, e);
        }
    }

    public static List<Update> getLatestUpdates(long branchId, int page) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(Main.DOMAIN + "/updates/"+branchId+"/?page="+page).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(3000);
        connection.setRequestProperty("Authorization", "Bearer "+ AuthManager.getInstance().getWorkingTokenOrThrow());
        connection.setDoInput(true);
        connection.setDoOutput(true);

        String payload = getResponse(connection);
        try {
            JSONArray jsonArray = new JSONArray(payload);
            List<Update> updates = new ArrayList<>();
            for (Object o_ : jsonArray) {
                JSONObject a = (JSONObject) o_;

                Update update = new Update();
                update.setId(a.getLong("id"));
                update.setBranchId(a.getLong("branchId"));
                update.setName(a.getString("versionName"));
                update.setUpdateLog(a.getString("updateLog"));
                update.setMetadata(a.getJSONObject("metadata"));
                update.setAssets(a.getJSONObject("assets").getJSONArray("assets")
                        .toList().stream().map(b -> (HashMap) b)
                        .map(b -> {
                            Update.Asset asset = new Update.Asset();
                            asset.setName((String) b.get("name"));
                            asset.setAssetId(UUID.fromString((String) b.get("assetId")));
                            asset.setSize((Integer) b.get("size"));
                            asset.setObjectId((String) b.get("objectId"));
                            return asset;
                        }).collect(Collectors.toList()));
                update.setReleaseDate(Instant.parse(a.getString("releaseDate")));
                updates.add(update);
            }
            return updates;
        } catch (Exception e) {
            throw new ResponseParsingException(payload, e);
        }
    }

    public static Update getUpdate(long branchId, long updateId) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(Main.DOMAIN + "/updates/"+branchId+"/"+updateId).openConnection();
        connection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
        connection.setRequestProperty("Authorization", "Bearer "+ AuthManager.getInstance().getWorkingTokenOrThrow());
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(3000);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        String payload = getResponse(connection);
        try {
            JSONObject a = new JSONObject(payload);

            Update update = new Update();
            update.setId(a.getLong("id"));
            update.setBranchId(a.getLong("branchId"));
            update.setName(a.getString("versionName"));
            update.setUpdateLog(a.getString("updateLog"));
            update.setMetadata(a.getJSONObject("metadata"));
            update.setAssets(a.getJSONObject("assets").getJSONArray("assets")
                    .toList().stream().map(b -> (HashMap) b)
                    .map(b -> {
                        Update.Asset asset = new Update.Asset();
                        asset.setName((String) b.get("name"));
                        asset.setAssetId(UUID.fromString((String) b.get("assetId")));
                        asset.setSize((Integer) b.get("size"));
                        asset.setObjectId((String) b.get("objectId"));
                        return asset;
                    }).collect(Collectors.toList()));
            update.setReleaseDate(Instant.parse(a.getString("releaseDate")));
            return update;
        } catch (Exception e) {
            throw new ResponseParsingException(payload, e);
        }
    }

    public static InputStream downloadFile(Update update, String assetName) throws IOException {
        Update.Asset asset = update.getAssets().stream().filter(a -> a.getName().equals(assetName))
                .findFirst().orElseThrow(() -> new AssetNotFoundException(update.getBranchId()+"", update.getId()+"("+update.getName()+")", assetName));


        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(Main.DOMAIN + "/updates/" + update.getBranchId() + "/" + update.getId() + "/" + asset.getAssetId()).openConnection();
            connection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + AuthManager.getInstance().getWorkingTokenOrThrow());
            connection.setDoInput(true);
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(3000);
            connection.setDoOutput(true);

            String payload = getResponse(connection);
            String url, method;
            try {
                JSONObject result = new JSONObject(payload);
                url = result.getString("url");
                method = result.getString("method");
            } catch (Exception e) {
                throw new ResponseParsingException(payload, e);
            }
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestProperty("User-Agent", "DungeonsGuide/4.0");
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod(method);
                return connection.getInputStream();
            } catch (IOException e) {
                throw new RuntimeException("Error occured while downloading update asset from "+method+" "+url, e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occured while downloading update asset "+update+"/"+assetName, e);
        }
    }

    @Data @Builder
    public static class VersionInfo {
        String friendlyBranchName = "";
        long branchId;
        String friendlyVersionName = "";
        long updateId;
    }
    public static VersionInfo getIds(String branch, String version) throws IOException, NoVersionFoundException {
        try {
            long branchId = -1, updateId = -1;
            UpdateBranch branch1 = null;
            List<UpdateBranch> branches = UpdateRetrieverUtil.getUpdateBranches();
            for (UpdateBranch updateBranch : branches) {
                if (updateBranch.getName().equals(branch) || (branch.equals("$default") &&
                        Optional.ofNullable(updateBranch.getMetadata())
                                .map(a -> a.isNull("additionalMeta") ? null : a.getJSONObject("additionalMeta"))
                                .map(a -> a.isNull("defaultMod") ? null : a.getBoolean("defaultMod")).orElse(false))) {
                    branchId = updateBranch.getId();
                    branch1 = updateBranch;
                    break;
                }
            }
            if (branchId == -1) {
                throw new NoVersionFoundException(branch, version,
                        branches.stream()
                                .map(a -> a.getName() +
                                        Optional.ofNullable(a.getMetadata()).map(b -> b.getJSONObject("additionalMeta")).map(b -> b.toString()).orElse(""))
                                .collect(Collectors.joining(", "))
                );
            }

            Update target = null;
            int page = 0;
            while (updateId == -1) {
                List<Update> updateList = UpdateRetrieverUtil.getLatestUpdates(branchId, page++);
                if (updateList == null || updateList.isEmpty()) {
                    throw new NoVersionFoundException(branch, version, "Unable to find version / branchId: " + branchId);
                }
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
        } catch (Exception e) {
            throw new NoVersionFoundException(branch, version, "Exception occured", e);
        }
    }
}
