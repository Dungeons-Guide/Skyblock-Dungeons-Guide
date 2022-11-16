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

package kr.syeyoung.dungeonsguide.launcher.exceptions.http;

import lombok.Getter;
import org.json.JSONObject;

public class ResponseParsingException extends RuntimeException {
    @Getter
    private final String payload;

    public ResponseParsingException(String payload, Exception e) {
        super(e);
        this.payload = payload;
    }

    public ResponseParsingException(String payload, String message, Exception e) {
        super(message, e);
        this.payload = payload;
    }

    public ResponseParsingException(String payload, String message) {
        super(message);
        this.payload = payload;
    }

    @Override
    public String toString() {
        return super.toString()+"\n Problematic Message: "+payload;
    }
}
