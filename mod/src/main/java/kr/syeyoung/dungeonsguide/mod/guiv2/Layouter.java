package kr.syeyoung.dungeonsguide.mod.guiv2;

import lombok.Getter;

import java.awt.*;

public abstract class Layouter {
    @Getter

    private DomElement domElement;
    public Layouter(DomElement element) {
        this.domElement = element;
    }


    public abstract Dimension layout(ConstraintBox constraintBox);


    public static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }
}
