package kr.syeyoung.modapi.v1_21_9.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.channel.ChannelHandlerContext;
import kr.syeyoung.modapi.v1_21_9.PacketListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Shadow
    @Final
    private NetworkSide side;

    @WrapMethod(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V")
    private void onNettyReceivePacket(ChannelHandlerContext context, Packet<?> packet, Operation<Void> original) {
        if (packet instanceof BundlePacket<?> bundle) {
            for (Packet<?> subPacket : bundle.getPackets()) {
                PacketListener.INSTANCE.onPacketReceive(subPacket);
            }
        } else {
            PacketListener.INSTANCE.onPacketReceive(packet);
        }
        original.call(context, packet);
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"))
    private void onSendPacket(CallbackInfo ci) {

    }
}
