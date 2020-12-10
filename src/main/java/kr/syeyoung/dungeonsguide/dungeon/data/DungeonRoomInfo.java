package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class DungeonRoomInfo implements Serializable {

    public DungeonRoomInfo(short shape, byte color) {
        this.uuid = UUID.randomUUID();
        this.name = this.uuid.toString();
        this.shape = shape;
        this.color = color;
    }

    private transient boolean registered;

    private short shape;
    private byte color;

    private int[][] blocks;

    private UUID uuid;
    private String name;

    private String processorId = "default";

    private Map<String, Object> properties = new HashMap<String, Object>();

    private Map<String, DungeonMechanic> mechanics = new HashMap<String, DungeonMechanic>();
}
