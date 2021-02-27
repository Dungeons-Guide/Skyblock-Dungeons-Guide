package kr.syeyoung.dungeonsguide.eventlistener;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.events.TitleEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
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
        if (packet instanceof S45PacketTitle) {
            MinecraftForge.EVENT_BUS.post(new TitleEvent((S45PacketTitle) packet));
        }
        super.channelRead(ctx, packet);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Packet packet = (Packet) msg;
        if (packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet2 = (C02PacketUseEntity) packet;
            PlayerInteractEntityEvent piee;
            if (packet2.getAction() == C02PacketUseEntity.Action.ATTACK)
                piee = new PlayerInteractEntityEvent(true, packet2.getEntityFromWorld(Minecraft.getMinecraft().theWorld));
            else
                piee = new PlayerInteractEntityEvent(false, ((C02PacketUseEntity) packet).getEntityFromWorld(Minecraft.getMinecraft().theWorld));

            if (MinecraftForge.EVENT_BUS.post(piee)) return;
        }
        super.write(ctx, msg, promise);
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        event.manager.channel().pipeline().addBefore("packet_handler", "dg_packet_handler", this);
    }
}
