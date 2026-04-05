package kr.syeyoung.modapi.data;


import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Vector3D {
    public double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D() {
        x = 0; y = 0; z = 0;
    }

    public Vector3D(VectorI3D vectorI3D) {
        this(vectorI3D.getX(), vectorI3D.getY(), vectorI3D.getZ());
    }

    public Vector3D add(double x, double y, double z) {
        Vector3D clone = clone();
        clone.x += x; clone.y += y; clone.z += z;
        return clone;
    }

    public Vector3D add(Vector3D DVector3D) {
        return add(DVector3D.x, DVector3D.y, DVector3D.z);
    }

    public Vector3D subtract(double x, double y, double z) {
        return add(-x, -y, -z);
    }
    public Vector3D subtract(Vector3D DVector3D) {
        return add(-DVector3D.x, -DVector3D.y, -DVector3D.z);
    }

    public double lengthSq() {
        return x*x + y*y + z*z;
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public Vector3D normalize() {
        Vector3D clone = clone();
        double len = length();
        if (len  < 1.0E-4) {
            clone.x = clone.y = clone.z = 0;
            return clone;
        }
        clone.x /= len;
        clone.y /= len;
        clone.z /= len;

        return clone;
    }

    public Vector3D mult(double scalar) {
        Vector3D clone = clone();
        clone.x *= scalar;
        clone.y *= scalar;
        clone.z *= scalar;
        return clone;
    }

    public double dotProduct(Vector3D DVector3D) {
        return x * DVector3D.x + y * DVector3D.y + z * DVector3D.z;
    }

    public double angleCos(Vector3D DVector3D) {
        return dotProduct(DVector3D) / DVector3D.length() / length();
    }

    public Vector3D crossProduct(Vector3D v) {
        return new Vector3D(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x);
    }

    public Vector3D clone() {
        return new Vector3D(x,y,z);
    }

    public double distanceSq(double x, double y, double z) {
        return (this.x-x)*(this.x-x) + (this.y-y)*(this.y-y) + (this.z-z)*(this.z-z);
    }
    public double distanceSq(Vector3D vector3D) {
        return distanceSq(vector3D.x, vector3D.y, vector3D.z);
    }
    public double distanceSq(VectorI3D vectorI3D) {
        return distanceSq(vectorI3D.x, vectorI3D.y, vectorI3D.z);
    }

    @Override
    public String toString() {
        return "Vec3{x="+x+",y="+y+",z="+z+"}";
    }


    public Vector3D getIntermediateWithXValue(Vector3D vec, double x) {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;
        if (d0 * d0 < 1.0000000116860974E-7) {
            return null;
        } else {
            double d3 = (x - this.x) / d0;
            return d3 >= 0.0 && d3 <= 1.0 ? new Vector3D(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }

    public Vector3D getIntermediateWithYValue(Vector3D vec, double y) {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;
        if (d1 * d1 < 1.0000000116860974E-7) {
            return null;
        } else {
            double d3 = (y - this.y) / d1;
            return d3 >= 0.0 && d3 <= 1.0 ? new Vector3D(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }

    public Vector3D getIntermediateWithZValue(Vector3D vec, double z) {
        double d0 = vec.x - this.x;
        double d1 = vec.y - this.y;
        double d2 = vec.z - this.z;
        if (d2 * d2 < 1.0000000116860974E-7) {
            return null;
        } else {
            double d3 = (z - this.z) / d2;
            return d3 >= 0.0 && d3 <= 1.0 ? new Vector3D(this.x + d0 * d3, this.y + d1 * d3, this.z + d2 * d3) : null;
        }
    }
}
