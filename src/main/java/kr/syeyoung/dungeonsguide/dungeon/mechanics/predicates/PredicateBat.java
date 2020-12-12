package kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import org.jetbrains.annotations.Nullable;


public class PredicateBat implements Predicate<Entity> {

    public static final PredicateBat INSTANCE = new PredicateBat();

    @Override
    public boolean apply(@Nullable Entity input) {
        return input instanceof EntityBat;
    }
}
