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

package kr.syeyoung.dungeonsguide.mod.resources;

import net.minecraft.client.resources.AbstractResourcePack;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class DGTexturePack extends AbstractResourcePack {
    public DGTexturePack() {
        super(null);
    }

    @Override
    protected InputStream getInputStreamByName(String name) {
        return this.getClass().getResourceAsStream("/"+name);
    }

    @Override
    protected boolean hasResourceName(String name) {
        return getInputStreamByName(name) != null;
    }

    @Override
    public Set<String> getResourceDomains() {
        return Collections.singleton("dungeonsguide");
    }

    @Override
    public String getPackName() {
        return "Dungeons Guide Default Pack";
    }
}
