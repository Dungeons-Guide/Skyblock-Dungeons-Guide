package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.WidgetCheckMissing;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.WidgetRequestCalculation;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;

import java.util.List;

public class FeaturePrecalcList extends SimpleFeature {
    public FeaturePrecalcList() {
        super("Pathfinding & Secrets", "Precalculations", "Browse Precalculations", "secret.precalclist");
        setEnabled(true);
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Override
    public void setupConfigureWidget(List<Widget> widgets) {
        super.setupConfigureWidget(widgets);
        widgets.add(new WidgetPrecalcList());
    }
}
