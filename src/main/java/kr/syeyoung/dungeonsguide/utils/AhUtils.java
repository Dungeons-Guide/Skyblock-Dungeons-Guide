package kr.syeyoung.dungeonsguide.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class AhUtils {
    public static volatile Map<String, AuctionData> auctions = new HashMap<String, AuctionData>();

    private static Map<String, AuctionData> semi_auctions = new HashMap<String, AuctionData>();

    public static Timer timer = new Timer();

    public static int totalAuctions = 0;

    public static void registerTimer() {
        timer.schedule(new TimerTask() {
            public void run() {
                AhUtils.loadAuctions();
            }
        },  0L, 1800000L);
    }

    public static void loadAuctions() {
        try {
            int i = 0;
            do {

            } while (loadPage(i++));
            loadBazaar();
            auctions = semi_auctions;
            semi_auctions = new HashMap<String, AuctionData>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadBazaar() throws IOException {
        System.out.println("Fetching bazaar data");
        URL url = new URL("https://api.hypixel.net/skyblock/bazaar");
        InputStreamReader reader = new InputStreamReader(url.openStream());
        JsonObject object = (JsonObject)(new JsonParser()).parse(reader);
        boolean success = object.get("success").getAsBoolean();
        if (!success)
            return;
        JsonObject element = object.getAsJsonObject("products");
        for (Map.Entry<String, JsonElement> product : (Iterable<Map.Entry<String, JsonElement>>)element.entrySet()) {
            String id = product.getKey();
            AuctionData auctionData = semi_auctions.get(id);
            boolean notexisted = (auctionData == null);
            if (notexisted)
                auctionData = new AuctionData(id);
            auctionData.sellPrice = ((JsonElement)product.getValue()).getAsJsonObject().getAsJsonObject("quick_status").get("sellPrice").getAsInt();
            auctionData.buyPrice = ((JsonElement)product.getValue()).getAsJsonObject().getAsJsonObject("quick_status").get("buyPrice").getAsInt();
            if (notexisted)
                semi_auctions.put(id, auctionData);
        }
    }

    public static boolean loadPage(int page) throws IOException {
        System.out.println("Fetching page " + page + " of auctions");
        URL url = new URL("https://api.hypixel.net/skyblock/auctions?page=" + page);
        InputStreamReader reader = new InputStreamReader(url.openStream());
        JsonObject object = (JsonObject)(new JsonParser()).parse(reader);
        boolean success = object.get("success").getAsBoolean();
        if (!success)
            return false;
        int maxPage = object.get("totalPages").getAsInt();
        int totalAuctions = object.get("totalAuctions").getAsInt();
        System.out.println("Fetched page " + page + "/" + maxPage + " of auctions! (" + totalAuctions + " total)");
        JsonArray array = object.get("auctions").getAsJsonArray();
        for (JsonElement element2 : array) {
            JsonObject element = element2.getAsJsonObject();
            JsonElement isBin = element.get("bin");
            if (isBin == null || !isBin.getAsBoolean())
                continue;
            byte[] itemData = Base64.decode(element.get("item_bytes").getAsString().replace("\\u003d", "="));
            NBTTagCompound nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(itemData));
            NBTTagCompound acutalItem = (NBTTagCompound)nbtTagCompound.getTagList("i", 10).get(0);
            NBTTagCompound attributes = acutalItem.getCompoundTag("tag").getCompoundTag("ExtraAttributes");
            String id = attributes.getString("id");
            if (id.equals("ENCHANTED_BOOK")) {
                NBTTagCompound enchants = attributes.getCompoundTag("enchantments");
                Set<String> keys = enchants.getKeySet();
                if (keys.size() != 1)
                    continue;
                String ench = keys.iterator().next();
                int lv = enchants.getInteger(ench);
                id = id + "::" + ench + "-" + lv;
            }
            AuctionData auctionData = semi_auctions.get(id);
            boolean notexisted = (auctionData == null);
            if (notexisted)
                auctionData = new AuctionData(id);
            auctionData.prices.add(element.get("starting_bid").getAsInt());
            if (notexisted)
                semi_auctions.put(id, auctionData);
        }
        return (page < maxPage);
    }

    public static class AuctionData {
        public String id;

        public SortedSet<Integer> prices;

        public int sellPrice = -1;

        public int buyPrice = -1;

        public AuctionData(String id) {
            this.id = id;
            this.prices = new TreeSet<Integer>();
        }
    }
}