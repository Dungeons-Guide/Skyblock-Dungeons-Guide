package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.GuiOpenListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class FeatureInstaCloseChest extends SimpleFeature implements GuiOpenListener, TickListener {
    public FeatureInstaCloseChest() {
        super("QoL", "Auto-Close Secret Chest", "Automatically closes Secret Chest as soon as you open it\nCan put item price threshold by clicking edit", "qol.autoclose", false);
        parameters.put("threshold", new FeatureParameter<Integer>("threshold", "Price Threshold", "The maximum price of item for chest to be closed. Default 1m", 1000000, "integer"));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    private boolean check;
    @Override
    public void onGuiOpen(GuiOpenEvent event) {
        if (!this.isEnabled()) return;

        if (!skyblockStatus.isOnDungeon()) return;
        if (!(event.gui instanceof GuiChest)) return;

        ContainerChest ch = (ContainerChest) ((GuiChest)event.gui).inventorySlots;
        if (!"container.chest".equals(ch.getLowerChestInventory().getName())) return;
        check = true;
    }

    public int getPrice(ItemStack itemStack) {
        if (itemStack == null) return 0;
        NBTTagCompound compound = itemStack.getTagCompound();
        if (compound == null)
            return 0;
        if (!compound.hasKey("ExtraAttributes"))
            return 0;
        final String id = compound.getCompoundTag("ExtraAttributes").getString("id");
        if (id.equals("ENCHANTED_BOOK")) {
            final NBTTagCompound enchants = compound.getCompoundTag("ExtraAttributes").getCompoundTag("enchantments");
            Set<String> keys = enchants.getKeySet();
            Set<String> actualKeys = new TreeSet<String>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    String id2 = id + "::" + o1 + "-" + enchants.getInteger(o1);
                    AhUtils.AuctionData auctionData = AhUtils.auctions.get(id2);
                    int price1 = (auctionData == null) ? 0 : ((Integer)auctionData.prices.first()).intValue();
                    String id3 = id + "::" + o2 + "-" + enchants.getInteger(o2);
                    AhUtils.AuctionData auctionData2 = AhUtils.auctions.get(id3);
                    int price2 = (auctionData2 == null) ? 0 : ((Integer)auctionData2.prices.first()).intValue();
                    return (compare2(price1, price2) == 0) ? o1.compareTo(o2) : compare2(price1, price2);
                }

                public int compare2(int y, int x) {
                    return (x < y) ? -1 : ((x == y) ? 0 : 1);
                }
            });
            actualKeys.addAll(keys);
            int totalLowestPrice = 0;
            for (String key : actualKeys) {
                String id2 = id + "::" + key + "-" + enchants.getInteger(key);
                AhUtils.AuctionData auctionData = AhUtils.auctions.get(id2);
                totalLowestPrice += auctionData.prices.first();
            }
            return totalLowestPrice;
        } else {
            AhUtils.AuctionData auctionData = AhUtils.auctions.get(id);
            if (auctionData == null) {
                return 0;
            } else {
                if (auctionData.sellPrice == -1 && auctionData.prices.size() > 0) return auctionData.prices.first();
                else if (auctionData.sellPrice != -1 && auctionData.prices.size() == 0) return auctionData.sellPrice;
                else {
                    int ahPrice = auctionData.prices.first();
                    if (ahPrice > auctionData.sellPrice) return ahPrice;
                    else return auctionData.sellPrice;
                }
            }
        }
    }

    @Override
    public void onTick() {
        if (!this.isEnabled()) return;
        if (check) {
            check = false;

            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen instanceof GuiChest){
                int priceSum = 0;
                for (ItemStack inventoryItemStack : ((GuiChest) screen).inventorySlots.inventoryItemStacks) {
                    priceSum += getPrice(inventoryItemStack);
                }

                int threshold = this.<Integer>getParameter("threshold").getValue();
                if (priceSum < threshold) {
                    Minecraft.getMinecraft().thePlayer.closeScreen();
                }
            }
        }
    }
}
