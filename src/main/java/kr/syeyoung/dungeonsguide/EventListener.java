package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;

public class EventListener {
    private int timerTick = 0;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            timerTick ++;
            if (timerTick % 5 == 0) {
                SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
                boolean isOnDungeon = skyblockStatus.isOnDungeon();
//                System.out.println(isOnDungeon);
                skyblockStatus.updateStatus();
                if (!skyblockStatus.isOnDungeon()) {
                    skyblockStatus.setContext(null);
                    MapUtils.clearMap();
                    return;
                }
                if (isOnDungeon) skyblockStatus.getContext().tick();
                else skyblockStatus.setContext(new DungeonContext(Minecraft.getMinecraft().thePlayer.worldObj));
            }
        }
    }

    DynamicTexture dynamicTexture = new DynamicTexture(128, 128);
    ResourceLocation location = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("dungeons/map/", dynamicTexture);
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        int[] textureData = dynamicTexture.getTextureData();
        MapUtils.getImage().getRGB(0,0,128,128, textureData, 0, 128);
        dynamicTexture.updateDynamicTexture();
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GuiScreen.drawModalRectWithCustomSizedTexture(0,0, 0, 0, 128, 128, 128, 128);
    }
}
