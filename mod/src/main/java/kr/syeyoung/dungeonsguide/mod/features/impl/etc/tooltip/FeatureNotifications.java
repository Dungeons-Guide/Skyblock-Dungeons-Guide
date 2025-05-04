package kr.syeyoung.dungeonsguide.mod.features.impl.etc.tooltip;

import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractGuiFeature;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.overlay.WholeScreenPositioner;

import java.util.List;

public class FeatureNotifications extends AbstractGuiFeature {

    public FeatureNotifications() {
        super("Misc", "Progress Bars", "- View progress bars you minimized", "etc.progress");
        setEnabled(true);
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Override
    public void setupConfigureWidget(List<Widget> widgets) {
        super.setupConfigureWidget(widgets);
    }


    private NotificationManagerRootWidget rootWidget;

    @Override
    public OverlayWidget instantiateWidget() {
        return new OverlayWidget(
                rootWidget = new NotificationManagerRootWidget(),
                OverlayType.OVER_ANY,
                new WholeScreenPositioner(),
                getClass().getSimpleName()
        );
    }

    public NotificationManagerRootWidget getRootWidget() {
        return rootWidget;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onTick(DGTickEvent tickEvent) {
//        if (progressUpdate) {
//            if (progress != null) {
//                progress.update(progresses);
//                progressUpdate = false;
//            }
//        }
    }

}
