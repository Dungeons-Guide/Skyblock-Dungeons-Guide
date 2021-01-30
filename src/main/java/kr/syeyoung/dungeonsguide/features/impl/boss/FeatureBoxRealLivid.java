package kr.syeyoung.dungeonsguide.features.impl.boss;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorLivid;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;


public class FeatureBoxRealLivid extends SimpleFeature implements WorldRenderListener {
    public FeatureBoxRealLivid() {
        super("Bossfight", "Box Real Livid", "Box Real Livid in bossfight", "bossfight.realLividBox", true);
        parameters.put("color", new FeatureParameter<AColor>("color", "Highlight Color", "Highlight Color of Skeleton master", new AColor(0,255,0,150), "acolor"));
    }


    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public void drawWorld(float partialTicks) {
        if (!isEnabled()) return;
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext().getBossfightProcessor() == null) return;
        if (!(skyblockStatus.getContext().getBossfightProcessor() instanceof BossfightProcessorLivid)) return;
        EntityOtherPlayerMP playerMP = ((BossfightProcessorLivid) skyblockStatus.getContext().getBossfightProcessor()).getRealLivid();

        Color c = this.<Color>getParameter("color").getValue();
        RenderUtils.highlightBox(playerMP, AxisAlignedBB.fromBounds(-0.4,-1.5,-0.4,0.4,0,0.4), c, partialTicks, true);
    }
}
