package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMove;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.RaytraceHelper;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LineRenderUtils {

    public static void renderDebug(ActionMoveSpot actionMoveSpot, DungeonRoom dungeonRoom, UWorldRenderContext context,  float partialTicks) {
        List<PossibleMoveSpot> targets = actionMoveSpot.getTargets();
        if (FeatureRegistry.DEBUG_ST.isEnabled()) {
            int i = 0;
            for (PossibleMoveSpot spot : targets) {
//                GlStateManager.disableAlpha();
                i++;
                Color c = Color.getHSBColor(
                        1.0f * i / targets.size(), 0.5f, 1.0f
                );
                Color actual;


//                GlStateManager.disableAlpha();
                if (!spot.isBlocked()) {
                    actual = new Color(c.getRGB() & 0xFFFFFF | 0x90000000, true);
                    PossibleMoveSpot spot2 = RaytraceHelper.chooseMinimalY2(Arrays.asList(spot)).get(0);
                    for (OffsetVec3 _vec3 : spot2.getOffsetPointSet()) {
                        Vector3D offsetVec3 = _vec3.getPos(dungeonRoom);
                        context.highlightBox(
                                new AABB(
                                        offsetVec3.x - 0.25f, offsetVec3.y + 0.025f, offsetVec3.z - 0.25f,
                                        offsetVec3.x + 0.25f, offsetVec3.y + 0.026f, offsetVec3.z + 0.25f
                                ).expand(0.0030000000949949026, 0.0030000000949949026, 0.0030000000949949026),
                                actual.getRGB(),
                                partialTicks,
                                true
                        );
                    }
                }
                actual = new Color(c.getRGB() & 0xFFFFFF | 0x10000000, true);
                for (OffsetVec3 _vec3 : spot.getOffsetPointSet()) {
                    Vector3D offsetVec3 = _vec3.getPos(dungeonRoom);
                    context.highlightBox(
                            new AABB(
                                    offsetVec3.x - 0.25f, offsetVec3.y - 0.025f, offsetVec3.z - 0.25f,
                                    offsetVec3.x + 0.25f, offsetVec3.y + 0.475f, offsetVec3.z + 0.25f
                            ).expand(0.0030000000949949026, 0.0030000000949949026, 0.0030000000949949026),
                            actual.getRGB(),
                            partialTicks,
                            true
                    );
                }
                double cx = 0, cy = 0, cz = 0;
                for (OffsetVec3 _offsetVec3 : spot.getOffsetPointSet()) {
                    Vector3D offsetVec3 = _offsetVec3.getPos(dungeonRoom);
                    cx += offsetVec3.x;
                    cy += offsetVec3.y;
                    cz += offsetVec3.z;
                }
                cx /= spot.getOffsetPointSet().size();
                cy /= spot.getOffsetPointSet().size();
                cz /= spot.getOffsetPointSet().size();
                cy += 0.2f;
                context.drawTextAtWorld(
                        spot.getClusterId() + "/" + spot.isBlocked() + " / " + spot.getOffsetPointSet().size(), (float) cx, (float) cy, (float) cz, actual.getRGB() | 0xFF000000, 0.01f, false, true, partialTicks);


//                GlStateManager.enableAlpha();
            }
        }
    }

    public static void renderDebug(ActionMove actionMove, DungeonRoom dungeonRoom, UWorldRenderContext context, float partialTicks) {
        List<PossibleClickingSpot> targets = actionMove.getTargets();
        if (FeatureRegistry.DEBUG_ST.isEnabled()) {

            int i = 0;
            for (PossibleClickingSpot spot : RaytraceHelper.chooseMinimalY(targets)) {
//                GlStateManager.disableAlpha();
                i++;
                Color c = Color.getHSBColor(
                        1.0f * i / targets.size(), 0.5f, 1.0f
                );
                Color actual = new Color(c.getRGB() & 0xFFFFFF | 0x90000000, true);
                for (OffsetVec3 _vec3 : spot.getOffsetPointSet()) {
                    Vector3D offsetVec3 = _vec3.getPos(dungeonRoom);
                    context.highlightBox(
                            new AABB(
                                    offsetVec3.x - 0.25f, offsetVec3.y + 0.025f, offsetVec3.z - 0.25f,
                                    offsetVec3.x + 0.25f, offsetVec3.y + 0.026f, offsetVec3.z + 0.25f
                            ).expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026),
                            actual.getRGB(),
                            partialTicks,
                            true
                    );
                }
            }

            i = 0;
            for (PossibleClickingSpot spot : targets) {
//                GlStateManager.disableAlpha();
                i++;
                Color c = Color.getHSBColor(
                        1.0f * i / targets.size(), 0.5f, 1.0f
                );
                Color actual = new Color(c.getRGB() & 0xFFFFFF | 0x10000000, true);
                for (OffsetVec3 _vec3 : spot.getOffsetPointSet()) {
                    Vector3D offsetVec3 = _vec3.getPos(dungeonRoom);
                    context.highlightBox(
                            new AABB(
                                    offsetVec3.x - 0.25f, offsetVec3.y - 0.025f, offsetVec3.z - 0.25f,
                                    offsetVec3.x + 0.25f, offsetVec3.y + 0.475f, offsetVec3.z + 0.25f
                            ).expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026),
                            actual.getRGB(),
                            partialTicks,
                            true
                    );
                }

                double cx = 0, cy = 0, cz = 0;
                for (OffsetVec3 _offsetVec3 : spot.getOffsetPointSet()) {
                    Vector3D offsetVec3 = _offsetVec3.getPos(dungeonRoom);
                    cx += offsetVec3.x;
                    cy += offsetVec3.y;
                    cz += offsetVec3.z;
                }
                cx /= spot.getOffsetPointSet().size();
                cy /= spot.getOffsetPointSet().size();
                cz /= spot.getOffsetPointSet().size();
                cy += 0.2f;
                context.drawTextAtWorld(
                        Arrays.stream(spot.getTools())
                                .map(a -> a == null ? "null" : a.getBreakingPower() + ":" + a.getHarvestLv()).collect(Collectors.joining(";"))
                                + ":::" + spot.getClusterId() + "/" + spot.isStonkingReq(), (float) cx, (float) cy, (float) cz, actual.getRGB(), 0.01f, false, true, partialTicks);


            }
        }
    }


}
