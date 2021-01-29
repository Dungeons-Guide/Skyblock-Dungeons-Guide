package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
import java.util.Collection;
import java.util.Set;

public class CatacombDataProvider implements DungeonSpecificDataProvider {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    @Override
    public BlockPos findDoor(World w, String dungeonName) {
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
    public Vector2d findDoorOffset(World w, String dungeonName) {
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
    /*
     *
    * */

    @Override
    public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        e.sendDebugChat(new ChatComponentText("Floor: "+floor+ " Building bossfight processor"));
        if (floor.equals("F1")) {
            return new BossfightProcessorBonzo();
        } else if (floor.equals("F2")) {
            return new BossfightProcessorScarf();
        } else if (floor.equals("F3")) {
            return new BossfightProcessorProf();
        } else if (floor.equals("F4")) {
            return new BossfightProcessorThorn();
        } else if (floor.equals("F5")) {
            return new BossfightProcessorLivid();
        }
        return null;
    }
}
