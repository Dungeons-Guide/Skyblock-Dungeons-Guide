package kr.syeyoung.dungeonsguide.eventlistener;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.lang.reflect.Field;

@ChannelHandler.Sharable
public class PacketListener extends ChannelDuplexHandler {
    private SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Packet packet = (Packet) msg;
        if (skyblockStatus.isOnSkyblock()
                && msg instanceof S04PacketEntityEquipment
                && FeatureRegistry.FIX_SPIRIT_BOOTS.isEnabled()) { // Inventory packet name
            S04PacketEntityEquipment packet2 = (S04PacketEntityEquipment) msg;
            if (packet2.getEntityID() == Minecraft.getMinecraft().thePlayer.getEntityId()) {
                packet2 = new S04PacketEntityEquipment(packet2.getEntityID(), packet2.getEquipmentSlot() + 1, packet2.getItemStack());
                packet = packet2;
            }
        }
        super.channelRead(ctx, packet);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (event.manager.channel().pipeline().get("dg_packet_handler") != null)
            event.manager.channel().pipeline().remove(this);
        try {
            event.manager.channel().pipeline().addBefore("packet_handler", "dg_packet_handler", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
