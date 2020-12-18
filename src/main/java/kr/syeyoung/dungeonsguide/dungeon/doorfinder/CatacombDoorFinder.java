package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
import java.util.Collection;
import java.util.Set;

public class CatacombDoorFinder implements StartDoorFinder {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    @Override
    public BlockPos find(World w) {
        Collection<EntityArmorStand> armorStand = w.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(EntityArmorStand input) {
                return input.getName().equals("§bMort");
            }
        });

        if (armorStand.size() != 0) {
            EntityArmorStand mort = armorStand.iterator().next();
            BlockPos pos = mort.getPosition();
            pos = pos.add(0, 3, 0);
            for (int i = 0; i < 5; i++) {
                for (Vector2d vector2d:directions) {
                    BlockPos test = pos.add(vector2d.x * i, 0, vector2d.y * i);
                    if (w.getChunkFromBlockCoords(test).getBlock(test) == Blocks.iron_bars) {
                        return pos.add(vector2d.x * (i + 2), -2, vector2d.y * (i+2));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Vector2d offset(World w) {
        Collection<EntityArmorStand> armorStand = w.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(EntityArmorStand input) {
                return input.getName().equals("§bMort");
            }
        });

        if (armorStand.size() != 0) {
            EntityArmorStand mort = armorStand.iterator().next();
            BlockPos pos = mort.getPosition();
            pos = pos.add(0, 3, 0);
            for (int i = 0; i < 5; i++) {
                for (Vector2d vector2d:directions) {
                    BlockPos test = pos.add(vector2d.x * i, 0, vector2d.y * i);
                    if (w.getChunkFromBlockCoords(test).getBlock(test) == Blocks.iron_bars) {
                        return vector2d;
                    }
                }
            }
        }
        return null;
    }
}
