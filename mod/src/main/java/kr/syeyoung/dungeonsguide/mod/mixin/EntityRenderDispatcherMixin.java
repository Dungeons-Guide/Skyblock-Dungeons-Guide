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

import kr.syeyoung.dungeonsguide.launcher.FabricEventBridge;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderLivingEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    // 1.8.9: RenderPlayerEvent.Pre / RenderLivingEvent.Pre, only for entities that are actually rendered
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private void dungeonsguide$shouldRender(Entity entity, Frustum frustum, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() || !(entity instanceof LivingEntity living)) return;

        float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        double x = Mth.lerp(partialTicks, entity.xo, entity.getX()) - camX;
        double y = Mth.lerp(partialTicks, entity.yo, entity.getY()) - camY;
        double z = Mth.lerp(partialTicks, entity.zo, entity.getZ()) - camZ;
        EntityRenderer<?, ?> renderer = ((EntityRenderDispatcher) (Object) this).getRenderer(entity);

        if (entity instanceof Player player && FabricEventBridge.fire(new RenderPlayerEvent.Pre(player, renderer, partialTicks, x, y, z))) {
            cir.setReturnValue(false);
            return;
        }
        if (FabricEventBridge.fire(new RenderLivingEvent.Pre(living, renderer, partialTicks, x, y, z)))
            cir.setReturnValue(false);
    }
}
