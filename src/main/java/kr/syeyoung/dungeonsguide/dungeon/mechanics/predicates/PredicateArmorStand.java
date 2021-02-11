package kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityBat;
import org.jetbrains.annotations.Nullable;


public class PredicateArmorStand implements Predicate<Entity> {

    public static final PredicateArmorStand INSTANCE = new PredicateArmorStand();

    @Override
    public boolean apply(@Nullable Entity input) {
        return input instanceof EntityArmorStand;
    }

    @Override
    public int hashCode() {
        return 0;
    }
    @Override
    public boolean equals(Object o) {
        return o == this || o != null && (o.getClass() == this.getClass());
    }
}
