/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features;

import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;

public abstract class AbstractGuiFeature extends AbstractFeature {
    protected AbstractGuiFeature(String category, String name, String description, String key) {
        super(category, name, description, key);
        getWidget();
    }

    public abstract OverlayWidget instantiateWidget();

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkVisibility();
    }

    private boolean wasVisible = false;
    private OverlayWidget widget;
    public OverlayWidget getWidget() {
        if (widget == null) widget = instantiateWidget();
        return widget;
    }
    public void checkVisibility() {
        boolean shouldBeVisible = isVisible();
        if (!wasVisible && shouldBeVisible) {
            OverlayManager.getInstance().addOverlay(getWidget());
        } else if (wasVisible && !shouldBeVisible) {
            OverlayManager.getInstance().removeOverlay(getWidget());
        }
        wasVisible = shouldBeVisible;
    }

    public void updatePosition() {
        if (isVisible())
            OverlayManager.getInstance().updateOverlayPosition(getWidget());
    }
    public boolean isVisible() {
        return isEnabled();
    }
}
