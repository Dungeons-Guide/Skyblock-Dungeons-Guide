/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import java.util.Iterator;

public class ElementTreeWalkIterator implements Iterator<DomElement> {
    private DomElement current;
    public ElementTreeWalkIterator(DomElement child) {
        this.current = child;
    }

    @Override
    public boolean hasNext() {
        return current != null;
    }

    @Override
    public DomElement next() {
        DomElement c1 = current;
        current = current.getParent();
        return c1;
    }
}
