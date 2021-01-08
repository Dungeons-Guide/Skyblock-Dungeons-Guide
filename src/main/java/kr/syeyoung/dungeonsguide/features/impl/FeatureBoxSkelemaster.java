package kr.syeyoung.dungeonsguide.features.impl;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;


public class FeatureBoxSkelemaster extends SimpleFeature implements WorldRenderListener {
    public FeatureBoxSkelemaster() {
        super("Dungeon", "Box Skeleton Masters", "Box skeleton masters in dungeons", "dungeon.skeletonmasterbox", true);
        parameters.put("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and skeletonmaster to be boxed", 20, "integer"));
        parameters.put("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Skeleton master", new AColor(255,0,0,50), "acolor"));
    }


    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;

        final BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;

        List<EntityArmorStand> skeletonList = Minecraft.getMinecraft().theWorld.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(@Nullable EntityArmorStand input) {
                if (player.distanceSq(input.getPosition()) > sq) return false;
                return input.getName().contains("Skeleton Master");
            }
        });
        Color c = this.<Color>getParameter("color").getValue();
        for (EntityArmorStand entitySkeleton : skeletonList) {
            RenderUtils.highlightBox(entitySkeleton, c, partialTicks, false);
        }
    }
}
