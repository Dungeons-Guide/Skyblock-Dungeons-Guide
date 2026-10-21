/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kr.syeyoung.dungeonsguide.launcher.FabricEventBridge;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
    // D105: a chunk the client already has, sent again. 1.8.9 dg listened to the chunk packet to see these block changes,
    // here the new sections are decoded from a copy of the buffer and compared with the loaded chunk
    @WrapOperation(method = "replaceWithPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;replaceWithPacketData(Lnet/minecraft/network/FriendlyByteBuf;Ljava/util/Map;Ljava/util/function/Consumer;)V", ordinal = 1))
    private void dungeonsguide$onChunkResend(LevelChunk chunk, FriendlyByteBuf buffer, Map<Heightmap.Types, long[]> heightmaps,
                                              Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> blockEntities, Operation<Void> original) {
        BlockUpdateEvent.Pre pre = new BlockUpdateEvent.Pre();
        try {
            FriendlyByteBuf copy = new FriendlyByteBuf(buffer.duplicate());
            LevelChunkSection[] sections = chunk.getSections();
            for (int i = 0; i < sections.length; i++) {
                LevelChunkSection neu = new LevelChunkSection(chunk.getLevel().palettedContainerFactory());
                neu.read(copy);
                LevelChunkSection old = sections[i];
                if (old.hasOnlyAir() && neu.hasOnlyAir()) continue;

                int baseX = chunk.getPos().getMinBlockX();
                int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
                int baseZ = chunk.getPos().getMinBlockZ();
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            BlockState newState = neu.getBlockState(x, y, z);
                            if (old.getBlockState(x, y, z) != newState)
                                pre.getUpdatedBlocks().add(new Tuple<>(new BlockPos(baseX + x, baseY + y, baseZ + z), newState));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Set<Tuple<BlockPos, BlockState>> updated = pre.getUpdatedBlocks();
        if (!updated.isEmpty()) FabricEventBridge.fire(pre);
        original.call(chunk, buffer, heightmaps, blockEntities);
        if (!updated.isEmpty()) {
            BlockUpdateEvent.Post post = new BlockUpdateEvent.Post();
            post.getUpdatedBlocks().addAll(updated);
            FabricEventBridge.fire(post);
        }
    }
}
