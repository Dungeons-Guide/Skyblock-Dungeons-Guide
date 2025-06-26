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
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.util.RaycastResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
        if (ctx.executor != null) ctx.executor.setTarget(ModAPI.getAPI().getPlayer().getPositionVector());
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
                ctx.poses = ctx.executor.getRoute(ModAPI.getAPI().getPlayer().getPositionVector());
                if (ctx.poses != null)
                    ctx.segment = transformPathfindResult(ctx.poses.getNodeList(), settings.getWidth(), settings.getSmooth());
            }

            if (ctx.executor != null) {
                if (!FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() && ctx.executor.isComplete()) {
                    ctx.executor.setTarget(ModAPI.getAPI().getPlayer().getPositionVector());
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
    public void renderActionRoute(float partialTicks) {
        if (actionRoute.isCalculating()) return;

        DungeonRoom dungeonRoom = actionRoute.getDungeonRoom();
        int current = actionRoute.getCurrent();
        List<AbstractAction> actions = actionRoute.getActions();
        if (current -1 >= 0) {
            AbstractAction abstractAction = actions.get(current - 1);
            if(((abstractAction instanceof AbstractActionMove && ((AbstractActionMove) abstractAction).getTargetVec3().getPos(dungeonRoom).distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) >= 25))) {
                drawActionMove((AbstractActionMove) abstractAction, dungeonRoom, partialTicks);
            }
        }

        AbstractAction currentAction = actionRoute.getCurrentAction();
        renderAction(currentAction, dungeonRoom, partialTicks);
    }

    public void renderAction(AbstractAction currentAction, DungeonRoom dungeonRoom, float partialTicks) {
        if (currentAction instanceof AbstractActionMove) {
            drawActionMove((AbstractActionMove) currentAction, dungeonRoom, partialTicks);
        } else if (currentAction instanceof ActionClick) {
            renderActionClick((ActionClick) currentAction, dungeonRoom, partialTicks);
        } else if (currentAction instanceof ActionClickSet) {
            renderActionClickSet((ActionClickSet) currentAction, dungeonRoom, partialTicks);
        } else if (currentAction instanceof ActionStonkClick) {
            renderActionStonkClick((ActionStonkClick) currentAction, dungeonRoom, partialTicks);
        } else if (currentAction instanceof ActionKill) {
            renderActionKill((ActionKill) currentAction, dungeonRoom, partialTicks);
        } else if (currentAction instanceof ActionInteract) {
            renderActionInteract((ActionInteract) currentAction, dungeonRoom ,partialTicks);
        } else if (currentAction instanceof ActionDropItem) {
            renderActionDropItem((ActionDropItem) currentAction, dungeonRoom, partialTicks);
        } else if (currentAction instanceof ActionBreakWithSuperBoom) {
            renderActionBreakWithSuperboom((ActionBreakWithSuperBoom) currentAction, dungeonRoom, partialTicks);
        } else if (currentAction instanceof AtomicAction) {
            int atomicActionCurrent = ((AtomicAction) currentAction).getCurrent();
            List<AbstractAction> atomicActionActions = ((AtomicAction) currentAction).getActions();
            if (atomicActionCurrent -1 >= 0) {
                AbstractAction abstractAction = atomicActionActions.get(atomicActionCurrent - 1);
                if(((abstractAction instanceof AbstractActionMove && ((AbstractActionMove) abstractAction).getTargetVec3().getPos(dungeonRoom).distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) >= 25))) {
                    drawActionMove((AbstractActionMove) abstractAction, dungeonRoom, partialTicks);
                }
            }
            renderAction(((AtomicAction) currentAction).getCurrentAction(), dungeonRoom, partialTicks);
        }
    }


    public void renderActionKill(ActionKill actionKill, DungeonRoom dungeonRoom, float partialTicks) {
        VectorI3D pos = actionKill.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Spawn", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionInteract(ActionInteract actionInteract, DungeonRoom dungeonRoom, float partialTicks) {
        VectorI3D pos = actionInteract.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Interact", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionDropItem(ActionDropItem dropItem, DungeonRoom dungeonRoom, float partialTicks) {
        VectorI3D pos = dropItem.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, true);
        RenderUtils.drawTextAtWorld("Drop Item", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionClickSet(ActionClickSet actionClickSet, DungeonRoom dungeonRoom, float partialTicks) {
        float xAcc = 0;
        float yAcc = 0;
        float zAcc = 0;
        int size = actionClickSet.getTarget().getOffsetPointList().size();
        List<VectorI3D> list = new ArrayList<>();
        for (OffsetPoint offsetPoint : actionClickSet.getTarget().getOffsetPointList()) {
            VectorI3D pos = offsetPoint.getBlockPos(dungeonRoom);
            xAcc += pos.getX() + 0.5f;
            yAcc += pos.getY()+ 0.5f;
            zAcc += pos.getZ()+ 0.5f;
            list.add(pos);
        }
        RenderUtils.highlightBlocksStencil(list, partialTicks, new AColor(0, 255,255,50), true);

        RenderUtils.drawTextAtWorld("Click", xAcc / size, yAcc / size, zAcc / size, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionClick(ActionClick actionClick, DungeonRoom dungeonRoom, float partialTicks) {
        VectorI3D pos = actionClick.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlockStencil(pos, partialTicks,new AColor(0, 255,0,100), false);
        RenderUtils.drawTextAtWorld("Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);

        Vector3D from = new Vector3D(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
        Vector3D eyePos = ModAPI.getAPI().getPlayer().getPositionEyes(partialTicks);
        Vector3D lookVec = from.subtract(eyePos).normalize();

        if (ModAPI.getAPI().getPlayer().getLook(partialTicks).dotProduct(lookVec) < 0.7) return;

        RaycastResult result = ModAPI.getAPI().getObjectMouseOver();
        if (result.getType() == RaycastResult.HitType.BLOCK) {
            VectorI3D blockPos = result.getBlockHit();

            for (Map.Entry<String, DungeonMechanicState> stringDungeonMechanicStateEntry : dungeonRoom.getMechanics().entrySet()) {
                if (stringDungeonMechanicStateEntry.getValue() instanceof WorldMutatingMechanicState && ((WorldMutatingMechanicState) stringDungeonMechanicStateEntry.getValue()).isBlocking(dungeonRoom)) {
                    List<OffsetPoint> offsetPointList = ((WorldMutatingMechanicState)stringDungeonMechanicStateEntry.getValue()).blockedPoints();
                    List<VectorI3D> blocks = offsetPointList.stream().map(a -> a.getBlockPos(dungeonRoom)).collect(Collectors.toList());
                    for (VectorI3D block : blocks) {
                        if (blockPos.equals(block)) {
                            highlightSuperboom(blocks, partialTicks, new AColor(255, 0, 0, 50));
                            break;
                        }
                    }
                }
            }
        }

    }

    public void renderActionStonkClick(ActionStonkClick actionStonkClick, DungeonRoom dungeonRoom, float partialTicks) {
        VectorI3D pos = actionStonkClick.getTarget().getBlockPos(dungeonRoom);
        RenderUtils.highlightBlockStencil(pos, partialTicks,new AColor(0, 255,0,100), false);
        RenderUtils.drawTextAtWorld("Stonk&Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionBreakWithSuperboom(ActionBreakWithSuperBoom superBoom, DungeonRoom dungeonRoom, float partialTicks) {
        List<VectorI3D> pos = new ArrayList<>();
        for (OffsetPoint offsetPoint : superBoom.getTarget().getOffsetPointList()) {
            pos.add(offsetPoint.getBlockPos(dungeonRoom));
        }

        highlightSuperboom(pos, partialTicks, new AColor(255,0,0,50));
    }

    public void drawActionMove(AbstractActionMove actionMove, DungeonRoom dungeonRoom, float partialTicks) {
        ActionMoveContext context = executorWeakHashMap.get(actionMove);

        VectorI3D target = actionMove.getBeaconTargetPos(dungeonRoom);
        PathfindResult poses = context == null ? null : context.poses;
        boolean flag2 =  FeatureRegistry.SECRET_FREEZE_LINES.isEnabled();



        float distance = MathHelper.sqrt_double(target.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()));
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
            RenderUtils.drawTextAtWorld(String.format("%.2f", MathHelper.sqrt_double(target.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()))) + "m", target.getX() + 0.5f, target.getY() + 0.5f - scale, target.getZ() + 0.5f, 0xFFFFFF00, (float) settings.getDestinationSize(), true, false, partialTicks);
        }

        if (!FeatureRegistry.SECRET_TOGGLE_KEY.isEnabled() || !FeatureRegistry.SECRET_TOGGLE_KEY.togglePathfindStatus) {
            if (poses != null){
                drawLinesPathfindNode(context.segment, settings.getBackground(), settings.getArrow(), partialTicks);

                Vector3D pos = ModAPI.getAPI().getPlayer().getPositionVector();
                if (settings.isEnableEtherwarpTracer()) {
                    for (PathSegment segment : context.segment) {
                        if (segment.nodeType == PathfindResult.PathfindNode.NodeType.ETHERWARP && segment.from.distanceSq(pos) < settings.getEtherwarpTracerDist()) {
                            RenderUtils.drawLinesVec3(Arrays.asList(
                                    ModAPI.getAPI().getPlayer().getLook(partialTicks),
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
                            pose.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 100) {
                        RenderUtils.drawTextAtWorld(pose.getType().toString(), pose.getX(), pose.getY() + 0.5f, pose.getZ(), 0xFF00FF00, 0.02f, false, true, partialTicks);
                    }
                    if (last != null && last.getType() == PathfindResult.PathfindNode.NodeType.SUPERBOOM) {
                        VectorI3D test = new VectorI3D(pose.getX(), pose.getY(), pose.getZ());
                        for (Map.Entry<String, DungeonMechanicState> stringDungeonMechanicStateEntry : dungeonRoom.getMechanics().entrySet()) {
                            if (stringDungeonMechanicStateEntry.getValue() instanceof WorldMutatingMechanicState && ((WorldMutatingMechanicState) stringDungeonMechanicStateEntry.getValue()).isBlocking(dungeonRoom)) {
                                List<OffsetPoint> offsetPointList = ((WorldMutatingMechanicState)stringDungeonMechanicStateEntry.getValue()).blockedPoints();
                                List<VectorI3D> blocks = offsetPointList.stream().map(a -> a.getBlockPos(dungeonRoom)).collect(Collectors.toList());
                                for (VectorI3D block : blocks) {
                                    if (test.distanceSq(block) <= 2) {
                                        highlightSuperboom(blocks, partialTicks, new AColor(255, 0, 0, 50));
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
            LineRenderUtils.renderDebug((ActionMoveSpot) actionMove, dungeonRoom, partialTicks);
        else if (actionMove instanceof ActionMove)
            LineRenderUtils.renderDebug((ActionMove) actionMove, dungeonRoom, partialTicks);
    }

    @AllArgsConstructor @Data
    public static class PathSegment {
        Vector3D from, to;
        Vector3D dir, side;
        Vector3D BL, BR, TL, TR;
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
        Vector3D up = new Vector3D(0, 1, 0);
        for (PathfindResult.PathfindNode pose : poses) {
            if (last != null) {
                Vector3D from = new Vector3D(last.getX(), last.getY(), last.getZ());
                Vector3D to = new Vector3D(pose.getX(), pose.getY(), pose.getZ());
                Vector3D dir = to.subtract(from).normalize();

                Vector3D normal = dir.crossProduct(up).normalize();

                segments.add(new PathSegment(
                        from.add(0, 0.05, 0), to.add(0, 0.05, 0), dir, normal, null,null,null,null, last.getType(),0 ,0
                ));
            }
            last = pose;
        }

        List<PathSegment> result = new ArrayList<>();
        for (int i = 1; i < segments.size(); i++) {
            Vector3D dir1 = segments.get(i-1).dir;
            Vector3D dir2 = segments.get(i).dir;
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

                Vector3D scaledVecTo = new Vector3D(to.dir.x * radTo, to.dir.y * radTo, to.dir.z * radTo);
                Vector3D scaledVecFrom = new Vector3D(prev.dir.x * radFrom, prev.dir.y*radFrom, prev.dir.z * radFrom);
                Vector3D P2 = to.from;
                to.from = to.from.add(scaledVecTo);
                prev.to = prev.to.subtract(scaledVecFrom);
                Vector3D P1 = prev.to, P3 = to.from;

                PathSegment realPrev = prev;
                for (double t = 0.1; t < 1.0; t += 0.1) {
                    double x = P1.x * (1-t)*(1-t) + P2.x * 2*(1-t) * t + P3.x * t * t;
                    double y = P1.y * (1-t)*(1-t) + P2.y * 2*(1-t) * t + P3.y * t * t;
                    double z = P1.z * (1-t)*(1-t) + P2.z * 2*(1-t) * t + P3.z * t * t;
                    Vector3D pt = new Vector3D(x, y, z);

                    double xTangent = P1.x * 2 * (t-1) + P2.x * (2 - 4*t) + P3.x * (2*t);
                    double yTangent = P1.y * 2 * (t-1) + P2.y * (2 - 4*t) + P3.y * (2*t);
                    double zTangent = P1.z * 2 * (t-1) + P2.z * (2 - 4*t) + P3.z * (2*t);
                    Vector3D dir = new Vector3D(xTangent, yTangent, zTangent);

                    realResult.add(realPrev = new PathSegment(
                            realPrev.to, pt, dir.normalize(), dir.crossProduct(up).normalize(), null, null, null, null, realPrev.nodeType, 0, 0
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
                segment.side = new Vector3D(1, 0, 0);
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
                Vector3D prevSideScaled = new Vector3D(prev.side.x * thickness/2, prev.side.y * thickness/2, prev.side.z * thickness/2);

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


    private static void highlightSuperboom(List<VectorI3D> blockPos, float partialTicks, AColor color) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        AABB bb = RenderUtils.highlightBlocksStencil(blockPos, partialTicks, color, true);

        GlStateManager.enableDepth();
        RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.disableCull();

        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("minecraft:textures/blocks/tnt_side.png"));
        GlStateManager.color(1, 1, 1, 1.0F);
        Tessellator tesselator = Tessellator.getInstance();
        WorldRenderer vertexBuffer = tesselator.getWorldRenderer();
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        int[][][] quads = {
                {{0,0,-1}, {1,0,-1}, {1,1,-1}, {0,1,-1}},
                {{0,0,2}, {1,0,2}, {1,1,2}, {0,1,2}},
                {{-1,0,0}, {-1,0,1}, {-1,1,1}, {-1,1,0}},
                {{2,0,0}, {2,0,1}, {2,1,1}, {2,1,0}},
                {{0,-1,0}, {1,-1,0}, {1,-1,1}, {0,-1,1}},
                {{0,2,0}, {1,2,0}, {1,2,1}, {0,2,1}}
        };
        int[][] quads2 = {
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
            for (int j = 0; j < 4; j++) {
                vertexBuffer.pos(faces[0][quads[i][j][0]+1],
                                faces[1][quads[i][j][1]+1],
                                faces[2][quads[i][j][2]+1])
                        .tex(quads2[j][0], quads2[j][1]).endVertex();
            }
        }

        tesselator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();

    }


    public static final ResourceLocation arrow = new ResourceLocation("dungeonsguide:textures/arrow.png");
    public static final ResourceLocation abilities = new ResourceLocation("dungeonsguide:textures/features/precalclist/abilities.png");
    public static void drawIcon(int iconIdx, PathSegment segment, AColor colour, AColor texture, float partialTicks, double animate, boolean path) {
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1,1,1,1);
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

        boolean flag = renderManager.getDistanceToCamera(segment.from.x, segment.from.y, segment.from.z) > 0.25;
        Vector3D fixedFrom = flag ? segment.from.add(0, 1.5, 0) : segment.from;
        Vector3D dir = segment.to.subtract(fixedFrom);
        if (!flag) {
            GlStateManager.disableDepth();
        }
        if (path){
            Vector3D normal;
            if (flag) {
                normal = dir.crossProduct(ModAPI.getAPI().getPlayer().getLook(partialTicks)).normalize();
                if (normal.length() == 0) normal = new Vector3D(1, 0, 0);
                normal = new Vector3D(normal.x * 0.05, normal.y * 0.05, normal.z * 0.05);
            } else {
                normal = new Vector3D(segment.side.x * 0.05, segment.side.y * 0.05, segment.side.z * 0.05);
            }

            GlStateManager.disableTexture2D();
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            double len = segment.to.subtract(segment.from).length();
            int i = RenderUtils.getColorAt(0, 0, 0, colour);
            float r = ((i >> 16) & 0xFF) / 255.0f, g = ((i >> 8) & 0xFF) / 255.0f, b = (i & 0xFF) / 255.0f, a = ((i >> 24) & 0xFF) / 255.0f;
            Vector3D quadBL = fixedFrom.add(normal), quadBR = fixedFrom.subtract(normal), quadTL = segment.to.add(normal), quadTR = segment.to.subtract(normal);
            {
                worldRenderer.pos(quadBL.x, quadBL.y, quadBL.z).color(r, g, b, a).endVertex();
                worldRenderer.pos(quadBR.x, quadBR.y, quadBR.z).color(r, g, b, a).endVertex();
                worldRenderer.pos(quadTR.x, quadTR.y, quadTR.z).color(r, g, b, a).endVertex();
                worldRenderer.pos(quadTL.x, quadTL.y, quadTL.z).color(r, g, b, a).endVertex();
            }
            Tessellator.getInstance().draw();

            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            GlStateManager.enableTexture2D();
            textureManager.bindTexture(TextureMap.locationBlocksTexture);

            ShaderProgram shaderProgram = ShaderManager.getShader("shaders/repeat");
            shaderProgram.useShader();
            TextureAtlasSprite sprite = FeatureRegistry.SECRET_ROUTE_REGISTRY.sprite;
            shaderProgram.uploadUniform("position", sprite.getMinU(), sprite.getMinV());
            shaderProgram.uploadUniform("size", sprite.getMaxU() - sprite.getMinU(), sprite.getMaxV() - sprite.getMinV());

            i = RenderUtils.getColorAt(0,0,0, texture);
            r= ((i >> 16) &0xFF)/255.0f; g=((i >> 8) &0xFF)/255.0f; b=(i &0xFF)/255.0f; a=((i >> 24) &0xFF)/255.0f;
            GlStateManager.color(r, g, b, a);

            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            {
                worldRenderer.pos(quadBL.x, quadBL.y, quadBL.z).tex(0, -animate).endVertex();
                worldRenderer.pos(quadBR.x, quadBR.y, quadBR.z).tex(1, -animate).endVertex();
                worldRenderer.pos(quadTR.x, quadTR.y, quadTR.z).tex(1, -len / 0.1-animate).endVertex();
                worldRenderer.pos(quadTL.x, quadTL.y, quadTL.z).tex(0, -len / 0.1-animate).endVertex();
            }
            Tessellator.getInstance().draw();

            GL20.glUseProgram(0);
        }
        if (!flag) {
            GlStateManager.enableDepth();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(segment.from.x, segment.from.y+0.5, segment.from.z);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(1/32.0, -1/32.0, 1/32.0);

        GlStateManager.enableDepth();
        GlStateManager.disableTexture2D();
        int size = 16;
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(-size /2 - 1, -size /2-1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        worldRenderer.pos(-size /2 - 1, size /2+1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        worldRenderer.pos(size /2 + 1, size /2+1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        worldRenderer.pos(size /2 + 1, -size /2-1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(abilities);

        GuiScreen.drawScaledCustomSizeModalRect(
                -size /2, -size /2, (iconIdx % 8)*32 + 0.5f , (iconIdx/8)*32 + 0.5f ,  31, 31, size, size, 512, 512
        );

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();

        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0f, -1.0f);
        VectorI3D pos = new VectorI3D(Math.floor(segment.to.x), Math.floor(segment.to.y) -1 , Math.floor(segment.to.z));
        RenderUtils._highlightBlock(pos, Color.green, partialTicks, true);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();

        GlStateManager.popMatrix();

//        }
    }

    public static void drawLinesNormal(List<PathSegment> poses, int from, int to, PathfindResult.PathfindNode.NodeType type, AColor colour, AColor texture, float partialTicks, double animate) {
        if (type == PathfindResult.PathfindNode.NodeType.STONK_WALK || type == PathfindResult.PathfindNode.NodeType.STONK_EXIT) {
            GlStateManager.disableDepth();
        } else {
            GlStateManager.enableDepth();
        }

        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1,1,1,1);

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int i = RenderUtils.getColorAt(0,0,0, colour);
        float r= ((i >> 16) &0xFF)/255.0f, g=((i >> 8) &0xFF)/255.0f, b=(i &0xFF)/255.0f, a=((i >> 24) &0xFF)/255.0f;
        for (int j = from; j < to; j++) {
            PathSegment pos = poses.get(j);
            Vector3D quadBL = pos.BL, quadBR = pos.BR, quadTL = pos.TL, quadTR = pos.TR;
            worldRenderer.pos(quadBL.x, quadBL.y, quadBL.z).color(r,g,b,a).endVertex();
            worldRenderer.pos(quadBR.x, quadBR.y, quadBR.z).color(r,g,b,a).endVertex();
            worldRenderer.pos(quadTR.x, quadTR.y, quadTR.z).color(r,g,b,a).endVertex();
            worldRenderer.pos(quadTL.x, quadTL.y, quadTL.z).color(r,g,b,a).endVertex();
        }
        Tessellator.getInstance().draw();

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        GlStateManager.enableTexture2D();
        textureManager.bindTexture(TextureMap.locationBlocksTexture);

        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/repeat");
        shaderProgram.useShader();
        TextureAtlasSprite sprite = FeatureRegistry.SECRET_ROUTE_REGISTRY.sprite;
        shaderProgram.uploadUniform("position", sprite.getMinU(), sprite.getMinV());
        shaderProgram.uploadUniform("size", sprite.getMaxU() - sprite.getMinU(), sprite.getMaxV() - sprite.getMinV());


        i = RenderUtils.getColorAt(0,0,0, texture);
        r= ((i >> 16) &0xFF)/255.0f; g=((i >> 8) &0xFF)/255.0f; b=(i &0xFF)/255.0f; a=((i >> 24) &0xFF)/255.0f;
        GlStateManager.color(r, g, b, a);

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (int j = from; j < to; j++) {
            PathSegment pos = poses.get(j);
            Vector3D quadBL = pos.BL, quadBR = pos.BR, quadTL = pos.TL, quadTR = pos.TR;
            worldRenderer.pos(quadBL.x, quadBL.y, quadBL.z).tex(0, -pos.texCulLenFrom-animate).endVertex();
            worldRenderer.pos(quadBR.x, quadBR.y, quadBR.z).tex(1, -pos.texCulLenFrom-animate).endVertex();
            worldRenderer.pos(quadTR.x, quadTR.y, quadTR.z).tex(1, -pos.texCulLenTo-animate).endVertex();
            worldRenderer.pos(quadTL.x, quadTL.y, quadTL.z).tex(0,-pos.texCulLenTo-animate).endVertex();
        }
        Tessellator.getInstance().draw();

        GL20.glUseProgram(0);
    }
    public void drawLinesPathfindNode(List<PathSegment> poses, AColor colour, AColor texture, float partialTicks) {
        if (poses.size() == 0) return;
        double speed = settings.getAnimationSpeed();
        double animate = System.currentTimeMillis() / 1000.0 * speed;
        animate = animate - Math.floor(animate);
        animate *= -1;


        RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();

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
                    if (ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(poses.get(st).from) < settings.getEtherwarpTracerDist()) path = false;
                }
                drawIcon(40, poses.get(st), colour, texture, partialTicks, animate, path);
            } else if (nodeType == PathfindResult.PathfindNode.NodeType.ENDERPEARL)
                drawIcon(41, poses.get(st), colour, texture, partialTicks, animate, true);
            else
                drawLinesNormal(poses, st, toIdx, poses.get(st).nodeType, colour, texture, partialTicks, animate);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GL11.glLineWidth(1);
    }
}
