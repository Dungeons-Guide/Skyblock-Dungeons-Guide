/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight;

import com.mojang.authlib.properties.Property;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;

public class BossfightProcessorScarf extends GeneralBossfightProcessor {
    public BossfightProcessorScarf(boolean isMasterMode) {
        super(isMasterMode ? "MASTERMODE_CATACOMBS_FLOOR_TWO" : "CATACOMBS_FLOOR_TWO");
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: This is where the journey ends for you, Adventurers.§r")
                .nextPhase("fight-1").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: ARISE, MY CREATIONS!§r")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: Those toys are not strong enough I see.§r")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: Did you forget? I was taught by the best! Let's dance.§r")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: Whatever...§r").build()
        );
        this.isMasterMode = isMasterMode;
    }

    private boolean isMasterMode;

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        {
            long health = 0;
            if (scarfStand != null) {
                String name = TextUtils.stripColor(scarfStand.getName());
                String healthPart = name.split(" ")[2];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            }
            healths.add(new HealthData("Scarf", (int) health, isMasterMode ? 375000000 : 1000000, this.getCurrentPhase().equals("fight-2")));
        }
        if (!getCurrentPhase().equals("start") && !getCurrentPhase().equals("final-defeat")) {
            {
                long health = 0;
                if (priestStand != null) {
                    String name = TextUtils.stripColor(priestStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Priest", (int) health, isMasterMode ? 90000000 : 600000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (berserkStand != null) {
                    String name = TextUtils.stripColor(berserkStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Warrior", (int) health, isMasterMode ? 75000000 : 500000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (mageStand != null) {
                    String name = TextUtils.stripColor(mageStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Mage", (int) health, isMasterMode ? 60000000 : 400000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (archerStand != null) {
                    String name = TextUtils.stripColor(archerStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Archer", (int) health, isMasterMode ? 60000000 : 400000, this.getCurrentPhase().startsWith("fight-")));
            }
        }
        return healths;
    }

    @Override
    public String getBossName() {
        return "Scarf";
    }

    private EntityArmorStand scarfStand;
    private EntityArmorStand priestStand;
    private EntityArmorStand mageStand;
    private EntityArmorStand berserkStand;
    private EntityArmorStand archerStand;
    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand) {
            if (updateEvent.entityLiving.getName().startsWith("§e﴾ §c§lScarf§r"))
                scarfStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().contains("§6§4§lUndead Archer"))
                archerStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().contains("§6§4§lUndead Mage"))
                mageStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().contains("§6§4§lUndead Priest"))
                priestStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().contains("§6§4§lUndead Warrior"))
                berserkStand = (EntityArmorStand) updateEvent.entityLiving;
        }
    }

    @Override
    public MarkerData convertToMarker(Entity entity) {
        if (entity instanceof EntityOtherPlayerMP) {
            if ("Scarf ".equals(entity.getName())) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.BOSS, 8);
            }
            Collection<Property> obj = ((EntityOtherPlayerMP) entity).getGameProfile().getProperties().get("textures");
            String texture = obj.stream().findFirst().map(Property::getValue).orElse(null);
            if (texture.equals("ewogICJ0aW1lc3RhbXAiIDogMTU4OTk5NDg1NjMyMCwKICAicHJvZmlsZUlkIiA6ICJkYTQ5OGFjNGU5Mzc0ZTVjYjYxMjdiMzgwODU1Nzk4MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaXRyb2hvbGljXzIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFlZjU2Y2QwNGQwMzlhMzRjZDZmMTZlMDIzYjZlNjNmY2M3MmYzN2Y3NTk1YTJjOWU0YTE5Zjk0ZTI3M2I0MiIKICAgIH0KICB9Cn0=")) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.MINIBOSS, 9); // priest
            } else if (texture.equals("eyJ0aW1lc3RhbXAiOjE1NzkxMDg2MTYxMjYsInByb2ZpbGVJZCI6IjJjMTA2NGZjZDkxNzQyODI4NGUzYmY3ZmFhN2UzZTFhIiwicHJvZmlsZU5hbWUiOiJOYWVtZSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBiYzI5ODg5M2FhMGZhYmQ1MjUwY2RjNTMxYmE4MmVhN2M3MmQwYzE5N2E4NzA4NTIzNGE5NDYzNTEwZmY1MCJ9fX0=")) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.MINIBOSS, 12); // mage
            } else if (texture.equals("eyJ0aW1lc3RhbXAiOjE1NzkxMDg1ODc3MDEsInByb2ZpbGVJZCI6IjczODJkZGZiZTQ4NTQ1NWM4MjVmOTAwZjg4ZmQzMmY4IiwicHJvZmlsZU5hbWUiOiJZYU9PUCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTNjYzQzYmJjNjA4N2QwYzBkYzgyNWM3OGI3NTViYjhhYzFiN2UyMzZlZjNlNWM4MmFjODUwNzc0MGFkM2JlZiJ9fX0=")) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.MINIBOSS, 10); // archer
            } else if (texture.equals("eyJ0aW1lc3RhbXAiOjE1NzkwMzU2NjQ5ODEsInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJNaW5lU2tpbl9vcmciLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RlNTRjMjE0NDVjNTg4Mjc4Mjk4YzFhMThlOTM0Mzk4ZDRlYTQ4NjIzYTJkZjVlZTU2OWY0NDg0OGJjODg5YjgifX19")) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.MINIBOSS, 11); // warrior
            }
        }
        return null;
    }
}
