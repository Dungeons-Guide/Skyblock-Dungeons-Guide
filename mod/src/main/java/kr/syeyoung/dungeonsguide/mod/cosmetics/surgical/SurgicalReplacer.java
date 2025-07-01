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

package kr.syeyoung.dungeonsguide.mod.cosmetics.surgical;

import kr.syeyoung.modapi.data.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;

public class SurgicalReplacer {

    public static Pair<Component, Integer> runDFSAndChange(int currIdx, int idx, int len, Component component, Component toInject) {
        if (component instanceof TextComponent) {
            String content = ((TextComponent) component).content();
            int subFromIdx = idx - currIdx;
            int subToIdx = (idx + len) - currIdx;

            // handle case where it is not included.
            // asd(lk|jsd)(dkd)(skd|asd)
            //
            // Case 1. entirely included.

            //           [    )
            //       |  |t    s                 C1
            //       |   |    s                 C1
            //       |   t  | s                 C5
            //       |   t    |                 C2
            //       |   t    s    |            C2
            //           | |  s                 C5
            //           |    |                 C2
            //           |    s     |           C2
            //           t || s                 C3 RESP
            //           t |  |                 C4 RESP
            //           t |  s      |          C4 RESP
            //           t    |     |           C4 RESP
            //           t    s  |      |       C1
            Component from;
            if (subFromIdx > content.length() || subToIdx <= 0) { // C1
                from = component.children(new ArrayList<>());
            } else if (subFromIdx <= 0 && subToIdx >= content.length()) { // C2
                from = ((TextComponent) component).content("").children(new ArrayList<>());
            } else if (0 < subFromIdx && subToIdx < content.length()) { // C3
                from = Component.text("").mergeStyle(component)
                        .append(Component.text(content.substring(0, subFromIdx)))
                        .append(toInject)
                        .append(Component.text(content.substring(subToIdx, content.length())));
            } else if (subFromIdx >= 0 && subToIdx >= content.length()) { // fine-yoinked
                from = ((TextComponent) component).content(content.substring(0, subFromIdx))
                        .children(new ArrayList<>())
                        .append(toInject);
            } else { // yoinked-fine
                from = ((TextComponent) component).content(content.substring(subToIdx, content.length())).children(new ArrayList<>());
            }

            currIdx += content.length();

            for (Component child : component.children()) {
                Pair<Component, Integer> result = runDFSAndChange(currIdx, idx, len, child, toInject);
                from = from.append(result.first);
                currIdx = result.second;
            }

            return new Pair<>(from, currIdx);
        } else {
            Component from;
            if (idx <= currIdx && currIdx < idx+len) {
                // don't include. just run children.
                from = Component.text("").mergeStyle(component);
            } else {
                from = component.children(new ArrayList<>());
            }
            for (Component child : component.children()) {
                Pair<Component, Integer> result = runDFSAndChange(currIdx, idx, len, child, toInject);
                from.append(result.first);
                currIdx = result.second;
            }
            return new Pair<>(from, currIdx);
        }
    }

    public static Component inject(int idx, int len, Component component, Component toInject) {
        if (idx <= 0) return Component.text("").append(toInject).append(runDFSAndChange(0, idx, len, component, toInject).first);
        return runDFSAndChange(0, idx, len, component, toInject).first;
    }
}
