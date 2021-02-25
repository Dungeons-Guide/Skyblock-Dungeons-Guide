package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class FeatureBoxBats extends SimpleFeature implements WorldRenderListener {
    public FeatureBoxBats() {
        super("Dungeon", "Box Bats", "Box bats in dungeons\nDoes not appear through walls", "dungeon.batbox", true);
        parameters.put("radius", new FeatureParameter<Integer>("radius", "Highlight Radius", "The maximum distance between player and bats to be boxed", 20, "integer"));
        parameters.put("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Bats", new AColor(255,0,0,50), "acolor"));
    }


    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;

        final BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        int val = this.<Integer>getParameter("radius").getValue();
        final int sq = val * val;

        List<EntityBat> skeletonList = Minecraft.getMinecraft().theWorld.getEntities(EntityBat.class, new Predicate<EntityBat>() {
            @Override
            public boolean apply(@Nullable EntityBat input) {
                return input != null && input.getDistanceSq(player) < sq;
            }
        });
        AColor c = this.<AColor>getParameter("color").getValue();
        for (EntityBat entitySkeleton : skeletonList) {
            RenderUtils.highlightBox(entitySkeleton, c, partialTicks, true);
        }
    }
}
