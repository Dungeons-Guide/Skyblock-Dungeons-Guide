package kr.syeyoung.modapi.v1_8_9.mod.classic;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.LineRenderUtils;
import kr.syeyoung.dungeonsguide.mod.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

            ctx.tick = (ctx.tick+1) % Math.max(1, classicPathEngineLineProperties.getLineRefreshRate());
            if (ctx.executor == null && classicPathEngineLineProperties.isPathfind()) {
                forceRefresh(actionMove);
            }
            if (ctx.executor != null && (ctx.poses == null || !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled())) {
                ctx.poses = ctx.executor.getRoute(ModAPI.getAPI().getPlayer().getPositionVector());
            }

            if (ctx.tick == 0 && classicPathEngineLineProperties.isPathfind() && ctx.executor != null) {
                if (classicPathEngineLineProperties.getLineRefreshRate() != -1 && !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() && ctx.executor.isComplete()) {
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

    @Override
    public ClassicPathEngineLineProperties getSettings() {
        return classicPathEngineLineProperties;
    }

    @Override
    public void setSettings(ClassicPathEngineLineProperties settings) {
        this.classicPathEngineLineProperties = settings;
    }

    @Override
    public void renderActionRoute(UWorldRenderContext context, float partialTicks) {
        if (actionRoute.isCalculating()) return;

        DungeonRoom dungeonRoom = actionRoute.getDungeonRoom();
        int current = actionRoute.getCurrent();
        List<AbstractAction> actions = actionRoute.getActions();
        if (current -1 >= 0) {
            AbstractAction abstractAction = actions.get(current - 1);
            if(((abstractAction instanceof AbstractActionMove && ((AbstractActionMove) abstractAction).getTargetVec3().getPos(dungeonRoom)
                    .distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) >= 25))) {
                drawActionMove((AbstractActionMove) abstractAction, dungeonRoom, context, partialTicks);
            }
        }

        AbstractAction currentAction = actionRoute.getCurrentAction();
        renderAction(currentAction, dungeonRoom, context, partialTicks);
    }

    public void renderAction(AbstractAction currentAction, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        if (currentAction instanceof AbstractActionMove) {
            drawActionMove((AbstractActionMove) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof ActionClick) {
            renderActionClick((ActionClick) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof ActionClickSet) {
            renderActionClickSet((ActionClickSet) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof ActionStonkClick) {
            renderActionStonkClick((ActionStonkClick) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof ActionKill) {
            renderActionKill((ActionKill) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof ActionInteract) {
            renderActionInteract((ActionInteract) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof ActionDropItem) {
            renderActionDropItem((ActionDropItem) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof ActionBreakWithSuperBoom) {
            renderActionBreakWithSuperboom((ActionBreakWithSuperBoom) currentAction, dungeonRoom, context, partialTicks);
        } else if (currentAction instanceof AtomicAction) {
            int atomicActionCurrent = ((AtomicAction) currentAction).getCurrent();
            List<AbstractAction> atomicActionActions = ((AtomicAction) currentAction).getActions();
            if (atomicActionCurrent -1 >= 0) {
                AbstractAction abstractAction = atomicActionActions.get(atomicActionCurrent - 1);
                if(((abstractAction instanceof AbstractActionMove && ((AbstractActionMove) abstractAction).getTargetVec3().getPos(dungeonRoom)
                        .distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) >= 25))) {
                    drawActionMove((AbstractActionMove) abstractAction, dungeonRoom, context, partialTicks);
                }
            }
            renderAction(((AtomicAction) currentAction).getCurrentAction(), dungeonRoom, context, partialTicks);
        }
    }


    public void renderActionKill(ActionKill actionKill, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = actionKill.getTarget().getBlockPos(dungeonRoom);
        context.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        context.drawTextAtWorld("Spawn", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionInteract(ActionInteract actionInteract, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = actionInteract.getTarget().getBlockPos(dungeonRoom);
        context.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        context.drawTextAtWorld("Interact", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionDropItem(ActionDropItem dropItem, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = dropItem.getTarget().getBlockPos(dungeonRoom);
        context.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, true);
        context.drawTextAtWorld("Drop Item", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionClickSet(ActionClickSet actionClickSet, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        float xAcc = 0;
        float yAcc = 0;
        float zAcc = 0;
        int size = actionClickSet.getTarget().getOffsetPointList().size();
        for (OffsetPoint offsetPoint : actionClickSet.getTarget().getOffsetPointList()) {
            VectorI3D pos = offsetPoint.getBlockPos(dungeonRoom);
            xAcc += pos.getX() + 0.5f;
            yAcc += pos.getY()+ 0.5f;
            zAcc += pos.getZ()+ 0.5f;
            context.highlightBlock(offsetPoint.getBlockPos(dungeonRoom), new Color(0, 255,255,50),partialTicks, true);
        }

        context.drawTextAtWorld("Click", xAcc / size, yAcc / size, zAcc / size, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionClick(ActionClick actionClick, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = actionClick.getTarget().getBlockPos(dungeonRoom);
        context.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, false);
        context.drawTextAtWorld("Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionStonkClick(ActionStonkClick actionStonkClick, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        VectorI3D pos = actionStonkClick.getTarget().getBlockPos(dungeonRoom);
        context.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, false);
        context.drawTextAtWorld("Stonk&Click", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    public void renderActionBreakWithSuperboom(ActionBreakWithSuperBoom superBoom, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

        VectorI3D blockpos = superBoom.getTarget().getOffsetPointList().get(0).getBlockPos(dungeonRoom);
        RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);

        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
        vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);

        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().theWorld,
                blockrendererdispatcher.getBlockModelShapes().getModelForState(Blocks.tnt.getDefaultState()),
                Blocks.tnt.getDefaultState(), new BlockPos(blockpos.x, blockpos.y, blockpos.z), vertexBuffer, false);
        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        context.highlightBlock(blockpos, new Color(0, 255,255,50), partialTicks, true);
        context.drawTextAtWorld("Superboom", blockpos.getX() + 0.5f, blockpos.getY() + 0.5f, blockpos.getZ() + 0.5f, 0xFFFFFF00, 0.03f, false, false, partialTicks);
    }

    public void drawActionMove(AbstractActionMove actionMove, DungeonRoom dungeonRoom, UWorldRenderContext ctx, float partialTicks) {
        ActionMoveContext context = executorWeakHashMap.get(actionMove);

        VectorI3D target = actionMove.getBeaconTargetPos(dungeonRoom);
        PathfindResult poses = context == null ? null : context.poses;
        boolean flag2 =  FeatureRegistry.SECRET_FREEZE_LINES.isEnabled();



        float distance = (float) Math.sqrt(target.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()));
        float multiplier = distance / 120f; //mobs only render ~120 blocks away
        float scale = 0.45f * multiplier;
        scale *= 25.0 / 6.0;
        if (classicPathEngineLineProperties.isBeacon()) {
            ctx.renderBeaconBeam(target.getX(), target.getY(), target.getZ(), classicPathEngineLineProperties.getBeaconBeamColor().getRGB(), classicPathEngineLineProperties.getBeaconBeamColor().isChroma(), classicPathEngineLineProperties.getBeaconBeamColor().getChromaSpeed(), partialTicks);
            ctx.highlightBlock(target, classicPathEngineLineProperties.getBeaconColor(), partialTicks, false);
        }
        ctx.drawTextAtWorld("Destination", target.getX() + 0.5f, target.getY() + 0.5f + scale, target.getZ() + 0.5f, 0xFF00FF00, 1f, true, false, partialTicks);

        ctx.drawTextAtWorld(String.format("%.2f",Math.sqrt(target.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector())))+"m", target.getX() + 0.5f, target.getY() + 0.5f - scale, target.getZ() + 0.5f, 0xFFFFFF00, 1f, true, false, partialTicks);

        if (!FeatureRegistry.SECRET_TOGGLE_KEY.isEnabled() || !FeatureRegistry.SECRET_TOGGLE_KEY.togglePathfindStatus) {
            if (poses != null){
                drawLinesPathfindNode(poses.getNodeList(), classicPathEngineLineProperties.getLineColor(), (float) classicPathEngineLineProperties.getLineWidth(), partialTicks, false);

                int cnt = 0;
                int warp = 0;
                for (PathfindResult.PathfindNode pose : poses.getNodeList()) {
                    cnt ++;
                    if (pose.getType() != null && pose.getType() != PathfindResult.PathfindNode.NodeType.WALK && pose.getType() != PathfindResult.PathfindNode.NodeType.STONK_WALK && pose.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 100) {
                        ctx.drawTextAtWorld(pose.getType().toString(), pose.getX(), pose.getY() + 0.5f, pose.getZ(), 0xFF00FF00, 0.02f, false, true, partialTicks);
                    }

                    if (warp == 1) {
                        VectorI3D pos = new VectorI3D(Math.floor(pose.getX()), Math.floor(pose.getY()) -1 , Math.floor(pose.getZ()));
                        ctx.highlightBox(
                                new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY() + 1, pos.getZ() + 1)
                                        .expand(0.003, 0.003, 0.003), Color.green.getRGB(), partialTicks, true);
                        warp = flag2 ? 0 : 2;
                    }
                    if (pose.getType() == PathfindResult.PathfindNode.NodeType.ETHERWARP &&
                            ((flag2 && ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(pose.getX(), pose.getY(), pose.getZ()) < 25) || (!flag2 && cnt < 2)) && warp < 2) {
                        warp = 1;
                    }

                }
            }
        }

        if (actionMove instanceof ActionMoveSpot)
            LineRenderUtils.renderDebug((ActionMoveSpot) actionMove, dungeonRoom, ctx, partialTicks);
        else if (actionMove instanceof ActionMove)
            LineRenderUtils.renderDebug((ActionMove) actionMove, dungeonRoom, ctx, partialTicks);
    }



    public static void drawLinesPathfindNode(List<PathfindResult.PathfindNode> poses, AColor colour, float thickness, float partialTicks, boolean nodepth) {
        if (poses.size() == 0) return;
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GL11.glLineWidth(thickness);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if (nodepth) {
            GlStateManager.disableDepth();
        }

        if ((poses.get(0).getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK ) && poses.get(0).distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 100) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }


//        GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue()/ 255f, colour.getAlpha() / 255f);
        GlStateManager.color(1,1,1,1);
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int num = 0;

        PathfindResult.PathfindNode lastNode = null;
        for (PathfindResult.PathfindNode pos:poses) {
            int i = RenderUtils.getColorAt(num++ * 10,0, colour);
            worldRenderer.pos(pos.getX(), pos.getY(), pos.getZ()).color(
                    ((i >> 16) &0xFF)/255.0f,
                    ((i >> 8) &0xFF)/255.0f,
                    (i &0xFF)/255.0f,
                    ((i >> 24) &0xFF)/255.0f
            ).endVertex();

            if (lastNode != null && lastNode.getType() != pos.getType()) {
                Tessellator.getInstance().draw();
                worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);


                if ((pos.getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK || poses.get(0).getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK)&& pos.distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) < 100) {
                    GlStateManager.disableDepth();
                    GlStateManager.depthMask(false);
                } else {
                    GlStateManager.enableDepth();
                    GlStateManager.depthMask(true);
                }

                worldRenderer.pos(pos.getX(), pos.getY(), pos.getZ()).color(
                        ((i >> 16) &0xFF)/255.0f,
                        ((i >> 8) &0xFF)/255.0f,
                        (i &0xFF)/255.0f,
                        ((i >> 24) &0xFF)/255.0f
                ).endVertex();
            }
            lastNode = pos;

        }
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GL11.glLineWidth(1);
    }
}
