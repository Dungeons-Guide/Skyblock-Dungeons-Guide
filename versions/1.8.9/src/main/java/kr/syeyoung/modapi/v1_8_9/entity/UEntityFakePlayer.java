package kr.syeyoung.modapi.v1_8_9.entity;

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.modapi.entity.UEntityPlayerFake;
import kr.syeyoung.modapi.item.UItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.UUID;

public class UEntityFakePlayer extends UEntityPlayerImpl implements UEntityPlayerFake {

    private final FakePlayer player;
    public UEntityFakePlayer(FakePlayer delegate) {
        super(delegate);
        this.player = delegate;
    }

    @Override
    public void setCurrentArmor(int i, UItemStack itemStack) {
        player.inventory.armorInventory[i] = (ItemStack) itemStack.getItemStack();
    }

    @Override
    public void setCurrentItem(int i) {
        player.inventory.currentItem = i;
    }

    @Override
    public void setMainInventory(int i, UItemStack itemStack) {
        player.inventory.mainInventory[i] = (ItemStack) itemStack.getItemStack();
    }


    public static class FakePlayer extends EntityOtherPlayerMP {
        private SkinFetcher.SkinSet skinSet;

        private FakePlayer(World w) {
            super(w, null);
            throw new UnsupportedOperationException("what");
        }

        public FakePlayer(GameProfile playerProfile) {
            super(Minecraft.getMinecraft().theWorld, playerProfile);

            SkinFetcher.getSkinSet(playerProfile).whenComplete((s, e) -> this.skinSet = s);

            this.inventory.armorInventory = new ItemStack[4];
            this.inventory.mainInventory[0] = null;
            this.inventory.currentItem = 0;
        }

        public String getSkinType() {
            return this.skinSet == null ? DefaultPlayerSkin.getSkinType(getGameProfile().getId()) : this.skinSet.getSkinType();
        }

        public ResourceLocation getLocationSkin() {
            return Objects.firstNonNull(skinSet != null ? skinSet.getSkinLoc() : null, DefaultPlayerSkin.getDefaultSkin(getGameProfile().getId()));
        }

        public ResourceLocation getLocationCape() {
            return skinSet != null ? skinSet.getSkinLoc() : null;
        }

        @Override
        public boolean isInvisibleToPlayer(EntityPlayer player) {
            return true;
        }

        @Override
        public Team getTeam() {
            return new ScorePlayerTeam(null, null) {
                @Override
                public EnumVisible getNameTagVisibility() {
                    return EnumVisible.NEVER;
                }
            };
        }
    }


    public static UEntityFakePlayer createFakePlayer(UUID uuid, String name){
        return new UEntityFakePlayer(new FakePlayer(new GameProfile(uuid, name)));
    }
}
