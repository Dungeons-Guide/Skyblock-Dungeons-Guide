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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import com.google.common.collect.ImmutableMap;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;

import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.listener.PlayerRenderListener;
import kr.syeyoung.dungeonsguide.mod.features.listener.TextureStichListener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;

import java.io.IOException;


public class FeaturePenguins extends SimpleFeature implements PlayerRenderListener, TextureStichListener {
    public FeaturePenguins() {
        super("Misc", "Penguins", "Awwww", "etc.penguin", false);
        OBJLoader.instance.addDomain("dungeonsguide");

    }
    @Override
    public void onTextureStitch(TextureStitchEvent event) {
        if (event instanceof TextureStitchEvent.Pre) {
            objModel = null;
            ResourceLocation modelResourceLocation = new ResourceLocation("dungeonsguide:models/penguin.obj");
            try {
                objModel = (OBJModel) OBJLoader.instance.loadModel(modelResourceLocation);
                objModel = (OBJModel) objModel.process(new ImmutableMap.Builder<String, String>().put("flip-v", "true").build());
                for (String obj : objModel.getMatLib().getMaterialNames()) {
                    ResourceLocation resourceLocation = objModel.getMatLib().getMaterial(obj).getTexture().getTextureLocation();
                    event.map.registerSprite(resourceLocation);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (objModel != null && event instanceof TextureStitchEvent.Post) {
            model = objModel.bake(objModel.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
        }
    }


    private OBJModel objModel;
    private final SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    private IBakedModel model;

    @Override
    public void onEntityRenderPre(RenderPlayerEvent.Pre renderPlayerEvent) {

        if (!isEnabled()) return;
        if (renderPlayerEvent.entityPlayer.isInvisible()) return;
        renderPlayerEvent.setCanceled(true);
        GlStateManager.pushMatrix();
        GlStateManager.color(1,1,1,1);
        GlStateManager.translate(renderPlayerEvent.x, renderPlayerEvent.y, renderPlayerEvent.z);
        if (renderPlayerEvent.entityPlayer.isSneaking())
        {
            GlStateManager.translate(0.0F, -0.203125F, 0.0F);
        }
        float f1 = renderPlayerEvent.entityPlayer.prevRotationYawHead + (renderPlayerEvent.entityPlayer.rotationYawHead - renderPlayerEvent.entityPlayer.prevRotationYawHead) * renderPlayerEvent.partialRenderTick;
        GlStateManager.rotate(f1+180,0,-1,0);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(
                model, 1,1,1,1
        );
        GlStateManager.popMatrix();


        EntityPlayer entitylivingbaseIn = renderPlayerEvent.entityPlayer;
        {
            ItemStack itemstack = entitylivingbaseIn.getHeldItem();

            if (itemstack != null)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(renderPlayerEvent.x, renderPlayerEvent.y, renderPlayerEvent.z);
                if (renderPlayerEvent.entityPlayer.isSneaking())
                {
                    GlStateManager.translate(0.0F, -0.203125F, 0.0F);
                }
                GlStateManager.rotate(f1+180, 0.0f, -1.0f, 0.0f);
                GlStateManager.translate(0,1.30 ,-0.5);



                if (entitylivingbaseIn.fishEntity != null)
                {
                    itemstack = new ItemStack(Items.fishing_rod, 0);
                }

                Item item = itemstack.getItem();
                Minecraft minecraft = Minecraft.getMinecraft();

                GlStateManager.rotate(180, 0.0f, 0.0f, 1.0f);
                if (item.isFull3D()) {
                    GlStateManager.translate(0.05,0,0);
                    GlStateManager.rotate(90, 0.0f, 0.0f, 1.0f);
                    GlStateManager.rotate(-45, 1.0f, 0.0f, 0.0f);
                } else if (item instanceof ItemBow) {
                    GlStateManager.translate(0,0.1, -0);
                    GlStateManager.rotate(90, 0.0f, 1.0f, 0.0f);
                    GlStateManager.rotate(-90, 0.0f, 0.0f, 1.0f);
                } else if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
                    GlStateManager.translate(0,-0.20,0.1);
                    GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
                    GlStateManager.rotate(-25.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                    f1 = 0.375F;
                    GlStateManager.scale(-f1, -f1, f1);
                } else if (item instanceof ItemBlock) {
                    GlStateManager.translate(0.0F, 0.05, 0.1);
                    GlStateManager.rotate(-25.0F, 1.0F, 0.0F, 0.0F);
                } else {
                    GlStateManager.translate(0,-0.1, 0.1);
                }

                GlStateManager.scale(0.8,0.8,0.8);

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
