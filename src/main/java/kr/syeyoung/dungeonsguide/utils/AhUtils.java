/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.utils;

import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.DungeonsGuide;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.util.Timer;
import java.util.TimerTask;

public class AhUtils {
    public static volatile Map<String, AuctionData> auctions = new HashMap<String, AuctionData>();

    public static Timer timer = new Timer();

    public static int totalAuctions = 0;

    public static void registerTimer() {
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    AhUtils.loadAuctions();
                } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | KeyStoreException | IllegalBlockSizeException | KeyManagementException e) {
                    e.printStackTrace();
                }
            }
        },  0L, 1800000L);
    }

    public static void loadAuctions() throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, KeyStoreException, IllegalBlockSizeException, KeyManagementException {
        try {

            Map<String, AuctionData> semi_auctions = new HashMap<String, AuctionData>();
            JsonElement object = DungeonsGuide.getDungeonsGuide().getAuthenticator().getJsonSecured("https://dungeons.guide/resource/keys");
            for (JsonElement element : object.getAsJsonArray()) {
                JsonObject object1 = element.getAsJsonObject();
                AuctionData auctionData = new AuctionData(object1.get("id").getAsString());
                auctionData.lowestBin = object1.get("lowestBin").getAsInt();
                auctionData.sellPrice = object1.get("sellPrice").getAsInt();
                auctionData.buyPrice = object1.get("buyPrice").getAsInt();
                semi_auctions.put(auctionData.id, auctionData);
            }
            auctions = semi_auctions;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static class AuctionData {
        public String id;

        public long lowestBin = -1;

        public int sellPrice = -1;

        public int buyPrice = -1;

        public AuctionData(String id) {
            this.id = id;
        }
    }
}