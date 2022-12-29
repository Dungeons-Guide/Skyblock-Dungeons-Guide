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

import kr.syeyoung.dungeonsguide.launcher.auth.AuthUtil;
import kr.syeyoung.dungeonsguide.launcher.auth.DgAuthUtil;
import org.json.JSONObject;

import java.time.Instant;

public class DGAuthToken implements AuthToken {
    private String token;
    private JSONObject parsed;

    public DGAuthToken(String token) {
        this.token = token;
        this.parsed = DgAuthUtil.getJwtPayload(token);
    }

    @Override
    public boolean isUserVerified() {
        return true;
    }

    @Override
    public boolean hasFullCapability() {
        return true;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public Instant getExpiryInstant() {
        return Instant.ofEpochSecond(Long.parseLong(parsed.getString("exp")));
    }

    @Override
    public String getUID() {
        return parsed.getString("userid");
    }

    @Override
    public String getUUID() {
        return parsed.getString("uuid");
    }

    @Override
    public String getUsername() {
        return parsed.getString("nickname");
    }

    @Override
    public String getToken() {
        return token;
    }
}
