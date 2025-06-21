package kr.syeyoung.modapi.data;

import lombok.Getter;

public class VectorI3D {
    @Getter
    public int x,y,z;

    public VectorI3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public VectorI3D add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public VectorI3D subtract(int x, int y, int z) {
        return add(-x, -y, -z);
    }

    public Vector3D toVector3D() {
        return new Vector3D(x,y,z);
    }
    public VectorI3D clone() {
        return new VectorI3D(x,y,z);
    }

}
