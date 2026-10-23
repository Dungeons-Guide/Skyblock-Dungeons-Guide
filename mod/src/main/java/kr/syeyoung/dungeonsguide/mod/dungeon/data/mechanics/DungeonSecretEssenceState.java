/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;

@Data
public class DungeonSecretEssenceState implements DungeonMechanicState, ISecret {
    private final DungeonSecretEssenceData data;
    private final DungeonRoom room;

    public DungeonSecretEssenceState(DungeonSecretEssenceData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    private boolean essenceWasThere = false;
    private boolean found = false;
    private int nearbyTicks = 0;
    public void tick(DungeonRoom dungeonRoom) {
        BlockPos pos = data.secretPoint.getBlockPos(dungeonRoom);
        BlockState blockState = dungeonRoom.getCachedWorld().getBlockState(pos);
        if (LegacyBlock.SKULL.is(blockState)) {
            essenceWasThere = true;
            List<ArmorStand> entities = Minecraft.getInstance().level.getEntitiesOfClass(ArmorStand.class, new AABB(pos.getX(),pos.getY()-3,pos.getZ(), pos.getX()+1, pos.getY()+2, pos.getZ()+1));
            BlockEntity tileEntity = dungeonRoom.getCachedWorld().getBlockEntity(pos);

            if (pos.distToLowCornerSqr(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ()) < 25) {
                if (tileEntity instanceof SkullBlockEntity) {
                    String texture = Optional.ofNullable(((SkullBlockEntity) tileEntity).getOwnerProfile())
                            .map(a -> a.partialProfile().properties())
                            .map(a -> a.get("textures"))
                            .flatMap(a -> a.stream().findFirst())
                            .map(a -> a.value()).orElse(null);

                    if (texture == null) return;
                    for (ArmorStand entity : entities) {
                        ItemStack itemStackIn = entity.getItemBySlot(EquipmentSlot.HEAD);
                        System.out.println(itemStackIn);
                        if (itemStackIn.isEmpty()) continue;
                        // 1.8.9's skull item, a player head for the SkullOwner
                        if (!itemStackIn.is(Items.PLAYER_HEAD)) continue;

                        // 1.8.9's SkullOwner nbt, the profile component on 26.1 (D125)
                        ResolvableProfile profile = itemStackIn.get(DataComponents.PROFILE);
                        if (profile != null) {
                            GameProfile gameprofile = profile.partialProfile();

                            if (texture.equals(gameprofile.properties().get("textures").stream().findFirst().map(a -> a.value()).orElse(null))) {
                                found = true;
                                return;
                            }
                        }
                    }
                }
            }
        } else if (LegacyBlock.AIR.is(blockState) && essenceWasThere) {
            found = true;
        } else if (LegacyBlock.AIR.is(blockState) && !essenceWasThere) {
            if (pos.distToLowCornerSqr(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ()) < 25) {
                nearbyTicks++;
            }
            if (nearbyTicks > 100) {
                essenceWasThere = true;
                found = true;
            }
        }

    }

    @Override
    public boolean isFound(DungeonRoom dungeonRoom) {
        return getSecretStatus(dungeonRoom) == SecretStatus.FOUND;
    }

    public SecretStatus getSecretStatus(DungeonRoom dungeonRoom) {
        if (found) return SecretStatus.FOUND;
        if (essenceWasThere) return SecretStatus.DEFINITELY_NOT;
        return SecretStatus.NOT_SURE;
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }
        if (!"found".equalsIgnoreCase(action))
            throw new PathfindImpossibleException(action + " is not valid state for secret");
        if (action.equals("found") && getSecretStatus(room) == SecretStatus.FOUND) return;


        if (data.secretCache != null)
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretCache, data.preRequisite, Collections.emptyList(), algorithmSetting);
        else
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretPoint, builder1 -> {
                for (String str : data.preRequisite) {
                    if (str.isEmpty()) continue;
                    builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
                }
                return null;
            }, algorithmSetting);
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        BlockPos pos = getSecretPoint().getBlockPos(room);
        RenderUtils.highlightBlockStencil(pos, partialTicks, color, false);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.375f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);


        if (data.secretCache != null && FeatureRegistry.DEBUG_ST.isEnabled())
            data.secretCache.render(partialTicks, room);
    }

    public enum SecretType {
        BAT, CHEST, ITEM_DROP, ESSENCE
    }

    @AllArgsConstructor
    @Getter
    public enum SecretStatus {
        DEFINITELY_NOT("definitely_not"), NOT_SURE("not_sure"), CREATED("created"), FOUND("found"), ERROR("error");

        private final String stateName;
    }


    @Override
    public String getCurrentState() {
        return getSecretStatus(room).getStateName();
    }

    @Override
    public Set<String> getAvailableActions() {
        SecretStatus status = getSecretStatus(room);
        if (status == SecretStatus.FOUND) return Sets.newHashSet("navigate");
        else return Sets.newHashSet("found", "navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("found"/*, "definitely_not", "not_sure", "created", "error"*/);
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Override
    public OffsetPoint getSecretPoint() {
        return data.secretPoint;
    }

    @Override
    public List<String> getPreRequisite() {
        return data.preRequisite;
    }

    @Data
    public static class DungeonSecretEssenceData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedStonk secretCache;
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonSecretEssenceData() {
        }

        @Override
        public DungeonSecretEssenceState createState(DungeonRoom room) {
            return new DungeonSecretEssenceState(this, room);
        }

        public DungeonSecretEssenceData clone() throws CloneNotSupportedException {
            DungeonSecretEssenceData data = new DungeonSecretEssenceData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.secretCache = secretCache;
            data.preRequisite = new ArrayList<String>(preRequisite);
            ;
            return data;
        }
    }
}
