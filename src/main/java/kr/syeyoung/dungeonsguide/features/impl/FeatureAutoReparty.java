package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.DungeonEndListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiBackgroundRenderListener;
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
import org.lwjgl.opengl.GL11;

public class FeatureAutoReparty extends SimpleFeature implements DungeonEndListener {
    public FeatureAutoReparty() {
        super("Bossfight", "Auto reparty when dungeon finishes","Auto reparty on dungeon finish", "bossfight.autoreparty", false);
    }

    @Override
    public void onDungeonEnd() {
        if (isEnabled()) e.getDungeonsGuide().getCommandReparty().requestReparty();
    }
}
