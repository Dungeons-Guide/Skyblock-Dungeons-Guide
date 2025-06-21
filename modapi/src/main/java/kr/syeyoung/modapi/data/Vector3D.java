package kr.syeyoung.modapi.data;


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

    public Vector3D add(double x, double y, double z) {
        this.x += x; this.y += y; this.z += z;
        return this;
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
        double len = length();
        x /= len;
        y /= len;
        z /= len;
        return this;
    }

    public Vector3D mult(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
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

}
