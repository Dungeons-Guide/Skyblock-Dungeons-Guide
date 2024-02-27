/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.RaytraceHelper;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.ShadowCast;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FeatureEtherwarpDebug extends SimpleFeature implements ShadowCast.Checker {

    public FeatureEtherwarpDebug() {
        super("Debug", "Etherwarp Debug", "Toggles etherwarp 3d shadow casting debug", "etdebug", false);
    }

    private List<BlockPos> toHighlight;
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.entityPlayer.getHeldItem() == null ||
                event.entityPlayer.getHeldItem().getItem() != Items.spawn_egg) {
            return;
        }
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            return;
        }
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            event.setCanceled(true);

            long start = System.nanoTime();
            toHighlight = ShadowCast.realShadowcast(this, event.pos.getX(), event.pos.getY(), event.pos.getZ(), 61);
            ChatTransmitter.sendDebugChat("Shadowcasting took "+(System.nanoTime() - start)+" ns with "+toHighlight.size());
        } else {
//            toHighlight = null;
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void renderworldLast(RenderWorldLastEvent event) {
        if (toHighlight == null) return;
        GlStateManager.disableAlpha();
        Color c =  new Color(0x1500FF00, true);
        for (BlockPos spot : toHighlight) {
             RenderUtils.highlightBlock(spot, c, event.partialTicks, false);
        }
        GlStateManager.enableAlpha();
    }

    @Override
    public boolean checkIfBlocked(int x, int y, int z) {
        return !Minecraft.getMinecraft().theWorld.isAirBlock(new BlockPos(x,y,z));
    }
}
