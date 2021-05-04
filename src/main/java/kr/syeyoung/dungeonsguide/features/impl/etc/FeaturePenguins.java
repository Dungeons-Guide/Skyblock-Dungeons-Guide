/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.PlayerRenderListener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;


public class FeaturePenguins extends SimpleFeature implements PlayerRenderListener {
    public FeaturePenguins() {
        super("ETC", "Penguins", "Awwww", "etc.penguin", false);
    }


    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    private final ResourceLocation penguin = new ResourceLocation("dungeonsguide:penguin.png");

    @Override
    public void onEntityRenderPre(RenderPlayerEvent.Pre renderPlayerEvent) {
        if (!isEnabled()) return;
        renderPlayerEvent.setCanceled(true);
        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPlayerEvent.x, renderPlayerEvent.y, renderPlayerEvent.z);

        GlStateManager.rotate(-renderPlayerEvent.renderer.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);

        GlStateManager.translate(0.5,2,0);
        GlStateManager.scale(-1,-1,-1);

        if (renderPlayerEvent.entityPlayer.isSneaking())
        {
            GlStateManager.translate(0.0F, 0.203125F, 0.0F);
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(penguin);
        GlStateManager.disableLighting();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Gui.drawModalRectWithCustomSizedTexture(0,0,0,0,1,2,1,2);
        GlStateManager.bindTexture(0);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        EntityPlayer entitylivingbaseIn = renderPlayerEvent.entityPlayer;
        {
            ItemStack itemstack = entitylivingbaseIn.getHeldItem();

            if (itemstack != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(renderPlayerEvent.x, renderPlayerEvent.y, renderPlayerEvent.z);
                GlStateManager.rotate(-renderPlayerEvent.renderer.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
                GlStateManager.translate(-0.3,1.8,0.1);
                GlStateManager.scale(0.8,0.8,0.8);


                if (entitylivingbaseIn.fishEntity != null)
                {
                    itemstack = new ItemStack(Items.fishing_rod, 0);
                }

                Item item = itemstack.getItem();
                Minecraft minecraft = Minecraft.getMinecraft();

                if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2)
                {
                    GlStateManager.translate(0,0,0.2);
                    GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
                    GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                    float f1 = 0.375F;
                    GlStateManager.scale(-f1, -f1, f1);
                }

                if (entitylivingbaseIn.isSneaking())
                {
                    GlStateManager.translate(0.0F, 0.203125F, 0.0F);
                }

                minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
                GlStateManager.popMatrix();
            }
        }


        renderPlayerEvent.renderer.renderName((AbstractClientPlayer) renderPlayerEvent.entityPlayer, renderPlayerEvent.x, renderPlayerEvent.y, renderPlayerEvent.z);


    }

    @Override
    public void onEntityRenderPost(RenderPlayerEvent.Post renderPlayerEvent) {
    }

}
