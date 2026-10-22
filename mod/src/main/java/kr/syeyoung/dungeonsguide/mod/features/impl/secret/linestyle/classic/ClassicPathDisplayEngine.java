package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.classic;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.LineRenderUtils;
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.render.world.DGWorldRenderer;
import kr.syeyoung.dungeonsguide.mod.render.world.state.BlockModelState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.LineStripState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClassicPathDisplayEngine implements IPathDisplayEngine<ClassicPathEngineLineProperties> {
    private final Map<AbstractActionMove, ActionMoveContext> executorWeakHashMap = new WeakHashMap<>();
    private final ActionRoute actionRoute;
    private final DungeonRoom dungeonRoom;
    private ClassicPathEngineLineProperties classicPathEngineLineProperties;

    private static class ActionMoveContext {
        private boolean mark = false;
        private PathfinderExecutor executor;
        private int tick = -1;
        private PathfindResult poses;
    }
    private RoomPresetPathPlanner pathPlanner;

    public ClassicPathDisplayEngine(ActionRoute actionRoute, ClassicPathEngineLineProperties initialRouteProperties) {
        this.actionRoute = actionRoute;
        this.dungeonRoom = actionRoute.getDungeonRoom();
        this.pathPlanner = new RoomPresetPathPlanner(actionRoute.getDungeonRoom().getContext().getPreset().getRoomPreset(actionRoute.getDungeonRoom().getDungeonRoomInfo().getUuid()));
        this.classicPathEngineLineProperties = initialRouteProperties;
    }

    @Override
    public void tick() {
        actionRoute.onTick();

        for (ActionMoveContext value : executorWeakHashMap.values()) {
            value.mark = false;
        }

        tickAction(actionRoute.getCurrentAction());
        if (actionRoute.getCurrent() >= 1)
            tickAction(actionRoute.getActions().get(actionRoute.getCurrent()-1));

        executorWeakHashMap.entrySet().removeIf(entry -> !entry.getValue().mark); // TODO: decide if explicit cleanup is needed
    }

    private void forceRefresh(AbstractActionMove actionMove) {
        if (!executorWeakHashMap.containsKey(actionMove)) executorWeakHashMap.put(actionMove, new ActionMoveContext());
        ActionMoveContext ctx = executorWeakHashMap.get(actionMove);

        if (ctx.executor == null) ctx.executor = pathPlanner.loadPrecalculatedByHash(actionMove.getPathfindRequest(dungeonRoom).getHash(), dungeonRoom);
        if (ctx.executor != null) ctx.executor.setTarget(Minecraft.getInstance().player.position());
    }

    private void tickAction(AbstractAction action) {
        if (action instanceof AtomicAction) {
            tickAction(((AtomicAction) action).getCurrentAction());
            if (((AtomicAction) action).getCurrent() >= 1)
                tickAction(((AtomicAction) action).getActions().get(((AtomicAction) action).getCurrent() - 1));;
            return;
        }

        if (action instanceof AbstractActionMove) {
            AbstractActionMove actionMove = (AbstractActionMove) action;
            if (!executorWeakHashMap.containsKey(actionMove)) executorWeakHashMap.put(actionMove, new ActionMoveContext());
            ActionMoveContext ctx = executorWeakHashMap.get(actionMove);
            ctx.mark = true;

            ctx.tick = (ctx.tick+1) % Math.max(1, classicPathEngineLineProperties.getLineRefreshRate());
            if (ctx.executor == null && classicPathEngineLineProperties.isPathfind()) {
                forceRefresh(actionMove);
            }
            if (ctx.executor != null && (ctx.poses == null || !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled())) {
                ctx.poses = ctx.executor.getRoute(Minecraft.getInstance().player.position());
            }

            if (ctx.tick == 0 && classicPathEngineLineProperties.isPathfind() && ctx.executor != null) {
                if (classicPathEngineLineProperties.getLineRefreshRate() != -1 && !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() && ctx.executor.isComplete()) {
                    ctx.executor.setTarget(Minecraft.getInstance().player.position());
                }
            }
            return;
        }
    }

    @Override
    public ActionRoute getActionRoute() {
        return actionRoute;
    }

    @Override
    public ClassicPathEngineLineProperties getSettings() {
        return classicPathEngineLineProperties;
    }

    @Override
    public void setSettings(ClassicPathEngineLineProperties settings) {
        this.classicPathEngineLineProperties = settings;
    }

    @Override
    public void renderActionRoute(RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        if (actionRoute.isCalculating()) return;

        DungeonRoom dungeonRoom = actionRoute.getDungeonRoom();
        int current = actionRoute.getCurrent();
        List<AbstractAction> actions = actionRoute.getActions();
        if (current -1 >= 0) {
            AbstractAction abstractAction = actions.get(current - 1);
            if(((abstractAction instanceof AbstractActionMove && ((AbstractActionMove) abstractAction).getTargetVec3().getPos(dungeonRoom).distanceToSqr(Minecraft.getInstance().player.position()) >= 25))) {
                drawActionMove((AbstractActionMove) abstractAction, dungeonRoom, event);
            }
        }

        AbstractAction currentAction = actionRoute.getCurrentAction();
        renderAction(currentAction, dungeonRoom, event);
    }

    public void renderAction(AbstractAction currentAction, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        if (currentAction instanceof AbstractActionMove) {
            drawActionMove((AbstractActionMove) currentAction, dungeonRoom, event);
        } else if (currentAction instanceof ActionClick) {
            renderActionClick((ActionClick) currentAction, dungeonRoom, event);
        } else if (currentAction instanceof ActionClickSet) {
            renderActionClickSet((ActionClickSet) currentAction, dungeonRoom, event);
        } else if (currentAction instanceof ActionStonkClick) {
            renderActionStonkClick((ActionStonkClick) currentAction, dungeonRoom, event);
        } else if (currentAction instanceof ActionKill) {
            renderActionKill((ActionKill) currentAction, dungeonRoom, event);
        } else if (currentAction instanceof ActionInteract) {
            renderActionInteract((ActionInteract) currentAction, dungeonRoom , event);
        } else if (currentAction instanceof ActionDropItem) {
            renderActionDropItem((ActionDropItem) currentAction, dungeonRoom, event);
        } else if (currentAction instanceof ActionBreakWithSuperBoom) {
            renderActionBreakWithSuperboom((ActionBreakWithSuperBoom) currentAction, dungeonRoom, event);
        } else if (currentAction instanceof AtomicAction) {
            int atomicActionCurrent = ((AtomicAction) currentAction).getCurrent();
            List<AbstractAction> atomicActionActions = ((AtomicAction) currentAction).getActions();
            if (atomicActionCurrent -1 >= 0) {
                AbstractAction abstractAction = atomicActionActions.get(atomicActionCurrent - 1);
                if(((abstractAction instanceof AbstractActionMove && ((AbstractActionMove) abstractAction).getTargetVec3().getPos(dungeonRoom).distanceToSqr(Minecraft.getInstance().player.position()) >= 25))) {
                    drawActionMove((AbstractActionMove) abstractAction, dungeonRoom, event);
                }
            }
            renderAction(((AtomicAction) currentAction).getCurrentAction(), dungeonRoom, event);
        }
    }


    public void renderActionKill(ActionKill actionKill, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos pos = actionKill.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Spawn", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionInteract(ActionInteract actionInteract, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos pos = actionInteract.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Interact", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionDropItem(ActionDropItem dropItem, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos pos = dropItem.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, true);
        RenderUtils.drawTextAtWorld("Drop Item", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionClickSet(ActionClickSet actionClickSet, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        float xAcc = 0;
        float yAcc = 0;
        float zAcc = 0;
        int size = actionClickSet.getTarget().getOffsetPointList().size();
        for (OffsetPoint offsetPoint : actionClickSet.getTarget().getOffsetPointList()) {
            BlockPos pos = offsetPoint.getBlockPos(dungeonRoom);
            xAcc += pos.getX() + 0.5f;
            yAcc += pos.getY()+ 0.5f;
            zAcc += pos.getZ()+ 0.5f;
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(dungeonRoom), new Color(0, 255,255,50),partialTicks, true);
        }

        RenderUtils.drawTextAtWorld("Click", xAcc / size, yAcc / size, zAcc / size, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionClick(ActionClick actionClick, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos pos = actionClick.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, false);
        RenderUtils.drawTextAtWorld("Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionStonkClick(ActionStonkClick actionStonkClick, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos pos = actionStonkClick.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, false);
        RenderUtils.drawTextAtWorld("Stonk&Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionBreakWithSuperboom(ActionBreakWithSuperBoom superBoom, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos blockpos = superBoom.getTarget().getOffsetPointList().get(0).getBlockPos(dungeonRoom);

        // 1.8.9 rendered the tnt model with depth off
        DGWorldRenderer.record(new BlockModelState(Blocks.TNT.defaultBlockState(),
                new Matrix4f().translate(blockpos.getX(), blockpos.getY(), blockpos.getZ()), false));

        RenderUtils.highlightBlock(blockpos, new Color(0, 255,255,50), partialTicks, true);
        RenderUtils.drawTextAtWorld("Superboom", blockpos.getX() + 0.5f, blockpos.getY() + 0.5f, blockpos.getZ() + 0.5f, 0xFFFFFF00, 0.03f, false, false, partialTicks);
    }

    public void drawActionMove(AbstractActionMove actionMove, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        ActionMoveContext context = executorWeakHashMap.get(actionMove);

        BlockPos target = actionMove.getBeaconTargetPos(dungeonRoom);
        PathfindResult poses = context == null ? null : context.poses;
        boolean flag2 =  FeatureRegistry.SECRET_FREEZE_LINES.isEnabled();



        // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
        float distance = (float) Math.sqrt(target.distSqr(Minecraft.getInstance().player.blockPosition()));
        float multiplier = distance / 120f; //mobs only render ~120 blocks away
        float scale = 0.45f * multiplier;
        scale *= 25.0 / 6.0;
        if (classicPathEngineLineProperties.isBeacon()) {
            RenderUtils.renderBeaconBeam(target.getX(), target.getY(), target.getZ(), classicPathEngineLineProperties.getBeaconBeamColor(), partialTicks);
            RenderUtils.highlightBlock(target, classicPathEngineLineProperties.getBeaconColor(), partialTicks);
        }
        RenderUtils.drawTextAtWorld("Destination", target.getX() + 0.5f, target.getY() + 0.5f + scale, target.getZ() + 0.5f, 0xFF00FF00, 1f, true, false, partialTicks);

        // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
        RenderUtils.drawTextAtWorld(String.format("%.2f",Math.sqrt(target.distSqr(Minecraft.getInstance().player.blockPosition())))+"m", target.getX() + 0.5f, target.getY() + 0.5f - scale, target.getZ() + 0.5f, 0xFFFFFF00, 1f, true, false, partialTicks);

        if (!FeatureRegistry.SECRET_TOGGLE_KEY.isEnabled() || !FeatureRegistry.SECRET_TOGGLE_KEY.togglePathfindStatus) {
            if (poses != null){
                drawLinesPathfindNode(poses.getNodeList(), classicPathEngineLineProperties.getLineColor(), (float) classicPathEngineLineProperties.getLineWidth(), event);

                int cnt = 0;
                int warp = 0;
                for (PathfindResult.PathfindNode pose : poses.getNodeList()) {
                    cnt ++;
                    // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
                    if (pose.getType() != null && pose.getType() != PathfindResult.PathfindNode.NodeType.WALK && pose.getType() != PathfindResult.PathfindNode.NodeType.STONK_WALK && pose.distanceSq(Minecraft.getInstance().player.blockPosition()) < 100) {
                        RenderUtils.drawTextAtWorld(pose.getType().toString(), pose.getX(), pose.getY() + 0.5f, pose.getZ(), 0xFF00FF00, 0.02f, false, true, partialTicks);
                    }

                    if (warp == 1) {
                        BlockPos pos = BlockPos.containing(pose.getX(), Math.floor(pose.getY()) -1 , pose.getZ());
                        RenderUtils.highlightBox(new AABB(pos).inflate(0.003), Color.green, partialTicks, true);
                        warp = flag2 ? 0 : 2;
                    }
                    if (pose.getType() == PathfindResult.PathfindNode.NodeType.ETHERWARP &&
                            ((flag2 && Minecraft.getInstance().player.distanceToSqr(pose.getX(), pose.getY(), pose.getZ()) < 25) || (!flag2 && cnt < 2)) && warp < 2) {
                        warp = 1;
                    }

                }
            }
        }

        if (actionMove instanceof ActionMoveSpot)
            LineRenderUtils.renderDebug((ActionMoveSpot) actionMove, dungeonRoom, event);
        else if (actionMove instanceof ActionMove)
            LineRenderUtils.renderDebug((ActionMove) actionMove, dungeonRoom, event);
    }



    public static void drawLinesPathfindNode(List<PathfindResult.PathfindNode> poses, AColor colour, float thickness, RenderWorldEvent event) {
        if (poses.size() == 0) return;

        // 1.8.9 drew a line strip per run of node types, with depth off for stonk walks near the player
        // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
        boolean depth = !((poses.get(0).getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK ) && poses.get(0).distanceSq(Minecraft.getInstance().player.blockPosition()) < 100);
        List<Vec3> points = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int num = 0;

        PathfindResult.PathfindNode lastNode = null;
        for (PathfindResult.PathfindNode pos:poses) {
            int i = RenderUtils.getColorAt(num++ * 10,0, colour);
            points.add(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            colors.add(i);

            if (lastNode != null && lastNode.getType() != pos.getType()) {
                recordLineStrip(points, colors, thickness, depth);
                points = new ArrayList<>();
                colors = new ArrayList<>();

                // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
                depth = !((pos.getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK || poses.get(0).getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK)&& pos.distanceSq(Minecraft.getInstance().player.blockPosition()) < 100);

                points.add(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                colors.add(i);
            }
            lastNode = pos;

        }
        recordLineStrip(points, colors, thickness, depth);
    }

    private static void recordLineStrip(List<Vec3> points, List<Integer> colors, float thickness, boolean depth) {
        DGWorldRenderer.record(new LineStripState(points, colors.stream().mapToInt(Integer::intValue).toArray(), thickness, false, depth));
    }
}
