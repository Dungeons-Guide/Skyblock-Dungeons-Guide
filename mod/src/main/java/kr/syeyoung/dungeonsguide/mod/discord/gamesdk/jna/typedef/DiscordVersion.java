/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/gamesdk/jna/typedef/DiscordVersion.java
package kr.syeyoung.dungeonsguide.gamesdk.jna.typedef;
========
package kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/discord/gamesdk/jna/typedef/DiscordVersion.java

public class DiscordVersion extends Int32 {
    public DiscordVersion() {
        this(0);
    }
    public DiscordVersion(long value) {
        super(value);
    }
}
