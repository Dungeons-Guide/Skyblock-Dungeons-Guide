/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.auth.token;

import java.security.KeyPair;
import java.time.Instant;

public class NullToken implements AuthToken {
    @Override
    public boolean isUserVerified() {
        return false;
    }

    @Override
    public boolean hasFullCapability() {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public Instant getExpiryInstant() {
        return Instant.MIN;
    }

    @Override
    public String getToken() {
        return null;
    }
}
