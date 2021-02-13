package kr.syeyoung.dungeonsguide.utils;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.e;

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
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
            }
        },  0L, 1800000L);
    }

    public static void loadAuctions() throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, KeyStoreException, IllegalBlockSizeException, KeyManagementException {
        System.out.println("I think i'm loading ah");
        try {

            Map<String, AuctionData> semi_auctions = new HashMap<String, AuctionData>();
            JsonElement object = e.getDungeonsGuide().getAuthenticator().d("https://dungeonsguide.kro.kr/resource/keys");
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