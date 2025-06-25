package kr.syeyoung.modapi.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Iterator;

@EqualsAndHashCode
public class VectorI3D {
    @Getter
    public int x,y,z;

    public VectorI3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public VectorI3D(double x, double y, double z) {
        this((int)x,(int)y,(int)z);
    }

    public VectorI3D(Vector3D vector3D) {
        this.x = (int) vector3D.x;
        this.y = (int) vector3D.y;
        this.z = (int) vector3D.z;
    }

    public VectorI3D add(int x, int y, int z) {
        VectorI3D clone = clone();
        clone.x += x;
        clone.y += y;
        clone.z += z;
        return clone;
    }

    public VectorI3D add(VectorI3D vectorI3D) {
        return this.add(vectorI3D.x, vectorI3D.y, vectorI3D.z);
    }

    public VectorI3D subtract(int x, int y, int z) {
        return add(-x, -y, -z);
    }
    public VectorI3D subtract(VectorI3D vectorI3D) {
        return subtract(vectorI3D.x, vectorI3D.y, vectorI3D.z);
    }

    public Vector3D toVector3D() {
        return new Vector3D(x,y,z);
    }
    public VectorI3D clone() {
        return new VectorI3D(x,y,z);
    }

    public double distanceSq(double x, double y, double z) {
        double dx = this.x-x, dy = this.y-y, dz = this.z-z;
        return dx*dx+dy*dy+dz*dz;
    }
    public double distanceSq(VectorI3D vectorI3D) {
        return distanceSq(vectorI3D.x, vectorI3D.y, vectorI3D.z);
    }
    public double distanceSq(Vector3D vector3D) {
        return distanceSq(vector3D.x, vector3D.y, vector3D.z);
    }

    @Override
    public String toString() {
        return "Vec3{x="+x+",y="+y+",z="+z+"}";
    }

    public static Iterable<VectorI3D> getAllInBox(VectorI3D from, VectorI3D to) {
        final VectorI3D actualFrom = new VectorI3D(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final VectorI3D actualTo = new VectorI3D(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        return new Iterable<VectorI3D>() {
            public Iterator<VectorI3D> iterator() {
                return new Iterator<VectorI3D>() {
                    private VectorI3D lastReturned = null;
                    private boolean next = true;
                    @Override
                    public boolean hasNext() {
                        return next;
                    }

                    @Override
                    public VectorI3D next() {
                        if (this.lastReturned == null) {
                            this.lastReturned = actualFrom;
                            return this.lastReturned;
                        } else if (this.lastReturned.equals(actualTo)) {
                            return null;
                        } else {
                            int i = this.lastReturned.getX();
                            int j = this.lastReturned.getY();
                            int k = this.lastReturned.getZ();
                            if (i < actualTo.getX()) {
                                ++i;
                            } else if (j < actualTo.getY()) {
                                i = actualFrom.getX();
                                ++j;
                            } else if (k < actualTo.getZ()) {
                                i = actualFrom.getX();
                                j = actualFrom.getY();
                                ++k;
                            }

                            this.lastReturned = new VectorI3D(i, j, k);

                            if (this.lastReturned.equals(actualTo))
                                next = false;
                            return this.lastReturned;
                        }
                    }
                };
            }
        };
    }
}
