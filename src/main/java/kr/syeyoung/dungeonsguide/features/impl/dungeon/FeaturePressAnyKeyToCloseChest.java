package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.impl.boss.FeatureChestPrice;
import kr.syeyoung.dungeonsguide.features.listener.KeyInputListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class FeaturePressAnyKeyToCloseChest extends SimpleFeature implements KeyInputListener {
    public FeaturePressAnyKeyToCloseChest() {
        super("Dungeon", "Press Any Key to close Secret Chest", "dungeon.presskeytoclose");
        parameters.put("threshold", new FeatureParameter<Integer>("threshold", "Price Threshold", "The maximum price of item for chest to be closed. Default 1m", 1000000, "integer"));
    }

    @Override
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiChest){
            ContainerChest ch = (ContainerChest) ((GuiChest)screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName())
                    || "Chest".equals(ch.getLowerChestInventory().getName()))) return;
            IInventory actualChest = ch.getLowerChestInventory();

            int priceSum = 0;
            for (int i = 0; i < actualChest.getSizeInventory(); i++) {
                priceSum += FeatureChestPrice.getPrice(actualChest.getStackInSlot(i));
            }

            int threshold = this.<Integer>getParameter("threshold").getValue();
            if (priceSum < threshold) {
                Minecraft.getMinecraft().thePlayer.closeScreen();
            }
        }
    }
}
