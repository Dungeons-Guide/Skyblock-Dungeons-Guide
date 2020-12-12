package kr.syeyoung.dungeonsguide.dungeon.mechanics.predicates;

import com.google.common.base.Predicate;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PredicateSuperBoom implements Predicate<ItemStack> {

    public static final PredicateSuperBoom INSTANCE = new PredicateSuperBoom();

    @Override
    public boolean apply(@Nullable ItemStack input) {
        return false;
    }
}
