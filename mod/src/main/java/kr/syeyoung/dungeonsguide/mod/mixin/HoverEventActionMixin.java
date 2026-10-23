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

import com.mojang.serialization.MapCodec;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.HoverEventRenderPlayer;
import net.minecraft.network.chat.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;

// 1.8.9 subclassed HoverEvent for HoverEventRenderPlayer; 26.1's actions are a closed enum, DG's action is added here.
// not allowed from the server, so the action codec rejects it
@Mixin(HoverEvent.Action.class)
public enum HoverEventActionMixin {
    DUNGEONSGUIDE_RENDER_PLAYER("dungeonsguide:render_player", false, HoverEventRenderPlayer.CODEC);

    // shadows HoverEvent.Action's constructor
    HoverEventActionMixin(String name, boolean allowFromServer, MapCodec<? extends HoverEvent> codec) {
    }
}
