package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.DungeonQuitListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiOpenListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FeatureCooldownCounter extends GuiFeature implements DungeonQuitListener, GuiOpenListener {
    public FeatureCooldownCounter() {
        super("ETC", "Dungeon Cooldown Counter", "Counts 10 seconds after leaving dungeon", "qol.cooldown", true, getFontRenderer().getStringWidth("Cooldown: 10s "), getFontRenderer().FONT_HEIGHT);
        parameters.put("color", new FeatureParameter<Color>("color", "Color", "Color of text", Color.white, "color"));
    }

    private long leftDungeonTime = 0L;
    private boolean wasInDungeon = false;
    @Override
    public void drawHUD(float partialTicks) {
        if (System.currentTimeMillis() - leftDungeonTime > 20000) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        fr.drawString("Cooldown: "+(20 - (System.currentTimeMillis() - leftDungeonTime) / 1000)+"s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        double scale = getFeatureRect().getHeight() / fr.FONT_HEIGHT;
        GlStateManager.scale(scale, scale, 0);
        fr.drawString("Cooldown: 20s", 0,0,this.<Color>getParameter("color").getValue().getRGB());
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void onDungeonQuit() {
        leftDungeonTime = System.currentTimeMillis();
    }

    @Override
    public void onGuiOpen(GuiOpenEvent rendered) {
        if (!(rendered.gui instanceof GuiChest)) return;
        ContainerChest chest = (ContainerChest) ((GuiChest) rendered.gui).inventorySlots;
        if (chest.getLowerChestInventory().getName().contains("On Cooldown!")) {
            leftDungeonTime = System.currentTimeMillis();
        } else if (chest.getLowerChestInventory().getName().contains("Error")) {
            leftDungeonTime = System.currentTimeMillis();
        }
    }
}
