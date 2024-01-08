/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonFacade;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureCollectDungeonRooms extends SimpleFeature {
    public FeatureCollectDungeonRooms() {
        super("Misc", "Collect Dungeon Data", "Enable to allow sending anything inside dungeon to developers server\n\nThis option is to rebuild dungeon room data to implement better features\n\nDisable to opt out of it","etc.collectdungeon", false);
        addParameter("prompted", new FeatureParameter<Boolean>("prompted", "Was this prompted?", "Did this feature prompt for user apporval yet?", false, TCBoolean.INSTANCE));
    }


    public class WidgetUserApproval extends AnnotatedImportOnlyWidget {
        public WidgetUserApproval() {
            super(new ResourceLocation("dungeonsguide:gui/collect_rooms_approval.gui"));
        }

        @On(functionName = "approve")
        public void onApprove() {
            FeatureCollectDungeonRooms.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDungeonRooms.this.setEnabled(true);
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        }

        @On(functionName = "deny")
        public void onDeny() {
            FeatureCollectDungeonRooms.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDungeonRooms.this.setEnabled(false);
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        }
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        if (!this.<Boolean>getParameter("prompted").getValue()) {
            Scaler scaler = new Scaler();
            scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
            scaler.child.setValue(new FeatureCollectDungeonRooms.WidgetUserApproval());
            GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(scaler, null, false));
        }
    }

    private Map<Integer, EntityData> map = new HashMap<>();

    @Data @Getter
    public static class EntityData {
        private int id;
        private long spawnedAt;
        private Vec3 spawnLocation;
        private Vec3 playerLocationOnSpawn;
        private IChatComponent armorstand;
        private ItemStack[] armoritems = new ItemStack[5];
        private Map<String, Double> attributes = new HashMap<>();
        private String type;
        private List<DataWatcher.WatchableObject> metadata;
        private String name;
    }


    @DGEventHandler
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (map.get(event.entity.getEntityId()) != null) return;
        EntityData entityData = new EntityData();
        entityData.spawnedAt = System.currentTimeMillis();
        entityData.spawnLocation = event.entity.getPositionVector();
        entityData.playerLocationOnSpawn = Minecraft.getMinecraft().thePlayer.getPositionVector();
        entityData.type = event.entity.getClass().getSimpleName();
        map.put(event.entity.getEntityId(), entityData);
    }

    @DGEventHandler
    public void onEntityAttributeUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityArmorStand) {
            return;
        }
        EntityData entityData = map.get(event.entity.getEntityId());
        entityData.id = event.entity.getEntityId();


        List<Entity> entityList = event.entity.worldObj.getEntitiesInAABBexcluding(event.entity, new AxisAlignedBB(-0.2,-0.2,-0.2,0.2,0.2,0.2).offset(event.entity.posX, event.entity.posY+event.entity.height, event.entity.posZ), e -> e instanceof EntityArmorStand);
        Entity theEntity =entityList.stream().min(Comparator.comparingDouble(a -> Math.abs(a.posX - event.entityLiving.posX) + Math.abs(a.posZ - event.entityLiving.posZ))).orElse(null);
//EntityPigZombie
        if (theEntity != null)
            entityData.armorstand = theEntity.getDisplayName();

        entityData.metadata = event.entityLiving.getDataWatcher().getAllWatched();
        entityData.name = event.entityLiving.getName();

        if (event.entityLiving.getHeldItem() != null)
            entityData.armoritems[4] = event.entityLiving.getHeldItem();
        if (event.entityLiving.getCurrentArmor(0) != null)
            entityData.armoritems[0] = event.entityLiving.getCurrentArmor(0);
        if (event.entityLiving.getCurrentArmor(1) != null)
            entityData.armoritems[1] = event.entityLiving.getCurrentArmor(1);
        if (event.entityLiving.getCurrentArmor(2) != null)
            entityData.armoritems[2] = event.entityLiving.getCurrentArmor(2);
        if (event.entityLiving.getCurrentArmor(3) != null)
            entityData.armoritems[3] = event.entityLiving.getCurrentArmor(3);
    }

    @DGEventHandler
    public void onEntityDespawn(LivingDeathEvent event) {
        System.out.println("Entity died!!");
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onWorldLoad(WorldEvent.Unload event) {
        map.clear();
    }

    @DGEventHandler
    public void onRender(RenderWorldLastEvent event) {
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null) return;

        Entity hovered = Minecraft.getMinecraft().pointedEntity;
        if (hovered == null) return;
        EntityData entityData = map.get(hovered.getEntityId());
        if (entityData == null) {
            RenderUtils.drawTextAtWorld("??Unknown??", (float) hovered.posX, (float) hovered.posY+3, (float) hovered.posZ, 0xFF000000, 0.02f, false, true, event.partialTicks);
        } else {
            if (entityData.getArmorstand() != null)
                RenderUtils.drawTextAtWorld(entityData.getArmorstand().getFormattedText(), (float) hovered.posX, (float) hovered.posY+3, (float) hovered.posZ, 0xFF000000, 0.02f, false, true, event.partialTicks);
            RenderUtils.drawTextAtWorld(entityData.getType(), (float) hovered.posX, (float) hovered.posY+3.2f, (float) hovered.posZ, 0xFF00FF00, 0.02f, false, true, event.partialTicks);
            RenderUtils.renderBeaconBeam(
                    entityData.getSpawnLocation().xCoord,
                    entityData.getSpawnLocation().yCoord,
                    entityData.getSpawnLocation().zCoord,
                    new AColor(0, 255, 0, 255),
                    event.partialTicks
            );
        }
    }

    @DGEventHandler
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null) return;

    }
}
