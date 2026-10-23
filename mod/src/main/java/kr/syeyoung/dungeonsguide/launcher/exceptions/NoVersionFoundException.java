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

package kr.syeyoung.dungeonsguide.launcher.exceptions;

import lombok.Getter;

@Getter
public class NoVersionFoundException extends Exception {
    private String branch;
    private String version;

    public NoVersionFoundException(String branch, String version, String payload) {
        super("No version found: "+branch+" - "+version+"\n Additional Data: "+payload);
        this.branch = branch;
        this.version = version;
    }
    public NoVersionFoundException(String branch, String version, String payload, Throwable e) {
        super("No version found: "+branch+" - "+version+"\n Additional Data: "+payload+"\n "+e.toString(), e);
        this.branch = branch;
        this.version = version;
    }
}
