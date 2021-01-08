package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.GuiBackgroundRenderListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPostRenderListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPreRenderListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.TreeMap;

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
                itemPrice += FeatureInstaCloseChest.getPrice(item) * item.stackSize;
            }
        }

        int i = 222;
        int j = i - 108;
        int ySize = j + (actualChest.getSizeInventory() / 9) * 18;
        int left = (rendered.gui.width + 176) / 2;
        int top = (rendered.gui.height - ySize ) / 2;

        int width = 120;

        GL11.glPushMatrix();
        GL11.glTranslated(left, top, 0);
        Gui.drawRect( 0,0,width, 30, 0xFFDDDDDD);

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("BIN/AH Price: ", 5,5, 0xFF000000);
        String str = TextUtils.format(itemPrice);
        fr.drawString(str, width - fr.getStringWidth(str) - 5, 5, 0xFF000000);

        fr.drawString("Profit: ", 5,15, 0xFF000000);
        str = (itemPrice > chestPrice ? "+" : "") +TextUtils.format(itemPrice - chestPrice);
        fr.drawString(str, width - fr.getStringWidth(str) - 5, 15, itemPrice > chestPrice ? 0xFF00CC00 : 0xFFCC0000);

        GL11.glPopMatrix();
    }
}
