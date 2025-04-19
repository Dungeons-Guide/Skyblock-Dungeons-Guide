/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.triggers;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCInteger;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.RoomRouteHandler;
import kr.syeyoung.dungeonsguide.mod.utils.VectorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.LinkedHashMap;

public class FeatureCreateRefreshLine extends SimpleFeature {
    public FeatureCreateRefreshLine() {
        super("Pathfinding & Secrets", "Refresh pathfind line or Trigger pathfind", "A keybind for creating or refresh pathfind lines for pathfind contexts that doesn't have line, or contexts that has refresh rate set to -1.\nPress settings to edit the key", "secret.refreshPathfind", true);
        this.parameters = new LinkedHashMap<>();
        addParameter("key", new FeatureParameter<Integer>("key", "Key","Press to refresh or create pathfind line", Keyboard.KEY_NONE, TCKeybind.INSTANCE));
        addParameter("pathfind", new FeatureParameter<Boolean>("pathfind", "Enable Pathfinding", "Force Enable pathfind for future actions when used", false, TCBoolean.INSTANCE));
        addParameter("refreshrate", new FeatureParameter<Integer>("refreshrate", "Line Refreshrate", "Ticks to wait per line refresh, to be overriden. If the line already has pathfind enabled, this value does nothing. Specify it to -1 to don't refresh line at all", 10, TCInteger.INSTANCE));
    }
    public int getKeybind() {return this.<Integer>getParameter("key").getValue();}
    public boolean isPathfind() {
        return this.<Boolean>getParameter("pathfind").getValue();
    }
    public int getRefreshRate() {
        return this.<Integer>getParameter("refreshrate").getValue();
    }


    private IPathDisplayEngine<?> getBestFit(RoomRouteHandler roomRouteHandler, float partialTicks) {
        IPathDisplayEngine smallest = null;
        double smallestTan = 0.002;
        DungeonRoom dungeonRoom = roomRouteHandler.getDungeonRoom();
        for (IPathDisplayEngine value2 : roomRouteHandler.getPath().values()) {
            ActionRoute value = value2.getActionRoute();

            BlockPos target;
            AbstractAction currentAction = value.getCurrentAction();
            if (currentAction instanceof AtomicAction) {
                AbstractAction consider = ((AtomicAction) currentAction).getCurrentAction();
                if (!(consider instanceof AbstractActionMove) && ((AtomicAction) currentAction).getCurrent() >= 1)
                    consider = ((AtomicAction) currentAction).getActions().get(((AtomicAction) currentAction).getCurrent()-1);
                currentAction = consider;
            }
            AbstractAction theActionBefore = value.getCurrent() >= 1 ? value.getActions().get(value.getCurrent()-1) : null;
            if (theActionBefore instanceof AtomicAction) theActionBefore = ((AtomicAction) theActionBefore).getCurrentAction();

            if (currentAction instanceof AbstractActionMove) target = ((AbstractActionMove) currentAction).getBeaconTargetPos(dungeonRoom);
            else if (theActionBefore instanceof AbstractActionMove) target = ((AbstractActionMove) theActionBefore).getBeaconTargetPos(dungeonRoom);
            else continue;


            if (((ActionRouteProperties) value2.getSettings()).getLineRefreshRate() != -1 &&
                    ((ActionRouteProperties) value2.getSettings()).isPathfind() && !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled()) continue;

            Entity e = Minecraft.getMinecraft().getRenderViewEntity();

            double vectorV = VectorUtils.distSquared(e.getLook(partialTicks), e.getPositionEyes(partialTicks), new Vec3(target).addVector(0.5,0.5,0.5));

            if (vectorV < smallestTan) {
                smallest = value2;
                smallestTan = vectorV;
            }
        }
        return smallest;
    }


    @DGEventHandler
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (!SkyblockStatus.isOnDungeon() || context == null) return;
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) return;
        if (context.getScaffoldParser() == null) return;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPositionVector());
        DungeonRoom currentRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (currentRoom == null) return;
        if (!currentRoom.getRoomBounds().isFullyWithin(Minecraft.getMinecraft().thePlayer.getPositionVector())) return;
        RoomRouteHandler handler = FeatureRegistry.SECRET_ROUTE_REGISTRY.getRoomHandler(currentRoom);

        if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.getKeybind() == keyInputEvent.getKey() && FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isEnabled()) {
            IPathDisplayEngine engine = getBestFit(handler, 0);
            if (engine == null) return;
            ActionRoute actionRoute = engine.getActionRoute();
            // Because no route found!
            if (actionRoute == null) return;
            // actually do force refresh because of force freeze pathfind
            if (actionRoute.getCurrentAction() instanceof ActionMove) {
                ActionMove ac = (ActionMove) actionRoute.getCurrentAction();
                ac.forceRefresh(currentRoom);
            } else if (actionRoute.getCurrentAction() instanceof ActionMoveNearestAir) {
                ActionMoveNearestAir ac = (ActionMoveNearestAir) actionRoute.getCurrentAction();
                ac.forceRefresh(currentRoom);
            } else if (actionRoute.getCurrent() >= 1 && actionRoute.getActions().get(actionRoute.getCurrent() - 1) instanceof ActionMove) {
//                engine.forceRefresh();
                ((ActionMove) actionRoute.getActions().get(actionRoute.getCurrent() - 1)).forceRefresh(currentRoom);
            } else if (actionRoute.getCurrent() >= 1 && actionRoute.getActions().get(actionRoute.getCurrent() - 1) instanceof ActionMoveNearestAir) {
                ((ActionMoveNearestAir) actionRoute.getActions().get(actionRoute.getCurrent() - 1)).forceRefresh(currentRoom);
            }

            if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isPathfind() && !((ActionRouteProperties) engine.getSettings()).isPathfind()) {
                ((ActionRouteProperties) engine.getSettings()).setPathfind(true);
                ((ActionRouteProperties) engine.getSettings()).setLineRefreshRate(FeatureRegistry.SECRET_CREATE_REFRESH_LINE.getRefreshRate());
            }
        }
    }

}
