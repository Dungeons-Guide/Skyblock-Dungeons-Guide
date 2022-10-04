package kr.syeyoung.dungeonsguide.features.impl.advanced;

import kr.syeyoung.dungeonsguide.features.GuiFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FeatureTestPepole extends GuiFeature {
    public FeatureTestPepole() {
        super("Dungeon", "Feuture test", "NOU", "", false, 200, 100);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiScreen(GuiScreenEvent.BackgroundDrawnEvent event) {
//        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
//        itemRender.renderItemAndEffectIntoGUI(new ItemStack(Items.skull, 1), 100, 100);
    }

    @Override
    public void drawScreen(float partialTicks) {
//        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.skull, 1), 100, 100);
        super.drawScreen(partialTicks);
    }

    @Override
    public void drawHUD(float partialTicks) {

        Gui.drawRect(0,0,10, 299, 0xFFFF0000);

        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.skull, 1), 100, 100);
//        EntityPlayer entitylivingbaseIn = Minecraft.getMinecraft().thePlayer;

//        Minecraft.getMinecraft().getItemRenderer().renderItem(entitylivingbaseIn, new ItemStack(Items.skull, 1), ItemCameraTransforms.TransformType.THIRD_PERSON);
    }
}
