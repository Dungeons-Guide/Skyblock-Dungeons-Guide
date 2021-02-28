package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.EntityLivingRenderListener;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorThorn;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.*;
import net.minecraftforge.client.event.RenderLivingEvent;


public class FeatureHideNameTags extends SimpleFeature implements EntityLivingRenderListener {
    public FeatureHideNameTags() {
        super("Dungeon", "Hide mob nametags", "Hide mob nametags. Developer personally don't suggest using this, it will be most likely annoying.", "dungeon.hidenametag", false);
    }


    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    @Override
    public void onEntityRenderPre(RenderLivingEvent.Pre renderPlayerEvent) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;

        if (renderPlayerEvent.entity instanceof EntityArmorStand) {
            EntityArmorStand armorStand = (EntityArmorStand) renderPlayerEvent.entity;
            if (armorStand.getAlwaysRenderNameTag())
                renderPlayerEvent.setCanceled(true);
        }
    }

    @Override
    public void onEntityRenderPost(RenderLivingEvent.Post renderPlayerEvent) {

    }
}
