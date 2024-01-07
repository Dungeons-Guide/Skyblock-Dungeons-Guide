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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2;

import kr.syeyoung.dungeonsguide.mod.config.types.GUIPosition;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.AbstractHUDFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// Stack'em
//
public class HUDConfigRootWidget extends Widget implements Layouter, Renderer {

    private List<Widget> widgets = new ArrayList<>();
    private List<HUDWidgetWrapper> widgets2= new ArrayList<>();

    private TreeSet<Position> markersX = new TreeSet<>(Comparator.comparingDouble(Position::getX));
    private TreeSet<Position> markersY = new TreeSet<>(Comparator.comparingDouble(Position::getY));

    private double ySnap = -1;
    private double xSnap = -1;

    private Position getVerticalSnapMarker(Position x) {
        Position pos1 = markersX.floor(x);
        Position pos2 = markersX.ceiling(x);
        return Stream.of(pos1, pos2)
                .filter(Objects::nonNull)
                .filter(a -> Math.abs(a.x - x.x) <= 3)
                .min(Comparator.comparingDouble(a -> Math.abs(a.x - x.x)))
                .orElse(null);
    }

    private Position getHorizontalSnapMarker(Position x) {
        Position pos1 = markersY.floor(x);
        Position pos2 = markersY.ceiling(x);
        return Stream.of(pos1, pos2)
                .filter(Objects::nonNull)
                .filter(a -> Math.abs(a.y - x.y) <= 3)
                .min(Comparator.comparingDouble(a -> Math.abs(a.y - x.y)))
                .orElse(null);
    }

    private void updateMarkers(HUDWidgetWrapper skip) {
        markersX.clear();
        markersY.clear();

        for (HUDWidgetWrapper widgetWrapper : widgets2) {
            if (widgetWrapper == skip) continue;
            if (widgetWrapper.getDemoWidget() instanceof MarkerProvider) {
                List<Position> pos = ((MarkerProvider) widgetWrapper.getDemoWidget()).getMarkers();
                pos.stream()
                        .map(a -> new Position(a.x + widgetWrapper.getDomElement().getRelativeBound().getX(), a.y + widgetWrapper.getDomElement().getRelativeBound().getY()))
                        .forEach(a -> {
                            markersY.add(a);
                            markersX.add(a);
                        });
            }
        }
        markersX.add(new Position(lastWidth/2, 0));
        markersY.add(new Position(0, lastHeight/2));
    }

    private AbstractHUDFeature filter;
    public HUDConfigRootWidget(AbstractHUDFeature filter) {
        this.filter = filter;
        widgets.add(new EventListenerWidget());
        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
            if (!(abstractFeature instanceof AbstractHUDFeature)) continue;
            if (!abstractFeature.isEnabled() && abstractFeature.isDisableable()) continue;
            HUDWidgetWrapper widgetWrapper = new HUDWidgetWrapper((AbstractHUDFeature) abstractFeature, this,
                    filter == null || abstractFeature == filter);
            widgets.add(widgetWrapper);
            widgets2.add(widgetWrapper);
        }
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        for (int i = buildContext.getChildren().size() - 1; i >= 0; i --) {
            DomElement value = buildContext.getChildren().get(i);
            Rect original = value.getRelativeBound();
            if (original == null) return;
            GlStateManager.pushMatrix();
            GlStateManager.translate(original.getX(), original.getY(), 0);

            double absXScale = buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth();
            double absYScale = buildContext.getAbsBounds().getHeight() / buildContext.getSize().getHeight();

            Rect elementABSBound = new Rect(
                    (buildContext.getAbsBounds().getX() + original.getX() * absXScale),
                    (buildContext.getAbsBounds().getY() + original.getY() * absYScale),
                    (original.getWidth() * absXScale),
                    (original.getHeight() * absYScale)
            );
            value.setAbsBounds(elementABSBound);

            value.getRenderer().doRender(
                    partialTicks, context, value);
            GlStateManager.popMatrix();
        }
        if (xSnap != -1) {
            context.drawRect(xSnap, 0, xSnap+1, buildContext.getSize().getHeight(), 0xFF00FF00);
        }
        if (ySnap != -1) {
            context.drawRect(0, ySnap, buildContext.getSize().getWidth(), ySnap+1, 0xFF00FF00);
        }
    }

    public class EventListenerWidget extends Widget implements Layouter{
        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
        }

        private HUDWidgetWrapper target;
        private double sx, sy;
        private Rect started;
        private List<Position> markers;
        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
            if (mouseButton != 0) return false;
            this.sx = relMouseX; this.sy = relMouseY;
            for (HUDWidgetWrapper widgetWrapper : widgets2) {
                if (filter != null && widgetWrapper.getAbstractHUDFeature() != filter) continue;
                if (widgetWrapper.getDomElement().getAbsBounds().contains(absMouseX, absMouseY)) {
                    target = widgetWrapper;
                    started = widgetWrapper.getDomElement().getRelativeBound();
                    if (target.getDemoWidget() instanceof MarkerProvider)
                        markers = ((MarkerProvider) target.getDemoWidget()).getMarkers();
                    getDomElement().obtainFocus();

                    if (!widgetWrapper.getDemoWidget().getDomElement().getAbsBounds().contains(absMouseX, absMouseY)) {
                        target = null;
                        started = null;
                        markers = null;
                        return false;
                    }
                    updateMarkers(target);
                    return true;
                }
            }
            return true;
        }

        @Override
        public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
            return false;
        }

        @Override
        public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (started == null) return;
            double dx = relMouseX - sx;
            double dy = relMouseY - sy;
            Rect newRect = new Rect(Layouter.clamp(dx + started.getX(), 0, lastWidth - started.getWidth() + target.getWidthPlus()),
                    Layouter.clamp(dy + started.getY(), 0, lastHeight - started.getHeight() + target.getHeightPlus()), started.getWidth(), started.getHeight());

            if (markers != null) {
                Rect finalNewRect = newRect;
                List<Position> positionedMarkers = markers.stream()
                        .map(a -> new Position(finalNewRect.getX() + a.getX(), finalNewRect.getY() + a.getY())).collect(Collectors.toList());
                double moveX = 0, moveY = 0;
                xSnap = -1; ySnap = -1;
                for (Position positionedMarker : positionedMarkers) {
                    Position bestSnap = getVerticalSnapMarker(positionedMarker);
                    if (bestSnap == null) continue;
                    moveX = bestSnap.getX()-positionedMarker.getX();
                    xSnap = bestSnap.getX();
                    break;
                }
                for (Position positionedMarker : positionedMarkers) {
                    Position bestSnap = getHorizontalSnapMarker(positionedMarker);
                    if (bestSnap == null) continue;
                    moveY = bestSnap.getY()-positionedMarker.getY();
                    ySnap = bestSnap.getY();
                    break;
                }

                newRect = new Rect(moveX + newRect.getX(), moveY + newRect.getY(), newRect.getWidth(), newRect.getHeight());
            }
            newRect = new Rect(Layouter.clamp(newRect.getX(), 0, lastWidth - started.getWidth() + target.getWidthPlus()),
                    Layouter.clamp(newRect.getY(), 0, lastHeight - started.getHeight() + target.getHeightPlus()),
                    newRect.getWidth(), newRect.getHeight());


            target.getDomElement().setRelativeBound(newRect);
        }

        @Override
        public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
            if (started == null) return;
            double dx = relMouseX - sx;
            double dy = relMouseY - sy;
            Rect newRect = new Rect(Layouter.clamp(dx + started.getX(), 0, lastWidth - started.getWidth() + target.getWidthPlus()),
                    Layouter.clamp(dy + started.getY(), 0, lastHeight - started.getHeight() + target.getHeightPlus()),
                    target.getDemoWidget().getDomElement().getSize().getWidth(), target.getDemoWidget().getDomElement().getSize().getHeight());

            if (markers != null) {
                Rect finalNewRect = newRect;
                List<Position> positionedMarkers = markers.stream()
                        .map(a -> new Position(finalNewRect.getX() + a.getX(), finalNewRect.getY() + a.getY())).collect(Collectors.toList());
                double moveX = 0, moveY = 0;
                for (Position positionedMarker : positionedMarkers) {
                    Position bestSnap = getVerticalSnapMarker(positionedMarker);
                    if (bestSnap == null) continue;
                    moveX = bestSnap.getX()-positionedMarker.getX();
                    break;
                }
                for (Position positionedMarker : positionedMarkers) {
                    Position bestSnap = getHorizontalSnapMarker(positionedMarker);
                    if (bestSnap == null) continue;
                    moveY = bestSnap.getY()-positionedMarker.getY();
                    break;
                }

                newRect = new Rect(moveX + newRect.getX(), moveY + newRect.getY(), newRect.getWidth(), newRect.getHeight());
            }

            newRect = new Rect(Layouter.clamp(newRect.getX(), 0, lastWidth - started.getWidth() + target.getWidthPlus()),
                    Layouter.clamp(newRect.getY(), 0, lastHeight - started.getHeight() + target.getHeightPlus()),
                    newRect.getWidth(), newRect.getHeight());
            GUIPosition newPos = GUIPosition.of(newRect, lastWidth, lastHeight);
            newPos.setWidth(target.getAbstractHUDFeature().getFeatureRect().getWidth());
            newPos.setHeight(target.getAbstractHUDFeature().getFeatureRect().getHeight());
            target.getAbstractHUDFeature().setFeatureRect(newPos);
            updatePosition(target);
            target = null;
            markers = null;
            started = null;
            xSnap = -1;
            ySnap = -1;
        }

        @Override
        public boolean mouseScrolled(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int scrollAmount) {
            return true;
        }
    }


    @Override
    public List<Widget> build(DomElement buildContext) {
        return widgets;
    }


    @Getter
    private double lastWidth, lastHeight;

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        lastWidth = constraintBox.getMaxWidth();
        lastHeight = constraintBox.getMaxHeight();
        for (DomElement child : buildContext.getChildren()) {
            if (!(child.getWidget() instanceof HUDWidgetWrapper)) {
                Size size = child.getLayouter().layout(child, constraintBox);
                child.setRelativeBound(new Rect(0,0,size.getWidth(),size.getHeight()));
                continue;
            }
            updatePosition((HUDWidgetWrapper) child.getWidget());
        }

        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    private void updatePosition(HUDWidgetWrapper widget) {
        Size size1 = widget.getDomElement().getLayouter().layout(widget.getDomElement(), new ConstraintBox(0, lastWidth,0,lastHeight));
        Rect pos =  widget.getPositioner().position(
                widget.getDemoWidget().getDomElement(), lastWidth, lastHeight
        );
        Rect newPos = new Rect(pos.getX(), pos.getY(), size1.getWidth(),size1.getHeight());

        widget.getDomElement().setRelativeBound(newPos);
    }
}
