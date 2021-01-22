package kr.syeyoung.dungeonsguide.features.impl.boss;

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
        super("Bossfight", "Auto reparty when dungeon finishes","Auto reparty on dungeon finish\n\nThis automates player chatting action, (disbanding, repartying) Thus it might be against hypixel's rules.\nBut mods like auto-gg exist so I'm leaving this feature.\nThis option is use-at-your-risk and you'll be responsible for ban if you somehow get banned because of this feature\n(Although it is not likely to happen)\nDefaults to off", "bossfight.autoreparty", false);
    }

    @Override
    public void onDungeonEnd() {
        if (isEnabled()) e.getDungeonsGuide().getCommandReparty().requestReparty();
    }
}
