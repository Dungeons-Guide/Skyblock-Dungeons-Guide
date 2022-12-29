package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.Controller;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Layouter;

import java.awt.*;

public class Stack {
    public static class SLayouter extends Layouter {

        public SLayouter(DomElement element) {
            super(element);
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            Dimension max = new Dimension();
            for (DomElement child : getDomElement().getChildren()) {
                Dimension dim = child.getLayouter().layout(constraintBox);
                if (max.width < dim.width) max.width = dim.width;
                if (max.height < dim.height) max.height = dim.height;
                child.setRelativeBound(new Rectangle(0,0,dim.width, dim.height));
            }
            return null;
        }
    }

    public static class SController extends Controller {

        public SController(DomElement element) {
            super(element);
            loadDom();
        }
    }
}
