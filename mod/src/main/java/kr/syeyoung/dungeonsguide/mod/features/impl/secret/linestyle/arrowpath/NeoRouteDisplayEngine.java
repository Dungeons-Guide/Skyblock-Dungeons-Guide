package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.arrowpath;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.LineRenderUtils;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.routedisplay.LiveRouteRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderWorldEvent;
import kr.syeyoung.dungeonsguide.mod.render.world.DGWorldRenderer;
import kr.syeyoung.dungeonsguide.mod.render.world.state.QuadState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.ShaderQuadState;
import kr.syeyoung.dungeonsguide.mod.render.world.state.TexturedQuadState;
import kr.syeyoung.dungeonsguide.mod.shader.RepeatUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;

public class NeoRouteDisplayEngine implements IPathDisplayEngine<NeoRouteDisplayEngineLineProperties> {

    private final Map<AbstractActionMove, ActionMoveContext> executorWeakHashMap = new WeakHashMap<>();
    private final ActionRoute actionRoute;
    private final DungeonRoom dungeonRoom;

    private static class ActionMoveContext {
        private boolean mark = false;
        private PathfinderExecutor executor;
        private PathfindResult poses;
        private List<PathSegment> segment;
    }
    private RoomPresetPathPlanner pathPlanner;

    public NeoRouteDisplayEngine(ActionRoute actionRoute, NeoRouteDisplayEngineLineProperties settings) {
        this.actionRoute = actionRoute;
        this.dungeonRoom = actionRoute.getDungeonRoom();
        this.pathPlanner = new RoomPresetPathPlanner(actionRoute.getDungeonRoom().getContext().getPreset().getRoomPreset(actionRoute.getDungeonRoom().getDungeonRoomInfo().getUuid()));
        this.settings = settings;
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

            if (ctx.executor == null) {
                forceRefresh(actionMove);
            }
            if (ctx.executor != null && (ctx.poses == null || !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled())) {
                ctx.poses = ctx.executor.getRoute(Minecraft.getInstance().player.position());
                if (ctx.poses != null)
                    ctx.segment = transformPathfindResult(ctx.poses.getNodeList(), settings.getWidth(), settings.getSmooth());
            }

            if (ctx.executor != null) {
                if (!FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() && ctx.executor.isComplete()) {
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

    @Getter @Setter
    private NeoRouteDisplayEngineLineProperties settings;

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
        List<BlockPos> list = new ArrayList<>();
        for (OffsetPoint offsetPoint : actionClickSet.getTarget().getOffsetPointList()) {
            BlockPos pos = offsetPoint.getBlockPos(dungeonRoom);
            xAcc += pos.getX() + 0.5f;
            yAcc += pos.getY()+ 0.5f;
            zAcc += pos.getZ()+ 0.5f;
            list.add(pos);
        }
        RenderUtils.highlightBlocksStencil(list, partialTicks, new AColor(0, 255,255,50), true);

        RenderUtils.drawTextAtWorld("Click", xAcc / size, yAcc / size, zAcc / size, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionClick(ActionClick actionClick, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos pos = actionClick.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlockStencil(pos, partialTicks,new AColor(0, 255,0,100), false);
        RenderUtils.drawTextAtWorld("Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);

        Vec3 from = new Vec3(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
        Vec3 eyePos = Minecraft.getInstance().player.getEyePosition(partialTicks);
        Vec3 lookVec = from.subtract(eyePos).normalize();

        if (Minecraft.getInstance().player.getViewVector(partialTicks).dot(lookVec) < 0.7) return;

        HitResult objectPosition = Minecraft.getInstance().hitResult;
        if (objectPosition.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) objectPosition).getBlockPos();
            for (Map.Entry<String, DungeonMechanicState> stringDungeonMechanicStateEntry : dungeonRoom.getMechanics().entrySet()) {
                if (stringDungeonMechanicStateEntry.getValue() instanceof WorldMutatingMechanicState && ((WorldMutatingMechanicState) stringDungeonMechanicStateEntry.getValue()).isBlocking(dungeonRoom)) {
                    List<OffsetPoint> offsetPointList = ((WorldMutatingMechanicState)stringDungeonMechanicStateEntry.getValue()).blockedPoints();
                    List<BlockPos> blocks = offsetPointList.stream().map(a -> a.getBlockPos(dungeonRoom)).collect(Collectors.toList());
                    for (BlockPos block : blocks) {
                        if (blockPos.equals(block)) {
                            highlightSuperboom(blocks, event, new AColor(255, 0, 0, 50));
                            break;
                        }
                    }
                }
            }
        }

    }

    public void renderActionStonkClick(ActionStonkClick actionStonkClick, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        BlockPos pos = actionStonkClick.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlockStencil(pos, partialTicks,new AColor(0, 255,0,100), false);
        RenderUtils.drawTextAtWorld("Stonk&Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionBreakWithSuperboom(ActionBreakWithSuperBoom superBoom, DungeonRoom dungeonRoom, RenderWorldEvent event) {
        float partialTicks = event.partialTicks;
        List<BlockPos> pos = new ArrayList<>();
        for (OffsetPoint offsetPoint : superBoom.getTarget().getOffsetPointList()) {
            pos.add(offsetPoint.getBlockPos(dungeonRoom));
        }

        highlightSuperboom(pos, event, new AColor(255,0,0,50));
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
        scale *= (float) settings.getDestinationSize();
        scale *= 25.0 / 6.0;

        if (getSettings().isEnableBeacon()) {
            RenderUtils.renderBeaconBeam(target.getX(), target.getY(), target.getZ(), settings.getBeamColor(), partialTicks);
            RenderUtils.highlightBlock(target, settings.getBeaconColor(), partialTicks);
        }

        if (settings.getDestinationSize() != 0) {
            RenderUtils.drawTextAtWorld("Destination", target.getX() + 0.5f, target.getY() + 0.5f + scale, target.getZ() + 0.5f, 0xFF00FF00, (float) settings.getDestinationSize(), true, false, partialTicks);
            // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
            RenderUtils.drawTextAtWorld(String.format("%.2f", Math.sqrt(target.distSqr(Minecraft.getInstance().player.blockPosition()))) + "m", target.getX() + 0.5f, target.getY() + 0.5f - scale, target.getZ() + 0.5f, 0xFFFFFF00, (float) settings.getDestinationSize(), true, false, partialTicks);
        }

        if (!FeatureRegistry.SECRET_TOGGLE_KEY.isEnabled() || !FeatureRegistry.SECRET_TOGGLE_KEY.togglePathfindStatus) {
            if (poses != null){
                drawLinesPathfindNode(context.segment, settings.getBackground(), settings.getArrow(), event);

                Vec3 pos = Minecraft.getInstance().player.position();
                if (settings.isEnableEtherwarpTracer()) {
                    for (PathSegment segment : context.segment) {
                        if (segment.nodeType == PathfindResult.PathfindNode.NodeType.ETHERWARP && segment.from.distanceToSqr(pos) < settings.getEtherwarpTracerDist()) {
                            RenderUtils.drawLinesVec3(Arrays.asList(
                                    Minecraft.getInstance().player.getEyePosition(partialTicks),
                                    segment.to.add(0, -0.5, 0)), settings.getEtherwarpTracerColor(), settings.getEtherwarpTracerWidth(), partialTicks, false);
                        }
                    }
                }

                PathfindResult.PathfindNode last = null;
                for (PathfindResult.PathfindNode pose : poses.getNodeList()) {
                    if (pose.getType() != null &&
                            pose.getType() != PathfindResult.PathfindNode.NodeType.WALK &&
                            pose.getType() != PathfindResult.PathfindNode.NodeType.STONK_WALK &&
                            pose.getType() != PathfindResult.PathfindNode.NodeType.ENDERPEARL &&
                            pose.getType() != PathfindResult.PathfindNode.NodeType.ETHERWARP &&
                            pose.getType() != PathfindResult.PathfindNode.NodeType.SUPERBOOM &&
                            // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
                            pose.distanceSq(Minecraft.getInstance().player.blockPosition()) < 100) {
                        RenderUtils.drawTextAtWorld(pose.getType().toString(), pose.getX(), pose.getY() + 0.5f, pose.getZ(), 0xFF00FF00, 0.02f, false, true, partialTicks);
                    }
                    if (last != null && last.getType() == PathfindResult.PathfindNode.NodeType.SUPERBOOM) {
                        BlockPos test = BlockPos.containing(pose.getX(), pose.getY(), pose.getZ());
                        for (Map.Entry<String, DungeonMechanicState> stringDungeonMechanicStateEntry : dungeonRoom.getMechanics().entrySet()) {
                            if (stringDungeonMechanicStateEntry.getValue() instanceof WorldMutatingMechanicState && ((WorldMutatingMechanicState) stringDungeonMechanicStateEntry.getValue()).isBlocking(dungeonRoom)) {
                                List<OffsetPoint> offsetPointList = ((WorldMutatingMechanicState)stringDungeonMechanicStateEntry.getValue()).blockedPoints();
                                List<BlockPos> blocks = offsetPointList.stream().map(a -> a.getBlockPos(dungeonRoom)).collect(Collectors.toList());
                                for (BlockPos block : blocks) {
                                    if (test.distSqr(block) <= 2) {
                                        highlightSuperboom(blocks, event, new AColor(255, 0, 0, 50));
                                        break;
                                    }
                                }
                            }
                        }

                    }
                    last = pose;

                }
            }
        }

        if (actionMove instanceof ActionMoveSpot)
            LineRenderUtils.renderDebug((ActionMoveSpot) actionMove, dungeonRoom, event);
        else if (actionMove instanceof ActionMove)
            LineRenderUtils.renderDebug((ActionMove) actionMove, dungeonRoom, event);
    }

    @AllArgsConstructor @Data
    public static class PathSegment {
        Vec3 from, to;
        Vec3 dir, side;
        Vec3 BL, BR, TL, TR;
        PathfindResult.PathfindNode.NodeType nodeType;
        double texCulLenFrom, texCulLenTo;
    }
    private static final  Map<PathfindResult.PathfindNode.NodeType, Integer> nodeTypeGroupMap = new HashMap<>();
    static {
        nodeTypeGroupMap.put(PathfindResult.PathfindNode.NodeType.ETHERWARP, 1);
        nodeTypeGroupMap.put(PathfindResult.PathfindNode.NodeType.ENDERPEARL, 2);
        for (PathfindResult.PathfindNode.NodeType value : PathfindResult.PathfindNode.NodeType.values()) {
            if (nodeTypeGroupMap.containsKey(value)) continue;
            nodeTypeGroupMap.put(value, 0);
        }
    }

    public static List<PathSegment> transformPathfindResult(List<PathfindResult.PathfindNode> poses, double width, double smooth) {
        if (poses.isEmpty()) return Collections.emptyList();
        if (poses.size() == 1)return Collections.emptyList();

        List<PathSegment> segments = new ArrayList<>();
        PathfindResult.PathfindNode last = null;
        Vec3 up = new Vec3(0, 1, 0);
        for (PathfindResult.PathfindNode pose : poses) {
            if (last != null) {
                Vec3 from = new Vec3(last.getX(), last.getY(), last.getZ());
                Vec3 to = new Vec3(pose.getX(), pose.getY(), pose.getZ());
                Vec3 dir = to.subtract(from).normalize();

                Vec3 normal = dir.cross(up).normalize();

                segments.add(new PathSegment(
                        from.add(0, 0.05, 0), to.add(0, 0.05, 0), dir, normal, null,null,null,null, last.getType(),0 ,0
                ));
            }
            last = pose;
        }

        List<PathSegment> result = new ArrayList<>();
        for (int i = 1; i < segments.size(); i++) {
            Vec3 dir1 = segments.get(i-1).dir;
            Vec3 dir2 = segments.get(i).dir;
            if (dir1.x == dir2.x && dir2.y == dir1.y && dir1.z == dir2.z && segments.get(i-1).nodeType == segments.get(i).nodeType) {
                segments.get(i).from = segments.get(i-1).from;
            } else {
                result.add(segments.get(i-1));
            }
        }
        result.add(segments.get(segments.size()-1)); // segment merging.

        double rad = smooth;
        double thickness = width;

        List<PathSegment> realResult = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            int fromIdx = i;
            int groupFrom = nodeTypeGroupMap.get(result.get(fromIdx).nodeType);
            for (i++; i < result.size() && nodeTypeGroupMap.get(result.get(i).nodeType) == groupFrom && groupFrom == 0; i++);
            int toIdx = i; i--;

            PathSegment prev = result.get(fromIdx);
            realResult.add(prev);
            for (int j = fromIdx + 1; j < toIdx; j++) {
                PathSegment to = result.get(j);
                double lenTo = to.to.subtract(to.from).length();
                double lenFrom = prev.to.subtract(prev.from).length();
                double radTo = Math.min(lenTo/2, rad);
                double radFrom = Math.min(lenFrom / 2, rad);

                Vec3 scaledVecTo = new Vec3(to.dir.x * radTo, to.dir.y * radTo, to.dir.z * radTo);
                Vec3 scaledVecFrom = new Vec3(prev.dir.x * radFrom, prev.dir.y*radFrom, prev.dir.z * radFrom);
                Vec3 P2 = to.from;
                to.from = to.from.add(scaledVecTo);
                prev.to = prev.to.subtract(scaledVecFrom);
                Vec3 P1 = prev.to, P3 = to.from;

                PathSegment realPrev = prev;
                for (double t = 0.1; t < 1.0; t += 0.1) {
                    double xCoord = P1.x * (1-t)*(1-t) + P2.x * 2*(1-t) * t + P3.x * t * t;
                    double yCoord = P1.y * (1-t)*(1-t) + P2.y * 2*(1-t) * t + P3.y * t * t;
                    double zCoord = P1.z * (1-t)*(1-t) + P2.z * 2*(1-t) * t + P3.z * t * t;
                    Vec3 pt = new Vec3(xCoord, yCoord, zCoord);

                    double xTangent = P1.x * 2 * (t-1) + P2.x * (2 - 4*t) + P3.x * (2*t);
                    double yTangent = P1.y * 2 * (t-1) + P2.y * (2 - 4*t) + P3.y * (2*t);
                    double zTangent = P1.z * 2 * (t-1) + P2.z * (2 - 4*t) + P3.z * (2*t);
                    Vec3 dir = new Vec3(xTangent, yTangent, zTangent);

                    realResult.add(realPrev = new PathSegment(
                            realPrev.to, pt, dir.normalize(), dir.cross(up).normalize(), null, null, null, null, realPrev.nodeType, 0, 0
                    ));
                }
                realResult.add(to);
                prev = to;
            }
        }

        for (int i = 1; i < realResult.size(); i++) {
            if (realResult.get(i).side.length() == 0)
                realResult.get(i).side = realResult.get(i-1).side;
        }
        for (int i = realResult.size()-2; i >= 0; i--) {
            if (realResult.get(i).side.length() == 0)
                realResult.get(i).side = realResult.get(i+1).side;
        }
        for (PathSegment segment : realResult) {
            if (segment.side.length() == 0)
                segment.side = new Vec3(1, 0, 0);
        }


        for (int i = 0; i < realResult.size(); i++) {
            int fromIdx = i;
            for (; i < realResult.size() && realResult.get(i).nodeType == realResult.get(fromIdx).nodeType; i++);
            int toIdx = i; i--;

            PathSegment prev = realResult.get(fromIdx);
            prev.BL = prev.from.subtract(prev.side.x * thickness/2, prev.side.y * thickness/2, prev.side.z * thickness/2);
            prev.BR = prev.from.add(prev.side.x * thickness/2, prev.side.y * thickness/2, prev.side.z * thickness/2);
            for (int j = fromIdx+1; j < toIdx; j++) {
                PathSegment curr = realResult.get(j);
                Vec3 prevSideScaled = new Vec3(prev.side.x * thickness/2, prev.side.y * thickness/2, prev.side.z * thickness/2);

                prev.TL = prev.to.subtract(prevSideScaled);
                prev.TR = prev.to.add(prevSideScaled);
                curr.BL = prev.TL;
                curr.BR = prev.TR;

                prev = curr;
            }
            PathSegment to = realResult.get(toIdx-1);
            to.TL = to.to.subtract(to.side.x * thickness/2, to.side.y * thickness/2, to.side.z * thickness/2);
            to.TR = to.to.add(to.side.x * thickness/2, to.side.y * thickness/2, to.side.z * thickness/2);
        }
        double culLen = 0;
        for (PathSegment segment : realResult) {
            double len = segment.to.subtract(segment.from).length();
            segment.texCulLenFrom = culLen / thickness;
            culLen += len;
            segment.texCulLenTo = culLen / thickness;
        }

        return realResult;
    }


    private static final Identifier tntSide = Identifier.parse("minecraft:textures/block/tnt_side.png");

    private static void highlightSuperboom(List<BlockPos> blockPos, RenderWorldEvent event, AColor color) {
        float partialTicks = event.partialTicks;
        AABB bb = RenderUtils.highlightBlocksStencil(blockPos, partialTicks, color, true);

        int[][][] quads = {
                {{0,0,-1}, {1,0,-1}, {1,1,-1}, {0,1,-1}},
                {{0,0,2}, {1,0,2}, {1,1,2}, {0,1,2}},
                {{-1,0,0}, {-1,0,1}, {-1,1,1}, {-1,1,0}},
                {{2,0,0}, {2,0,1}, {2,1,1}, {2,1,0}},
                {{0,-1,0}, {1,-1,0}, {1,-1,1}, {0,-1,1}},
                {{0,2,0}, {1,2,0}, {1,2,1}, {0,2,1}}
        };
        float[][] quads2 = {
                {0,1}, {1,1}, {1,0}, {0,0}
        };

        double size = 1;
        double zLee = 0.001;
        double[][] faces = {
                {bb.minX-zLee, (bb.minX+bb.maxX)/2 - size/2, (bb.minX+bb.maxX)/2 + size/2, bb.maxX+zLee},
                {bb.minY-zLee, (bb.minY+bb.maxY)/2 - size/2, (bb.minY+bb.maxY)/2 + size/2, bb.maxY+zLee},
                {bb.minZ-zLee, (bb.minZ+bb.maxZ)/2 - size/2, (bb.minZ+bb.maxZ)/2 + size/2, bb.maxZ+zLee}
        };

        for (int i = 0; i < 6; i++) {
            Vec3[] corners = new Vec3[4];
            for (int j = 0; j < 4; j++) {
                corners[j] = new Vec3(faces[0][quads[i][j][0]+1],
                                faces[1][quads[i][j][1]+1],
                                faces[2][quads[i][j][2]+1]);
            }
            DGWorldRenderer.record(new TexturedQuadState(tntSide, corners, quads2, 0xFFFFFFFF, true));
        }
    }


    public static final Identifier arrow = Identifier.parse("dungeonsguide:textures/arrow.png");
    public static final Identifier abilities = Identifier.parse("dungeonsguide:textures/features/precalclist/abilities.png");

    // the arrow repeated along the quad by the repeat shader. 1.8.9 passed the arrow sprite's rect in the block atlas,
    // 26.1 binds the standalone texture (D128)
    private static void drawArrows(Vec3[] corners, float[][] uvs, int color, boolean depth) {
        DGWorldRenderer.record(new ShaderQuadState(ShaderManager.getShader("shaders/repeat"), new RepeatUniforms(0, 0, 1, 1),
                arrow, corners, uvs, color, depth));
    }

    // a point of 1.8.9's icon billboard: translate(from + (0, 0.5, 0)), rotate(-viewY, y), rotate(viewX, x), scale(1/32, -1/32, 1/32)
    private static Vec3 billboardCorner(Vec3 from, Matrix4f rotation, float u, float v) {
        Vector3f offset = rotation.transformPosition(new Vector3f(u, v, 0));
        return from.add(offset.x, offset.y + 0.5, offset.z);
    }

    public static void drawIcon(int iconIdx, PathSegment segment, AColor colour, AColor texture, RenderWorldEvent event, double animate, boolean path) {
        float partialTicks = event.partialTicks;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        boolean flag = camera.position().distanceToSqr(segment.from.x, segment.from.y, segment.from.z) > 0.25;
        Vec3 fixedFrom = flag ? segment.from.add(0, 1.5, 0) : segment.from;
        Vec3 dir = segment.to.subtract(fixedFrom);
        if (path){
            Vec3 normal;
            if (flag) {
                normal = dir.cross(Minecraft.getInstance().getCameraEntity().getLookAngle()).normalize();
                if (normal.length() == 0) normal = new Vec3(1, 0, 0);
                normal = new Vec3(normal.x * 0.05, normal.y * 0.05, normal.z * 0.05);
            } else {
                normal = new Vec3(segment.side.x * 0.05, segment.side.y * 0.05, segment.side.z * 0.05);
            }

            double len = segment.to.subtract(segment.from).length();
            int i = RenderUtils.getColorAt(0, 0, 0, colour);
            Vec3 quadBL = fixedFrom.add(normal), quadBR = fixedFrom.subtract(normal), quadTL = segment.to.add(normal), quadTR = segment.to.subtract(normal);
            // 1.8.9 turned depth off when the segment starts at the camera
            DGWorldRenderer.record(new QuadState(new Vec3[] {quadBL, quadBR, quadTR, quadTL}, i, flag));

            i = RenderUtils.getColorAt(0,0,0, texture);
            drawArrows(new Vec3[] {quadBL, quadBR, quadTR, quadTL}, new float[][] {
                    {0, (float) -animate},
                    {1, (float) -animate},
                    {1, (float) (-len / 0.1-animate)},
                    {0, (float) (-len / 0.1-animate)}
            }, i, flag);
        }

        Matrix4f rotation = new Matrix4f()
                .rotateY((float) Math.toRadians(-camera.yRot()))
                .rotateX((float) Math.toRadians(camera.xRot()))
                .scale(1/32.0f, -1/32.0f, 1/32.0f);

        int size = 16;
        DGWorldRenderer.record(new QuadState(new Vec3[] {
                billboardCorner(segment.from, rotation, -size /2 - 1, -size /2-1),
                billboardCorner(segment.from, rotation, -size /2 - 1, size /2+1),
                billboardCorner(segment.from, rotation, size /2 + 1, size /2+1),
                billboardCorner(segment.from, rotation, size /2 + 1, -size /2-1)
        }, 0x40000000, true));

        // 1.8.9's drawScaledCustomSizeModalRect(-size/2, -size/2, u, v, 31, 31, size, size, 512, 512)
        float u = (iconIdx % 8)*32 + 0.5f, v = (iconIdx/8)*32 + 0.5f;
        DGWorldRenderer.record(new TexturedQuadState(abilities, new Vec3[] {
                billboardCorner(segment.from, rotation, -size /2, size /2),
                billboardCorner(segment.from, rotation, size /2, size /2),
                billboardCorner(segment.from, rotation, size /2, -size /2),
                billboardCorner(segment.from, rotation, -size /2, -size /2)
        }, new float[][] {
                {u / 512, (v + 31) / 512},
                {(u + 31) / 512, (v + 31) / 512},
                {(u + 31) / 512, v / 512},
                {u / 512, v / 512}
        }, 0xFFFFFFFF, true));

        // TODO: 1.8.9 drew this highlight with polygon offset (-1, -1), 26.1 draws it as is
        BlockPos pos = BlockPos.containing(segment.to.x, Math.floor(segment.to.y) -1 , segment.to.z);
        RenderUtils._highlightBlock(pos, Color.green, partialTicks, true);
    }

    public static void drawLinesNormal(List<PathSegment> poses, int from, int to, PathfindResult.PathfindNode.NodeType type, AColor colour, AColor texture, RenderWorldEvent event, double animate) {
        boolean depth = !(type == PathfindResult.PathfindNode.NodeType.STONK_WALK || type == PathfindResult.PathfindNode.NodeType.STONK_EXIT);

        int i = RenderUtils.getColorAt(0,0,0, colour);
        for (int j = from; j < to; j++) {
            PathSegment pos = poses.get(j);
            DGWorldRenderer.record(new QuadState(new Vec3[] {pos.BL, pos.BR, pos.TR, pos.TL}, i, depth));
        }

        i = RenderUtils.getColorAt(0,0,0, texture);
        for (int j = from; j < to; j++) {
            PathSegment pos = poses.get(j);
            drawArrows(new Vec3[] {pos.BL, pos.BR, pos.TR, pos.TL}, new float[][] {
                    {0, (float) (-pos.texCulLenFrom-animate)},
                    {1, (float) (-pos.texCulLenFrom-animate)},
                    {1, (float) (-pos.texCulLenTo-animate)},
                    {0, (float) (-pos.texCulLenTo-animate)}
            }, i, depth);
        }
    }
    public void drawLinesPathfindNode(List<PathSegment> poses, AColor colour, AColor texture, RenderWorldEvent event) {
        if (poses.size() == 0) return;
        double speed = settings.getAnimationSpeed();
        double animate = System.currentTimeMillis() / 1000.0 * speed;
        animate = animate - Math.floor(animate);
        animate *= -1;

        for (int i = 0; i < poses.size(); i++) {
            int st = i;
            if (nodeTypeGroupMap.get(poses.get(st).nodeType) == 0)
                for (i++; i < poses.size() && poses.get(st).nodeType == poses.get(i).nodeType; i++);
            else i++;
            int toIdx = i; i--;

            PathfindResult.PathfindNode.NodeType nodeType = poses.get(st).nodeType;
            if (nodeType == PathfindResult.PathfindNode.NodeType.ETHERWARP) {
                boolean path = true;
                if (settings.isEtherwarpTracerDisableEtherwarpRoute() && settings.isEnableEtherwarpTracer()) {
                    if (Minecraft.getInstance().player.position().distanceToSqr(poses.get(st).from) < settings.getEtherwarpTracerDist()) path = false;
                }
                drawIcon(40, poses.get(st), colour, texture, event, animate, path);
            } else if (nodeType == PathfindResult.PathfindNode.NodeType.ENDERPEARL)
                drawIcon(41, poses.get(st), colour, texture, event, animate, true);
            else
                drawLinesNormal(poses, st, toIdx, poses.get(st).nodeType, colour, texture, event, animate);
        }
    }
}
