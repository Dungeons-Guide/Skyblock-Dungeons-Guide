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

package kr.syeyoung.dungeonsguide.stomp;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

@Data
@Accessors(chain = true, fluent = true)
public class StompPayload {
    private StompHeader method;
    private Map<String, String> headers = new HashMap<>();
    private String payload;

    public StompPayload header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public String getBuilt() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.name());
        sb.append("\n");
        for (Map.Entry<String, String> stringStringEntry : headers.entrySet()) {
            sb.append(stringStringEntry.getKey());
            sb.append(":");
            sb.append(stringStringEntry.getValue());
            sb.append("\n");
            if (stringStringEntry.getKey().contains(":")) throw new IllegalStateException("Illegal Character : inside headers");
            if (stringStringEntry.getValue().contains(":")) throw new IllegalStateException("Illegal Character : inside headers");
        }
        sb.append("\n");
        if (payload != null)
            sb.append(payload);
        sb.append((char) 0);
        if (FeatureRegistry.DEBUG.isEnabled()) System.out.println("Sending.. "+ sb);
        return sb.toString();
    }

    public static StompPayload parse(String payload) {
        if (FeatureRegistry.DEBUG.isEnabled()) System.out.println("Receving.. "+payload);

        Scanner scanner = new Scanner(payload);
        StompPayload stompPayload = new StompPayload();
        stompPayload.method = StompHeader.valueOf(scanner.nextLine());
        String line;
        while (!(line = scanner.nextLine()).isEmpty()) {
            int index = line.indexOf(":");
            if (index == -1) throw new IllegalArgumentException("No : found in headers section");
            String name = line.substring(0, index);
            String value;
            if (index == line.length() - 1)
                value = "";
            else
                value = line.substring(index+1);
            stompPayload.headers.put(name, value);
        }

        List<String> lines = new ArrayList<>();
        while (scanner.hasNextLine() && !(line = scanner.nextLine()).equals("\0")) {
            lines.add(line);
        }
        stompPayload.payload = String.join("\n", lines);
        return stompPayload;
    }
}
