package kr.syeyoung.modapi.rendering;

public interface URenderContext {
    // get old id.
    void pushMatrix();
    void popMatrix();
    void translate(double x, double y, double z);
    void scale(double x, double y, double z);
    void rotate(float angle, int x, int y, int z);
}
