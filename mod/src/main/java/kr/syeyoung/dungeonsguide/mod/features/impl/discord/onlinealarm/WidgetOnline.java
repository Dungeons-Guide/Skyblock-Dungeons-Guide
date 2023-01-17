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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.onlinealarm;

import kr.syeyoung.dungeonsguide.mod.discord.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.TTL;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

public class WidgetOnline extends AnnotatedWidget implements TTL {

    @Bind(variableName = "username")
    public final BindableAttribute<String> username;
    @Bind(variableName = "discriminator")
    public final BindableAttribute<String> discriminator;
    @Bind(variableName = "avatarUrl")
    public final BindableAttribute<String> avatarURL;
    private WidgetOnlinePeopleViewer viewer;
    private JDiscordRelation relation;
    private long start;
    public WidgetOnline(WidgetOnlinePeopleViewer parent, JDiscordRelation relation) {
        super(new ResourceLocation("dungeonsguide:gui/features/discordOnline/discordOnline.gui"));
        this.viewer = parent;
        this.relation = relation;
        start = System.currentTimeMillis();
        username = new BindableAttribute<>(String.class, relation.getDiscordUser().getName());
        discriminator = new BindableAttribute<>(String.class, relation.getDiscordUser().getDiscriminator());
        avatarURL = new BindableAttribute<>(String.class, relation.getDiscordUser().getEffectiveAvatarUrl());
    }

    public JDiscordRelation getRelation() {
        return relation;
    }

    @Override
    public long startedDisplaying() {
        return start;
    }

    @Override
    public long ttl() {
        return 2000;
    }
}
