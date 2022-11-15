/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;


import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;

import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.listener.TooltipListener;
import kr.syeyoung.dungeonsguide.mod.utils.AhUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class FeatureTooltipPrice extends SimpleFeature implements TooltipListener {
    public FeatureTooltipPrice() {
        super("Misc.API Features", "Item Price", "Shows price of items", "tooltip.price");
        addParameter("reqShift", new FeatureParameter<Boolean>("reqShift", "Require Shift", "If shift needs to be pressed in order for this feature to be activated", false, "boolean"));
        setEnabled(false);
    }

    @Override
    public void onTooltip(ItemTooltipEvent event) {
        if (!isEnabled()) return;

        boolean activated = !this.<Boolean>getParameter("reqShift").getValue() || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        ItemStack hoveredItem = event.itemStack;
        NBTTagCompound compound = hoveredItem.getTagCompound();
        if (compound == null)
            return;
        if (!compound.hasKey("ExtraAttributes"))
            return;
        if (!activated) {
            event.toolTip.add("§7Shift to view price");
            return;
        }

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
            int iterations = 0;
            for (String key : actualKeys) {
                iterations++;
                String id2 = id + "::" + key + "-" + enchants.getInteger(key);
                AhUtils.AuctionData auctionData = AhUtils.auctions.get(id2);
                if (auctionData == null) {
                    if (iterations < 10)
                        event.toolTip.add("§f"+ key + " " + enchants.getInteger(key) + "§7: §cn/a");
                    continue;
                }
                if (iterations < 10)
                    event.toolTip.add("§f"+ key + " " + enchants.getInteger(key) + "§7: §e"+ TextUtils.format( auctionData.lowestBin));
                totalLowestPrice += auctionData.lowestBin;
            }
            if (iterations >= 10)
                event.toolTip.add("§7"+ (iterations - 10) + " more enchants... ");
            event.toolTip.add("§fTotal Lowest§7: §e"+ TextUtils.format(totalLowestPrice));
        } else {
            AhUtils.AuctionData auctionData = AhUtils.auctions.get(id);
            event.toolTip.add("");
            if (auctionData == null) {
                event.toolTip.add("§fLowest ah §7: §cn/a");
                event.toolTip.add("§fBazaar sell price §7: §cn/a");
                event.toolTip.add("§fBazaar buy price §7: §cn/a");
            } else {
                event.toolTip.add("§fLowest ah §7: " + ((auctionData.lowestBin != -1) ? ("§e"+ TextUtils.format(auctionData.lowestBin)) : "§cn/a"));
                event.toolTip.add("§fBazaar sell price §7: " + ((auctionData.sellPrice == -1) ? "§cn/a": ("§e"+ TextUtils.format(auctionData.sellPrice))));
                event.toolTip.add("§fBazaar buy price §7: " + ((auctionData.buyPrice == -1) ? "§cn/a": ("§e"+ TextUtils.format(auctionData.buyPrice))));
            }
        }
    }
}
