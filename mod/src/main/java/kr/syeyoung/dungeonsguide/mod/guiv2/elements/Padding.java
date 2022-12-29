package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.*;

import java.awt.*;

public class Padding {
    public static class PLayouter extends Layouter {
        PController controller;
        public PLayouter(DomElement element) {
            super(element);
            this.controller = (PController) element.getController();
        }

        @Override
        public Dimension layout(ConstraintBox constraintBox) {
            DomElement domElement = getDomElement().getChildren().get(0);

            int width = (int) (controller.left.getValue() + controller.right.getValue());
            int height = (int) (controller.top.getValue() + controller.bottom.getValue());
            Dimension dim = domElement.getLayouter().layout(new ConstraintBox(
                    constraintBox.getMinWidth() - width,
                    constraintBox.getMaxWidth() - width,
                    constraintBox.getMinHeight() - height,
                    constraintBox.getMaxHeight() - height
            ));

            domElement.setRelativeBound(new Rectangle(
                    controller.left.getValue().intValue(),
                    controller.top.getValue().intValue(),
                    dim.width,
                    dim.height
            ));


            return new Dimension(dim.width + width, dim.height + height);
        }
    }

    public static class PController extends Controller {
        @Export(attributeName = "left")
        public BindableAttribute<Double> left = new BindableAttribute<>(Double.class, 0.0);
        @Export(attributeName = "right")
        public BindableAttribute<Double> right = new BindableAttribute<>(Double.class, 0.0);
        @Export(attributeName = "top")
        public BindableAttribute<Double> top = new BindableAttribute<>(Double.class, 0.0);
        @Export(attributeName = "bottom")
        public BindableAttribute<Double> bottom = new BindableAttribute<>(Double.class, 0.0);

        public PController(DomElement element) {
            super(element);

            loadDom();

            left.addOnUpdate(a -> element.requestRelayout());
            right.addOnUpdate(a -> element.requestRelayout());
            top.addOnUpdate(a -> element.requestRelayout());
            bottom.addOnUpdate(a -> element.requestRelayout());
        }
    }
}
