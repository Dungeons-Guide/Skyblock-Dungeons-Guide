/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteTooltip;

import kr.syeyoung.dungeonsguide.mod.discord.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class WidgetDiscordFriend extends AnnotatedImportOnlyWidget {
    private JDiscordRelation relation;
    @Bind(variableName = "avatarUrl")
    public final BindableAttribute<String> avatarUrl = new BindableAttribute<String>(String.class);
    @Bind(variableName = "username")
    public final BindableAttribute<String> username = new BindableAttribute<String>(String.class);


    @Bind(variableName = "invited")
    public final BindableAttribute<String> invited = new BindableAttribute<String>(String.class);

    private Consumer<String> invite;

    public WidgetDiscordFriend(JDiscordRelation relation, boolean invited, Consumer<String> invite) {
        super(new ResourceLocation("dungeonsguide:gui/features/discordInvite/friend.gui"));
        this.avatarUrl.setValue(relation.getDiscordUser().getEffectiveAvatarUrl());
        if (relation.getDiscordUser().getDiscriminator().equalsIgnoreCase("0")) {
            this.username.setValue(relation.getDiscordUser().getName());
        } else {
            this.username.setValue(relation.getDiscordUser().getName()+"#"+relation.getDiscordUser().getDiscriminator());
        }
        this.invited.setValue(invited ? "true" : "false");
        this.invite = invite;
        this.relation =relation;
    }

    @On(functionName = "invite")
    public void invite() {
        this.invite.accept(relation.getDiscordUser().getId());
        this.invited.setValue("true");
    }
}
