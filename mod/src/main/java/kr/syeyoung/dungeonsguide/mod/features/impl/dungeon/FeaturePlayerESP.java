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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;


public class FeaturePlayerESP extends SimpleFeature {
    public FeaturePlayerESP() {
        super("Dungeon.Teammates", "See players through walls", "See players through walls", "dungeon.playeresp", false);
        setEnabled(false);
    }


    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();

    private boolean preCalled = false;
    @DGEventHandler
    public void onEntityRenderPre(RenderPlayerEvent.Pre renderPlayerEvent) {


        if (preCalled) return;
        


        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
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

}
