package kr.syeyoung.dungeonsguide.launcher.gui.tooltip;

import lombok.Builder;
import lombok.Data;

import java.awt.*;

@Data @Builder
public class Notification {
    private String title;
    private int titleColor = 0xFF00FF00;
    private String description;

    private Runnable onClick;
    private boolean unremovable;


    private Rectangle boundRect;
}
