package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.*;

import java.awt.*;

public class SizedBox {
    public static class BLayout extends Layouter {
        BController bController;

        public BLayout(DomElement element) {
            super(element);
            this.bController = (BController) element.getController();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {

            int width = (int) Math.min(bController.width.getValue(), constraintBox.getMaxWidth());
            int height = (int) Math.min(bController.height.getValue(), constraintBox.getMaxHeight());

            if (getDomElement().getChildren().isEmpty()) {
                return new Dimension(width, height);
            }

            DomElement child = getDomElement().getChildren().get(0);
            Dimension dim = child.getLayouter().layout(new ConstraintBox(
                    width, width, height, height
            )); // force size heh.
            child.setRelativeBound(new Rectangle(0,0,dim.width,dim.height));
            return dim;
        }
    }

    public static class BController extends Controller {

        @Export(attributeName = "width")
        public BindableAttribute<Double> width = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
        @Export(attributeName = "height")
        public BindableAttribute<Double> height = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);

        public BController(DomElement element) {
            super(element);

            loadDom();
            width.addOnUpdate(a -> element.requestRelayout());
            height.addOnUpdate(a -> element.requestRelayout());
        }
    }
}
