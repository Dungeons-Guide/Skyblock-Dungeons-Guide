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
import net.arikia.dev.drpc.callbacks.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Nicolas "Vatuu" Adamoglou
 * @version 1.5.1
 * <p>
 * Object containing references to all event handlers registered. No callbacks are necessary,
 * every event handler is optional. Non-assigned handlers are being ignored.
 */
public class DiscordEventHandlers extends Structure {

	/**
	 * Callback called when Discord-RPC was initialized successfully.
	 */
	public ReadyCallback ready;
	/**
	 * Callback called when the Discord connection was disconnected.
	 */
	public DisconnectedCallback disconnected;
	/**
	 * Callback called when a Discord error occurred.
	 */
	public ErroredCallback errored;
	/**
	 * Callback called when the player joins the game.
	 */
	public JoinGameCallback joinGame;
	/**
	 * Callback called when the player spectates a game.
	 */
	public SpectateGameCallback spectateGame;
	/**
	 * Callback called when a join request is received.
	 */
	public JoinRequestCallback joinRequest;

	@Override
	public List<String> getFieldOrder() {
		return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
	}

	public static class Builder {

		DiscordEventHandlers h;

		public Builder() {
			h = new DiscordEventHandlers();
		}

		public Builder setReadyEventHandler(ReadyCallback r) {
			h.ready = r;
			return this;
		}

		public Builder setDisconnectedEventHandler(DisconnectedCallback d) {
			h.disconnected = d;
			return this;
		}

		public Builder setErroredEventHandler(ErroredCallback e) {
			h.errored = e;
			return this;
		}

		public Builder setJoinGameEventHandler(JoinGameCallback j) {
			h.joinGame = j;
			return this;
		}

		public Builder setSpectateGameEventHandler(SpectateGameCallback s) {
			h.spectateGame = s;
			return this;
		}

		public Builder setJoinRequestEventHandler(JoinRequestCallback j) {
			h.joinRequest = j;
			return this;
		}

		public DiscordEventHandlers build() {
			return h;
		}
	}
}
