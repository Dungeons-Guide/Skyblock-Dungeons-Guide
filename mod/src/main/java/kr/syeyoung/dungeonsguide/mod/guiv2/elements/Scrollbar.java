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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthrough;
import net.minecraft.util.ResourceLocation;

@Passthrough(exportName = "_track", bindName = "track", type = Widget.class)
@Passthrough(exportName = "_thumb", bindName = "thumb", type = Widget.class)
public class Scrollbar extends AnnotatedWidget {
    // to set location and stuff
    @Bind(variableName = "x")
    public final BindableAttribute<Double> thumbX = new BindableAttribute<>(Double.class, 0.0);
    @Bind(variableName = "y")
    public final BindableAttribute<Double> thumbY = new BindableAttribute<>(Double.class, 0.0);
    @Bind(variableName = "width")
    public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
    @Bind(variableName = "height")
    public final BindableAttribute<Double> height = new BindableAttribute<>(Double.class, Double.POSITIVE_INFINITY);
    @Bind(variableName = "size")
    public final BindableAttribute<Size> size = new BindableAttribute<>(Size.class);

    @Export(attributeName = "thumbValue")
    public final BindableAttribute<Double> thumbValue = new BindableAttribute<>(Double.class, 10.0);


    @Export(attributeName = "minThumbSize")
    public final BindableAttribute<Double> minimumThumbSize = new BindableAttribute<>(Double.class, 10.0);

    @Export(attributeName = "min")
    public final BindableAttribute<Double> min = new BindableAttribute<>(Double.class, 0.0);
    @Export(attributeName = "max")
    public final BindableAttribute<Double> max = new BindableAttribute<>(Double.class, 100.0);
    @Export(attributeName = "current")
    public final BindableAttribute<Double> current = new BindableAttribute<>(Double.class, 0.0);

    @Export(attributeName = "orientation")
    public final BindableAttribute<Line.Orientation> orientation = new BindableAttribute<>(Line.Orientation.class);

    @Override
    protected Renderer createRenderer() {
        return OnlyChildrenRenderer.INSTANCE;
    }

    public Scrollbar() {
        super(new ResourceLocation("dungeonsguide:gui/elements/scrollBar.gui"));

        thumbValue.addOnUpdate(this::updateStuff);
        min.addOnUpdate(this::updateStuff);
        max.addOnUpdate(this::updateStuff);
        current.addOnUpdate(this::updateThumbLocation);
        size.addOnUpdate((a,b) -> this.updateStuff(0,0));
        updateStuff(0, 0);
    }

    private double per1, per2;

    private void updateStuff(double _, double __) {
        if (size.getValue() != null)
            updatePers(size.getValue());
        updateThumbLocation(0, 0);
    }
    private void updatePers(Size size) {
        double min = this.min.getValue();
        double max = this.max.getValue();
        double thumbSize = this.thumbValue.getValue();

        double movingLength = orientation.getValue() == Line.Orientation.VERTICAL ? size.getHeight() : size.getWidth();


        per1 = movingLength / (max - min + thumbSize);
        per2 = (max - min + thumbSize) / movingLength;

        if (per1 * thumbSize > minimumThumbSize.getValue()) {
            if (orientation.getValue() == Line.Orientation.VERTICAL) height.setValue(per1 * thumbSize);
            else width.setValue(per1 * thumbSize);
        } else {
            movingLength -= minimumThumbSize.getValue();
            per1 = movingLength / (max - min);
            per2 = (max - min) / movingLength;

            if (orientation.getValue() == Line.Orientation.VERTICAL) height.setValue(minimumThumbSize.getValue());
            else width.setValue(minimumThumbSize.getValue());
        }
    }

    private void updateThumbLocation(double newCurrent, double newMin) {
        double location = per1 * (current.getValue() - min.getValue());

        if (orientation.getValue() == Line.Orientation.VERTICAL) thumbY.setValue(location);
        else thumbX.setValue(location);
    }


    private double movingDir;
    private double startCurr;
    private boolean moving = false;
    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (childHandled) return false;
        getDomElement().obtainFocus();
        if (orientation.getValue() == Line.Orientation.VERTICAL) movingDir = relMouseY;
        else movingDir = relMouseX;

        startCurr = current.getValue();
        moving = true;
        return true;
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        double old = movingDir;
        double movingDir = 0;
        if (orientation.getValue() == Line.Orientation.VERTICAL) movingDir = relMouseY;
        else movingDir = relMouseX;

        double dThing = movingDir - old;
        double newVal = dThing * per2 + startCurr;
        newVal = Layouter.clamp(newVal, min.getValue(), max.getValue());
        this.current.setValue(newVal);
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        moving = false;
    }

    @Override
    public boolean mouseScrolled(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int scrollAmount, boolean childHandled) {
        if (childHandled) return false; // how tho
        if (moving) return false;

//        double old = this.current.getValue();
//        double neu;
//        this.current.setValue(
//                neu = Layouter.clamp(this.current.getValue() + scrollAmount, min.getValue(), max.getValue())
//        );
//        return old != neu;
        return false;
    }
}
