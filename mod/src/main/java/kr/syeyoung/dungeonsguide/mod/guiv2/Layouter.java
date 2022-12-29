package kr.syeyoung.dungeonsguide.mod.guiv2;

import lombok.Getter;

import java.awt.*;

public abstract class Layouter {
    @Getter

    private DomElement domElement;
    public Layouter(DomElement element) {
        this.domElement = element;
    }

    public abstract Dimension getPreferredSize(ConstraintBox constraints);


    public abstract void relayout();
}
