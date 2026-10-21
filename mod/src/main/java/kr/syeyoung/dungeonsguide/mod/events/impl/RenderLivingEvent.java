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

package kr.syeyoung.dungeonsguide.mod.events.impl;

import kr.syeyoung.dungeonsguide.mod.events.Cancelable;
import kr.syeyoung.dungeonsguide.mod.events.DGEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.LivingEntity;

// fired when minecraft decides to render the entity this frame, cancel to not render it
// TODO: on 26.1 entities are drawn later from render states, so drawing inside this event does not work like 1.8.9. ported with rendering
public abstract class RenderLivingEvent extends DGEvent implements Cancelable {
    public final LivingEntity entity;
    public final EntityRenderer<?, ?> renderer;
    public final float partialRenderTick;
    // entity position relative to the camera
    public final double x;
    public final double y;
    public final double z;

    @Getter @Setter
    private boolean canceled;

    protected RenderLivingEvent(LivingEntity entity, EntityRenderer<?, ?> renderer, float partialRenderTick, double x, double y, double z) {
        this.entity = entity;
        this.renderer = renderer;
        this.partialRenderTick = partialRenderTick;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static class Pre extends RenderLivingEvent {
        public Pre(LivingEntity entity, EntityRenderer<?, ?> renderer, float partialRenderTick, double x, double y, double z) {
            super(entity, renderer, partialRenderTick, x, y, z);
        }
    }
}
