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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget;

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.RawMinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class WidgetDataRendererWrapper extends Widget implements Layouter, Renderer {
    private PlayerProfile profile;
    private final IDataRenderer dataRenderer;
    public WidgetDataRendererWrapper(PlayerProfile profile, IDataRenderer dataRenderer) {
        this.profile = profile;
        this.dataRenderer = dataRenderer;
    }

    public void setProfile(PlayerProfile profile) {
        this.profile = profile;
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        Dimension dim = dataRenderer.getDimension();
        return new Size(
                Layouter.clamp(dim.width, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                Layouter.clamp(dim.height, constraintBox.getMinHeight(), constraintBox.getMaxHeight())
        );
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        dataRenderer.renderData(context, profile);
    }


    private RawMinecraftTooltip actualTooltip = new RawMinecraftTooltip(0, 0);
    private boolean tooltipShown = false;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        List<String> toHover = null;
        if (getDomElement().getAbsBounds().contains(absMouseX, absMouseY))
            toHover = dataRenderer.onHover(profile);

        if (toHover != null)
            actualTooltip.setTooltip(toHover);

        if (toHover == null && tooltipShown) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(actualTooltip, null);
            this.tooltipShown = false;
        } else if (toHover != null && !tooltipShown) {
            tooltipShown = true;
            actualTooltip.setMousePos(absMouseX, absMouseY);
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(actualTooltip, (a) -> {
                        this.tooltipShown = false;
                    });
        }
        return false;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (tooltipShown) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(actualTooltip, null);
            this.tooltipShown = false;
        }
    }


    @Override
    public void onUnmount() {
        if (tooltipShown) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(actualTooltip, null);
            this.tooltipShown = false;
        }
        super.onUnmount();
    }
}
