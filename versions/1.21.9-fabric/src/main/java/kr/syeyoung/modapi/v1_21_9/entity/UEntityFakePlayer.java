package kr.syeyoung.modapi.v1_21_9.entity;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.modapi.entity.UEntityPlayerFake;
import kr.syeyoung.modapi.item.UItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class UEntityFakePlayer extends UEntityPlayerImpl implements UEntityPlayerFake {

    private final FakePlayer player;
    public UEntityFakePlayer(FakePlayer delegate) {
        super(delegate);
        this.player = delegate;
    }

    @Override
    public void setCurrentArmor(int i, UItemStack itemStack) {
        ItemStack item =  itemStack == null ? ItemStack.EMPTY : (ItemStack) itemStack.getItemStack();
        if (i == 0) player.equipStack(EquipmentSlot.HEAD, item);
        else if (i == 1) player.equipStack(EquipmentSlot.CHEST, item);
        else if (i == 2) player.equipStack(EquipmentSlot.LEGS, item);
        else if (i == 3) player.equipStack(EquipmentSlot.FEET, item);
    }

    @Override
    public void setCurrentItem(int i) {
        player.getInventory().setSelectedSlot(i);
    }

    @Override
    public void setMainInventory(int i, UItemStack itemStack) {
        player.getInventory().getMainStacks().set(i, itemStack == null ? ItemStack.EMPTY : (ItemStack) itemStack.getItemStack());
    }


    public static class FakePlayer extends OtherClientPlayerEntity {
//        private SkinFetcher.SkinSet skinSet;

        private FakePlayer(ClientWorld w) {
            super(w, null);
            throw new UnsupportedOperationException("what");
        }

        public FakePlayer(GameProfile playerProfile) {
            super(MinecraftClient.getInstance().world, playerProfile);
        }
    }


    public static UEntityFakePlayer createFakePlayer(UUID uuid, String name){
        return new UEntityFakePlayer(new FakePlayer(new GameProfile(uuid, name)));
    }
}
