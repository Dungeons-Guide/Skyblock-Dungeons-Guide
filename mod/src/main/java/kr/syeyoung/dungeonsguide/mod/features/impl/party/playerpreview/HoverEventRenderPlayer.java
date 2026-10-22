/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.ExtraCodecs;

import java.util.Objects;

public class HoverEventRenderPlayer implements HoverEvent {
    public static final MapCodec<HoverEventRenderPlayer> CODEC = ExtraCodecs.STORED_GAME_PROFILE.xmap(HoverEventRenderPlayer::new, HoverEventRenderPlayer::getGameProfile);
    // added to HoverEvent.Action by HoverEventActionMixin
    private static HoverEvent.Action action;

    @Getter
    private final GameProfile gameProfile;

    public HoverEventRenderPlayer(GameProfile gameProfile) {
        this.gameProfile = Objects.requireNonNull(gameProfile);
    }

    @Override
    public HoverEvent.Action action() {
        if (action == null) action = HoverEvent.Action.valueOf("DUNGEONSGUIDE_RENDER_PLAYER");
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoverEventRenderPlayer that = (HoverEventRenderPlayer) o;
        return Objects.equals(gameProfile.id(), that.gameProfile.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameProfile.id());
    }

    // 1.8.9's getValue (a text hover carrying the uuid) has no 26.1 counterpart: vanilla draws no tooltip for this action
}
