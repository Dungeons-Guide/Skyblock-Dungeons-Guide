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

package kr.syeyoung.dungeonsguide.features.impl.secret;

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;

public class FeaturePathfindToAll extends SimpleFeature {
    public FeaturePathfindToAll(){
        super("Dungeon.Secrets.Pathfind To All", "Start pathfind to all secrets upon entering a room", "Auto browse to all secrets in the room", "secret.secretpathfind.allbrowse", false);
        addParameter("bat", new FeatureParameter<Boolean>("bat", "Trigger pathfind to Bat", "This feature will trigger pathfind to all bats in this room when entering a room", true, "boolean"));
        addParameter("chest", new FeatureParameter<Boolean>("chest", "Trigger pathfind to Chest", "This feature will trigger pathfind to all chests in this room when entering a room", true, "boolean"));
        addParameter("essence", new FeatureParameter<Boolean>("essence", "Trigger pathfind to Essence", "This feature will trigger pathfind to all essences in this room when entering a room", true, "boolean"));
        addParameter("itemdrop", new FeatureParameter<Boolean>("itemdrop", "Trigger pathfind to Itemdrop", "This feature will trigger pathfind to all itemdrops in this room when entering a room", true, "boolean"));
    }

    public boolean isBat() {
        return this.<Boolean>getParameter("bat").getValue();
    }
    public boolean isChest() {
        return this.<Boolean>getParameter("chest").getValue();
    }
    public boolean isEssence() {
        return this.<Boolean>getParameter("essence").getValue();
    }
    public boolean isItemdrop() {
        return this.<Boolean>getParameter("itemdrop").getValue();
    }
}
