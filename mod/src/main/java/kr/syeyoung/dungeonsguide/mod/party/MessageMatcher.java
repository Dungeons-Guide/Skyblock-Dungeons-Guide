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

package kr.syeyoung.dungeonsguide.mod.party;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageMatcher {
    List<String> simpleEquals = new ArrayList<>();
    List<PatternData> regexPatterns = new ArrayList<>();

    @AllArgsConstructor
    public static class PatternData {
        Pattern pattern;
        int flags;
    }

    public MessageMatcher(List<String> patterns) {
        for (String pattern : patterns) {
            if (pattern.startsWith("=")) simpleEquals.add(pattern.substring(1));
            else regexPatterns.add(new PatternData(Pattern.compile(pattern.substring(1), Pattern.DOTALL | Pattern.MULTILINE),
                    (pattern.contains("<p0>") ? 1 : 0) |
                            (pattern.contains("<p1>") ? 2 : 0) |
                            (pattern.contains("<p2>") ? 4 : 0)
            ));
        }
    }

    public boolean match(String str, Map<String, String> matchGroups) {
        if (matchGroups != null)
            matchGroups.clear();
        for (String simpleEqual : simpleEquals) {
            if (simpleEqual.equals(str)) return true;
        }

        for (PatternData regexPattern : regexPatterns) {
            Matcher m = regexPattern.pattern.matcher(str);
            if (m.matches()) {
                if (matchGroups != null) {
                    if ((regexPattern.flags & 4) > 0)
                        matchGroups.put("2", m.group("p2"));
                    if ((regexPattern.flags & 2) > 0)
                        matchGroups.put("1", m.group("p1"));
                    if ((regexPattern.flags & 1) > 0)
                        matchGroups.put("0", m.group("p0"));
                }
                return true;
            }
        }
        return false;
    }
}
