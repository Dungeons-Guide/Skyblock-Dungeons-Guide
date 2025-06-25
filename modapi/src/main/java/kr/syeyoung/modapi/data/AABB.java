package kr.syeyoung.modapi.data;

public class AABB {
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    public AABB(VectorI3D p1, VectorI3D p2) {
        this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }
    public AABB(Vector3D p1, Vector3D p2) {
        this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }


    public AABB expand(double x, double y, double z) {
        double minX = this.minX - x;
        double minY = this.minY - y;
        double minZ = this.minZ - z;
        double maxX = this.maxX + x;
        double maxY = this.maxY + y;
        double maxZ = this.maxZ + z;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AABB addCoord(double x, double y, double z) {
        return new AABB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }


}
