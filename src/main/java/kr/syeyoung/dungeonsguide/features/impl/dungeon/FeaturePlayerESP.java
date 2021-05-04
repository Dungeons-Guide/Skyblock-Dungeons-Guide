package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.PlayerRenderListener;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.List;


public class FeaturePlayerESP extends SimpleFeature implements PlayerRenderListener {
    public FeaturePlayerESP() {
        super("Dungeon", "See players through walls", "See players through walls", "dungeon.playeresp", false);
        setEnabled(false);
    }


    private final SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    private boolean preCalled = false;
    @Override
    public void onEntityRenderPre(RenderPlayerEvent.Pre renderPlayerEvent) {


        if (preCalled) return;
        if (!isEnabled()) return;


        DungeonContext dungeonContext = skyblockStatus.getContext();
        if (dungeonContext == null) return;
        if (!dungeonContext.getPlayers().contains(renderPlayerEvent.entityPlayer.getName())) {
            return;
        }

        preCalled = true;

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        GL11.glStencilMask(0xFF);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);

        EntityPlayer entity = renderPlayerEvent.entityPlayer;
        InventoryPlayer inv = entity.inventory;
        ItemStack[] armor = inv.armorInventory;
        inv.armorInventory = new ItemStack[4];
        ItemStack[] hand = inv.mainInventory;
        inv.mainInventory = new ItemStack[36];

        float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * renderPlayerEvent.partialRenderTick;
        try {
            renderPlayerEvent.renderer.doRender((AbstractClientPlayer) renderPlayerEvent.entityPlayer, renderPlayerEvent.x, renderPlayerEvent.y, renderPlayerEvent.z, f, renderPlayerEvent.partialRenderTick);
        } catch (Throwable t) {}

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_NOTEQUAL, 1, 0xff);
        GL11.glDepthMask(false);
        GL11.glDepthFunc(GL11.GL_GEQUAL);

        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPlayerEvent.x, renderPlayerEvent.y + 0.9, renderPlayerEvent.z);
        GlStateManager.scale(1.2f, 1.1f, 1.2f);
        renderPlayerEvent.renderer.setRenderOutlines(true);
        try {
            renderPlayerEvent.renderer.doRender((AbstractClientPlayer) renderPlayerEvent.entityPlayer, 0,-0.9,0, f, renderPlayerEvent.partialRenderTick);
        } catch (Throwable t) {}

        renderPlayerEvent.renderer.setRenderOutlines(false);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_STENCIL_TEST); // Turn this shit off!

        inv.armorInventory = armor;
        inv.mainInventory = hand;

        preCalled = false;

    }

    @Override
    public void onEntityRenderPost(RenderPlayerEvent.Post renderPlayerEvent) {
    }

}
