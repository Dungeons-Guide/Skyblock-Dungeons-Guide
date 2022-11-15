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

package kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/gamesdk/jna/datastruct/DiscordUser.java
import kr.syeyoung.dungeonsguide.gamesdk.jna.typedef.DiscordSnowflake;
========
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.typedef.DiscordSnowflake;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/discord/gamesdk/jna/datastruct/DiscordUser.java

import java.util.Arrays;
import java.util.List;

public class DiscordUser extends DiscordStruct {
    public DiscordSnowflake id = new DiscordSnowflake();
    public byte[] username = new byte[256];
    public byte[] discriminator = new byte[8];
    public byte[] avatar = new byte[128];
    public boolean bot;
    public DiscordUser() {super();} public DiscordUser(Pointer pointer) {super(pointer);}

    public static class ByReference extends DiscordUser implements Structure.ByReference { public ByReference() {super();} public ByReference(Pointer pointer) {super(pointer);}}
    public static class ByValue extends DiscordUser implements Structure.ByValue { public ByValue() {super();} public ByValue(Pointer pointer) {super(pointer);}}

    @Override
    protected List getFieldOrder() {
        return Arrays.asList("id", "username", "discriminator", "avatar", "bot");
    }
}
