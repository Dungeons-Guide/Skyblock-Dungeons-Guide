/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.authapi.branch;

import kr.syeyoung.dungeonsguide.authapi.auth.AuthManager;
import kr.syeyoung.dungeonsguide.authapi.exceptions.AssetNotFoundException;
import kr.syeyoung.dungeonsguide.authapi.exceptions.NoVersionFoundException;
import kr.syeyoung.dungeonsguide.authapi.exceptions.http.ResponseParsingException;
import kr.syeyoung.dungeonsguide.authapi.util.LetsEncrypt;
import lombok.Builder;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class UpdatesAPI {
    private final String userAgent;
    private final String baseUrl;
    private final AuthManager authManager;
    
    public UpdatesAPI(String baseUrl, String userAgent, AuthManager authManager) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.authManager = authManager;
    }
    
    private static String getResponse(HttpURLConnection connection) throws IOException {
        connection.getResponseCode();
        InputStream toRead = connection.getErrorStream();
        if (toRead == null)
            toRead = connection.getInputStream();
        return new BufferedReader(new InputStreamReader(toRead, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    public  List<UpdateBranch> getUpdateBranches() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(baseUrl + "/updates/").openConnection();
        connection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Authorization", "Bearer "+ authManager.getWorkingTokenOrThrow());
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
                updateBranch.setMetadata(JSONObject.NULL.equals(a.get("metadata")) ? new JSONObject() : a.getJSONObject("metadata"));
                if (JSONObject.NULL.equals(a.get("metadata") )) {
                    System.out.println("Update Branch has null metadata: "+a_);
                }
                branches.add(updateBranch);
            }
            return branches;
        } catch (Exception e) {
            throw new ResponseParsingException(payload, e);
        }
    }

    public  List<Update> getLatestUpdates(long branchId, int page) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(baseUrl + "/updates/"+branchId+"/?page="+page).openConnection();
        connection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(3000);
        connection.setRequestProperty("Authorization", "Bearer "+ authManager.getWorkingTokenOrThrow());
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

    public  Update getUpdate(long branchId, long updateId) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(baseUrl + "/updates/"+branchId+"/"+updateId).openConnection();
        connection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Authorization", "Bearer "+ authManager.getWorkingTokenOrThrow());
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

    public  InputStream downloadFile(Update update, String assetName) throws IOException {
        Update.Asset asset = update.getAssets().stream().filter(a -> a.getName().equals(assetName))
                .findFirst().orElseThrow(() -> new AssetNotFoundException(update.getBranchId()+"", update.getId()+"("+update.getName()+")", assetName));


        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(baseUrl + "/updates/" + update.getBranchId() + "/" + update.getId() + "/" + asset.getAssetId()).openConnection();
            connection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + authManager.getWorkingTokenOrThrow());
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
                connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
                connection.setRequestProperty("User-Agent", userAgent);
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod(method);
                return connection.getInputStream();
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while downloading update asset from "+method+" "+url, e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while downloading update asset "+update+"/"+assetName, e);
        }
    }

    @Data @Builder
    public  class VersionInfo {
        String friendlyBranchName = "";
        long branchId;
        String friendlyVersionName = "";
        long updateId;
    }
    public  VersionInfo getIds(String branch, String version) throws IOException, NoVersionFoundException {
        try {
            long branchId = -1, updateId = -1;
            UpdateBranch branch1 = null;
            List<UpdateBranch> branches = getUpdateBranches();
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
                List<Update> updateList = getLatestUpdates(branchId, page++);
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
            throw new NoVersionFoundException(branch, version, "Exception occurred", e);
        }
    }
}
