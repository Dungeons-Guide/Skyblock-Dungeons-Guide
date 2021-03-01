package kr.syeyoung.dungeonsguide.stomp;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Data
@Accessors(chain = true, fluent = true)
public class StompPayload {
    private StompHeader method;
    private Map<String, String> headers = new HashMap<String, String>();
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
        if (FeatureRegistry.DEBUG.isEnabled())
        System.out.println("Probably sending "+sb.toString());
        return sb.toString();
    }

    public static StompPayload parse(String payload) {
        if (FeatureRegistry.DEBUG.isEnabled())
        System.out.println("Parsing "+payload);
        Scanner scanner = new Scanner(payload);
        StompPayload stompPayload = new StompPayload();
        stompPayload.method = StompHeader.valueOf(scanner.nextLine());
        String line = "";
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

        StringBuilder payloadBuilder = new StringBuilder();
        while (scanner.hasNextLine() && !(line = scanner.nextLine()).equals("\0")) {
            payloadBuilder.append(line);
            payloadBuilder.append("\n");
        }
        stompPayload.payload = payloadBuilder.toString();
        return stompPayload;
    }
}
