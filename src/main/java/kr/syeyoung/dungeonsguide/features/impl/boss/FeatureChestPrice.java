package kr.syeyoung.dungeonsguide.features.impl.boss;

import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.GuiBackgroundRenderListener;
import kr.syeyoung.dungeonsguide.utils.AhUtils;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class FeatureChestPrice extends SimpleFeature implements GuiBackgroundRenderListener {
    public FeatureChestPrice() {
        super("Bossfight", "Show Profit of Dungeon Chests","Show Profit of Dungeon Chests", "bossfight.profitchest", true);
    }

    @Override
    public void onGuiBGRender(GuiScreenEvent.BackgroundDrawnEvent rendered) {
        if (!isEnabled()) return;
        if (!(rendered.gui instanceof GuiChest)) return;
        if (!e.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;

        GlStateManager.disableLighting();

        ContainerChest chest = (ContainerChest) ((GuiChest) rendered.gui).inventorySlots;
        if (!chest.getLowerChestInventory().getName().endsWith("Chest")) return;
        IInventory actualChest = chest.getLowerChestInventory();

        int chestPrice = 0;
        int itemPrice = 0;
        for (int i = 0; i <actualChest.getSizeInventory(); i++) {
            ItemStack item = actualChest.getStackInSlot(i);
            if (item != null) {
                if (item.getDisplayName() != null && item.getDisplayName().contains("Reward")) {
                    NBTTagCompound tagCompound = item.serializeNBT().getCompoundTag("tag");
                    if (tagCompound != null && tagCompound.hasKey("display", 10)) {
                        NBTTagCompound nbttagcompound = tagCompound.getCompoundTag("display");

                        if (nbttagcompound.getTagId("Lore") == 9) {
                            NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

                            if (nbttaglist1.tagCount() > 0) {
                                for (int j1 = 0; j1 < nbttaglist1.tagCount(); ++j1) {
                                    String str = nbttaglist1.getStringTagAt(j1);
                                    if (str.endsWith("Coins")) {
                                        String coins = TextUtils.stripColor(str).replace(" Coins", "").replace(",","");
                                        try {
                                            chestPrice = Integer.parseInt(coins);
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                itemPrice += getPrice(item) * item.stackSize;
            }
        }

        int i = 222;
        int j = i - 108;
        int ySize = j + (actualChest.getSizeInventory() / 9) * 18;
        int left = (rendered.gui.width + 176) / 2;
        int top = (rendered.gui.height - ySize ) / 2;

        int width = 120;

        GlStateManager.pushMatrix();
        GlStateManager.translate(left, top, 0);
        Gui.drawRect( 0,0,width, 30, 0xFFDDDDDD);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("BIN/AH Price: ", 5,5, 0xFF000000);
        String str = TextUtils.format(itemPrice);
        fr.drawString(str, width - fr.getStringWidth(str) - 5, 5, 0xFF000000);

        fr.drawString("Profit: ", 5,15, 0xFF000000);
        str = (itemPrice > chestPrice ? "+" : "") +TextUtils.format(itemPrice - chestPrice);
        fr.drawString(str, width - fr.getStringWidth(str) - 5, 15, itemPrice > chestPrice ? 0xFF00CC00 : 0xFFCC0000);

        GlStateManager.popMatrix();

        GlStateManager.enableLighting();
        GlStateManager.enableBlend();
    }

    public static long getPrice(ItemStack itemStack) {
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
                    long price1 = (auctionData == null) ? 0 : auctionData.lowestBin;
                    String id3 = id + "::" + o2 + "-" + enchants.getInteger(o2);
                    AhUtils.AuctionData auctionData2 = AhUtils.auctions.get(id3);
                    long price2 = (auctionData2 == null) ? 0 : auctionData2.lowestBin;
                    return (compare2(price1, price2) == 0) ? o1.compareTo(o2) : compare2(price1, price2);
                }

                public int compare2(long y, long x) {
                    return (x < y) ? -1 : ((x == y) ? 0 : 1);
                }
            });
            actualKeys.addAll(keys);
            int totalLowestPrice = 0;
            for (String key : actualKeys) {
                String id2 = id + "::" + key + "-" + enchants.getInteger(key);
                AhUtils.AuctionData auctionData = AhUtils.auctions.get(id2);
                totalLowestPrice += auctionData.lowestBin;
            }
            return totalLowestPrice;
        } else {
            AhUtils.AuctionData auctionData = AhUtils.auctions.get(id);
            if (auctionData == null) {
                return 0;
            } else {
                if (auctionData.sellPrice == -1 && auctionData.lowestBin != -1) return auctionData.lowestBin;
                else if (auctionData.sellPrice != -1 && auctionData.lowestBin == -1) return auctionData.sellPrice;
                else {
                    long ahPrice = auctionData.lowestBin;
                    if (ahPrice > auctionData.sellPrice) return ahPrice;
                    else return auctionData.sellPrice;
                }
            }
        }
    }
}
