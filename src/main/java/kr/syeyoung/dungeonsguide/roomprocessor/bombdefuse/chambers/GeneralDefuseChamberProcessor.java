package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

public abstract class GeneralDefuseChamberProcessor  implements ChamberProcessor{
    @Override
    public void onDataRecieve(NBTTagCompound compound) {

    }

    @Override
    public void tick() {

    }

    @Override
    public void drawScreen(float partialTicks) {

    }

    @Override
    public void drawWorld(float partialTicks) {

    }

    @Override
    public void chatReceived(IChatComponent chat) {

    }

    @Override
    public void actionbarReceived(IChatComponent chat) {

    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }

    @Override
    public void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {

    }

    @Override
    public void onEntitySpawn(LivingEvent.LivingUpdateEvent updateEvent) {

    }
}
