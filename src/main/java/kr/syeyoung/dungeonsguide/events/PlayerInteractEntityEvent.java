package kr.syeyoung.dungeonsguide.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

@AllArgsConstructor
public class PlayerInteractEntityEvent extends Event {

    @Getter @Setter
    private boolean attack;
    @Getter @Setter
    private Entity entity;

    @Override
    public boolean isCancelable() {
        return true;
    }
}
