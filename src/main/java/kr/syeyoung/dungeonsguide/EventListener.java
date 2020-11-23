package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.doorfinder.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

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
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;
        int[] textureData = dynamicTexture.getTextureData();
        MapUtils.getImage().getRGB(0,0,128,128, textureData, 0, 128);
        dynamicTexture.updateDynamicTexture();
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GuiScreen.drawModalRectWithCustomSizedTexture(0,0, 0, 0, 128, 128, 128, 128);

        if (skyblockStatus.getContext() != null) {
            DungeonContext context = skyblockStatus.getContext();
            EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
            Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());

            DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            if (dungeonRoom == null) {
                fontRenderer.drawString("Where are you?!", 5, 128, 0xFFFFFF);
            } else {
                fontRenderer.drawString("you're in the room... "+dungeonRoom.getColor()+" / "+dungeonRoom.getShape(), 5, 128, 0xFFFFFF);
                fontRenderer.drawString("room uuid: "+dungeonRoom.getDungeonRoomInfo().getUuid() + (dungeonRoom.getDungeonRoomInfo().isRegistered() ?"":" (not registered)"), 5, 138, 0xFFFFFF);
                fontRenderer.drawString("room name: "+dungeonRoom.getDungeonRoomInfo().getName(), 5, 148, 0xFFFFFF);
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent renderWorldLastEvent) {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) return;

        DungeonContext context = skyblockStatus.getContext();
        if (context == null) return;

        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            for(DungeonDoor door : dungeonRoom.getDoors()) {
                RenderUtils.renderDoor(door, renderWorldLastEvent.partialTicks);
            }
        }
    }
}
