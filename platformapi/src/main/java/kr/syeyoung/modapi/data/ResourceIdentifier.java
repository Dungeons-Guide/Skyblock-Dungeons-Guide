package kr.syeyoung.modapi.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class ResourceIdentifier {
    @Getter
    private final String mod, location;

    public ResourceIdentifier(String identifier) {
        if (identifier.contains(":")) {
            this.mod = identifier.split(":")[0];
            this.location = identifier.split(":")[1];
        } else {
            this.mod = "minecraft";
            this.location = identifier.split(":")[0];
        }
    }

    public ResourceIdentifier(String mod, String location) {
        this.mod = mod;
        this.location = location;
    }

    @Override
    public String toString() {
        return mod+":"+location;
    }
}
