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
import lombok.Getter;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class HoverEventRenderPlayer extends HoverEvent {
    @Getter
    private final GameProfile gameProfile;
    private Component cached;

    public HoverEventRenderPlayer(GameProfile gameProfile) {
        super(Action.SHOW_TEXT, Component.literal(""));
        this.gameProfile = Objects.requireNonNull(gameProfile);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HoverEventRenderPlayer that = (HoverEventRenderPlayer) o;
        return Objects.equals(gameProfile.getId(), that.gameProfile.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gameProfile.getId());
    }

    @Override
    public Component getValue() {
        if (cached == null) {
            cached = Component.literal("")
                    .setStyle(
                            Style.EMPTY
                                    .withHoverEvent(
                                            new HoverEvent(
                                                    Action.SHOW_TEXT,
                                                    Component.literal(gameProfile.getId().toString())
                                            )
                                    )
                    );
            return cached;
        }
        return cached;
    }
}
