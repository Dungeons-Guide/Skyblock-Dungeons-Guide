package kr.syeyoung.modapi.data;

import java.util.HashMap;
import java.util.Map;

public enum EnumFacing {
    DOWN(0, -1, new VectorI3D(0, -1, 0), "down"),
    UP(1, -1, new VectorI3D(0, 1, 0), "up"),
    NORTH(2, 2, new VectorI3D(0, 0, -1), "north"),
    SOUTH(3, 0, new VectorI3D(0, 0, 1), "south"),
    WEST(4, 1, new VectorI3D(-1, 0, 0), "west"),
    EAST(5, 3, new VectorI3D(1, 0, 0), "east");


    private final int index;
    private final int horizontalIndex;
    private final VectorI3D directionVec;
    public static final EnumFacing[] VALUES = new EnumFacing[6];
    public static final EnumFacing[] HORIZONTALS = new EnumFacing[4];
    private final String name;
    private static final Map<String, EnumFacing> NAME_LOOKUP = new HashMap<>();

    private EnumFacing(int indexIn, int horizontalIndexIn, VectorI3D directionVecIn, String name) {
        this.index = indexIn;
        this.horizontalIndex = horizontalIndexIn;
        this.directionVec = directionVecIn;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return this.index;
    }

    public int getHorizontalIndex() {
        return this.horizontalIndex;
    }

    public int getFrontOffsetX() {
        return directionVec.x;
    }

    public int getFrontOffsetY() {
        return directionVec.y;
    }

    public int getFrontOffsetZ() {
        return directionVec.z;
    }

    public VectorI3D getDirectionVec() {
        return this.directionVec;
    }

    public static EnumFacing byName(String name) {
        return name == null ? null : (EnumFacing)NAME_LOOKUP.get(name.toLowerCase());
    }


    public EnumFacing rotateY() {
        switch (this) {
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }


    static {
        for(EnumFacing enumfacing : values()) {
            VALUES[enumfacing.index] = enumfacing;
            if (enumfacing.getHorizontalIndex() != -1) {
                HORIZONTALS[enumfacing.horizontalIndex] = enumfacing;
            }
            NAME_LOOKUP.put(enumfacing.getName().toLowerCase(), enumfacing);
        }
    }
}
