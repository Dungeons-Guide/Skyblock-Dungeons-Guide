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

package net.arikia.dev.drpc.callbacks;

import com.sun.jna.Callback;
import net.arikia.dev.drpc.DiscordUser;

/**
 * @author Nicolas "Vatuu" Adamoglou
 * @version 1.5.1
 * <p>
 * Interface to be implemented in classes that will be registered as "JoinRequestCallback" Event Handler.
 * @see net.arikia.dev.drpc.DiscordEventHandlers
 **/
public interface JoinRequestCallback extends Callback {

	/**
	 * Method called when another player requests to join a game.
	 *
	 * @param request Object containing all required information about the user requesting to join.
	 * @see DiscordUser
	 */
	void apply(DiscordUser request);
}
