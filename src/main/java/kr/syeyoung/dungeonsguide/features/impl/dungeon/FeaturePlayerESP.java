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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
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


    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

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
        GlStateManager.depthFunc(GL11.GL_GEQUAL);
        Entity entity = renderPlayerEvent.entityPlayer;
        float f = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * renderPlayerEvent.partialRenderTick;
        try {
            renderPlayerEvent.renderer.doRender((AbstractClientPlayer) renderPlayerEvent.entityPlayer, renderPlayerEvent.x, renderPlayerEvent.y, renderPlayerEvent.z, f, renderPlayerEvent.partialRenderTick);
        } catch (Throwable t) {}
        preCalled = false;
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
    }

    @Override
    public void onEntityRenderPost(RenderPlayerEvent.Post renderPlayerEvent) {
    }
}
