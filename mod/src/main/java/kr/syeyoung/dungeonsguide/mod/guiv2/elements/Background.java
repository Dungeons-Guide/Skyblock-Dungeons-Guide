/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;


import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;

// passes down constraints
// but sets background!! cool!!!
public class Background {

    public static class BRender extends SingleChildRenderer {

        BController bController;
        public BRender(DomElement domElement) {
            super(domElement);
            this.bController = (BController) domElement.getController();
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, int relMouseX, int relMouseY, float partialTicks) {
            Gui.drawRect(0,0,getDomElement().getRelativeBound().width, getDomElement().getRelativeBound().height,
                    bController.color.getValue()
                    );

            super.doRender(absMouseX, absMouseY, relMouseX, relMouseY, partialTicks);
        }
    }

    public static class BController extends Controller {
        @Export(attributeName = "backgroundColor")
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0xFFFFFFFF);

        public BController(DomElement element) {
            super(element);
            loadAttributes();
            loadDom();
        }
    }

    public static final DomElementRegistry.DomElementCreator CREATOR = new DomElementRegistry.GeneralDomElementCreator(
            SingleChildPassingLayouter::new, BRender::new, BController::new
    );
}
