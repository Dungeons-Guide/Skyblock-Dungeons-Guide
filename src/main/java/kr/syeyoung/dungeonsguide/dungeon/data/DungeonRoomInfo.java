package kr.syeyoung.dungeonsguide.dungeon.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DungeonRoomInfo {

    private transient boolean registered;

    private short shape;
    private byte color;

    private int[][] blocks;

    private UUID uuid;
    private String name;
}
