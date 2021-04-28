package kr.syeyoung.dungeonsguide.features.impl.party;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.impl.party.api.ApiFetchur;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonType;
import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import kr.syeyoung.dungeonsguide.features.listener.GuiPostRenderListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.event.GuiScreenEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FeatureGoodParties extends SimpleFeature implements GuiPostRenderListener {
    public FeatureGoodParties() {
        super("Party Kicker", "Highlight parties in party viewer", "Highlight parties you can't join with red", "partykicker.goodparty",true);
    }

    @Override
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!isEnabled()) return;
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;
        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();
        if (!"Party Finder".equals(name)) return;


        int i = 222;
        int j = i - 108;
        int ySize = j + (((ContainerChest)(((GuiChest) Minecraft.getMinecraft().currentScreen).inventorySlots)).getLowerChestInventory().getSizeInventory() / 9) * 18;
        int left = (rendered.gui.width - 176) / 2;
        int top = (rendered.gui.height - ySize ) / 2;
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.colorMask(true, true, true, false);
        GlStateManager.translate(left, top, 0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        try {

            for (int i1 = 0; i1 < Integer.min(54, cont.inventorySlots.size()); i1++) {
                Slot s = cont.inventorySlots.get(i1);
                if (s.getStack() == null) continue;
                if (s.getStack().getItem() != Items.skull) continue;
                NBTTagCompound nbt = s.getStack().getTagCompound();
                if (nbt == null || nbt.hasNoTags()) continue;
                NBTTagCompound display = nbt.getCompoundTag("display");
                if (display.hasNoTags()) return;
                NBTTagList lore = display.getTagList("Lore", 8);
                int classLvReq = 0;
                int cataLvReq = 0;
                boolean Req = false;
                for (int n = 0; n < lore.tagCount(); n++) {
                    String str = lore.getStringTagAt(n);
                    if (str.startsWith("§7Dungeon Level Required: §b")) cataLvReq = Integer.parseInt(str.substring(28));
                    if (str.startsWith("§7Class Level Required: §b")) classLvReq = Integer.parseInt(str.substring(26));
                    if (str.startsWith("§cRequires")) Req = true;
                }
                System.out.println(classLvReq + " / "+cataLvReq);

                if (Req) {
                    int x = s.xDisplayPosition;
                    int y = s.yDisplayPosition;
                    Gui.drawRect(x, y, x + 16, y + 16, 0x77AA0000);
                }


            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }
}
