/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.arikia.dev.drpc;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @author Nicolas "Vatuu" Adamoglou
 * @version 1.5.1
 * <p>
 * Object containing information about a Discord user.
 */
public class DiscordUser extends Structure {

	/**
	 * The userId of the player asking to join.
	 */
	public String userId;
	/**
	 * The username of the player asking to join.
	 */
	public String username;
	/**
	 * The discriminator of the player asking to join.
	 */
	public String discriminator;
	/**
	 * The avatar hash of the player asking to join.
	 *
	 * @see <a href="https://discordapp.com/developers/docs/reference#image-formatting">Image Formatting</a>
	 */
	public String avatar;

	@Override
	public List<String> getFieldOrder() {
		return Arrays.asList("userId", "username", "discriminator", "avatar");
	}
}