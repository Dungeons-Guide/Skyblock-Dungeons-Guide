package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.BossfightProcessorLivid;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.GeneralBossfightProcessor;
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
        if (floor.equals("F2")) {
            GeneralBossfightProcessor bossfightProcessor = new GeneralBossfightProcessor();
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("start")
                    .signatureMsg("§r§c[BOSS] Scarf§r§f: This is where the journey ends for you, Adventurers.§r")
                    .nextPhase("fight-1").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight-1")
                    .signatureMsg("§r§c[BOSS] Scarf§r§f: ARISE, MY CREATIONS!§r")
                    .nextPhase("first-defeat").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("first-defeat")
                    .signatureMsg("§r§c[BOSS] Scarf§r§f: Those toys are not strong enough I see.§r")
                    .nextPhase("fight-2").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight-2")
                    .signatureMsg("§r§c[BOSS] Scarf§r§f: Did you forget? I was taught by the best! Let's dance.§r")
                    .nextPhase("final-defeat").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("final-defeat")
                    .signatureMsg("§r§c[BOSS] Scarf§r§f: Whatever...§r").build()
            );
            return bossfightProcessor;
        } else if (floor.equals("F1")) {
            GeneralBossfightProcessor bossfightProcessor = new GeneralBossfightProcessor();
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("start")
                    .signatureMsg("§r§c[BOSS] Bonzo§r§f: Gratz for making it this far, but I’m basically unbeatable.§r")
                    .nextPhase("fight-1").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight-1")
                    .signatureMsg("§r§c[BOSS] Bonzo§r§f: I can summon lots of undead! Check this out.§r")
                    .nextPhase("first-defeat").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("first-defeat")
                    .signatureMsg("§r§c[BOSS] Bonzo§r§f: Oh I'm dead!§r").signatureMsg("§r§c[BOSS] Bonzo§r§f: Hoho, looks like you killed me!§r")
                    .nextPhase("fight-2").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight-2")
                    .signatureMsg("§r§c[BOSS] Bonzo§r§f: Sike§r").signatureMsg("§r§c[BOSS] Bonzo§r§f: I can revive myself and become much stronger!§r")
                    .nextPhase("final-defeat").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("final-defeat")
                    .signatureMsg("§r§c[BOSS] Bonzo§r§f: Alright, maybe I'm just weak after all..§r").build()
            );
            bossfightProcessor.setBossMaxHealth(250000);
            return bossfightProcessor;
        } else if (floor.equals("F3")) {
            GeneralBossfightProcessor bossfightProcessor = new GeneralBossfightProcessor();
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("start")
                    .signatureMsg("§r§c[BOSS] The Professor§r§f: I was burdened with terrible news recently...§r")
                    .nextPhase("fight-1").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight-1")
                    .signatureMsg("§r§c[BOSS] The Professor§r§f: I'll show you real power!§r")
                    .nextPhase("first-defeat").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("first-defeat")
                    .signatureMsg("§r§c[BOSS] The Professor§r§f: Oh? You found my Guardians one weakness?§r")
                    .nextPhase("fight-2").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight-2")
                    .signatureMsg("§r§c[BOSS] The Professor§r§f: This time I'll be your opponent!§r")
                    .nextPhase("second-defeat").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("second-defeat")
                    .signatureMsg("§r§c[BOSS] The Professor§r§f: I see. You have forced me to use my ultimate technique.§r")
                    .nextPhase("fight-3").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight-3")
                    .signatureMsg("§r§c[BOSS] The Professor§r§f: The process is irreversible, but I'll be stronger than a Wither now!§r")
                    .nextPhase("final-defeat").build()
            );
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("final-defeat")
                    .signatureMsg("§r§c[BOSS] The Professor§r§f: What?! My Guardian power is unbeatable!§r").build()
            );
            return bossfightProcessor;
        } else if (floor.equals("F4")) {
            GeneralBossfightProcessor bossfightProcessor = new GeneralBossfightProcessor();
            bossfightProcessor.addPhase(GeneralBossfightProcessor.PhaseData.builder()
                    .phase("fight").build()
            );
            return bossfightProcessor;
        } else if (floor.equals("F5")) {
            return new BossfightProcessorLivid();
        }
        return null;
    }
}
