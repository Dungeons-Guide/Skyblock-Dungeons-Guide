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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

// client side, main hand only (1.8.9 had one hand). cancel to stop the interaction
public class PlayerInteractEvent extends DGEvent implements Cancelable {
    public final Action action;
    // null for RIGHT_CLICK_AIR
    public final BlockPos pos;
    public final Direction face;
    public final Level world;
    public final Player entityPlayer;

    @Getter @Setter
    private boolean canceled;

    public enum Action {
        RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK, LEFT_CLICK_BLOCK
    }

    public PlayerInteractEvent(Action action, BlockPos pos, Direction face, Level world, Player entityPlayer) {
        this.action = action;
        this.pos = pos;
        this.face = face;
        this.world = world;
        this.entityPlayer = entityPlayer;
    }
}
