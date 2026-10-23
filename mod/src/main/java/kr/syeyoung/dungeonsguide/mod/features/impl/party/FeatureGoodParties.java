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

package kr.syeyoung.dungeonsguide.mod.features.impl.party;

import net.minecraft.core.component.DataComponents;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;


import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui.PartyFinderParty;
import net.minecraft.client.Minecraft;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.nbt.CompoundTag;

public class FeatureGoodParties extends SimpleFeature {
    public FeatureGoodParties() {
        super("Party Kicker", "Highlight parties in party viewer", "Highlight parties you can't join with red", "partykicker.goodparty",true);
    }

    @DGEventHandler
    public void onGuiPostRender(ScreenRenderEvent.Post rendered) {
        
        if (!(Minecraft.getInstance().screen instanceof ContainerScreen)) return;
        ContainerScreen chest = (ContainerScreen) Minecraft.getInstance().screen;
        ChestMenu cont = (ChestMenu) chest.getMenu();
        String name = chest.getTitle().getString();
        if (!"Party Finder".equals(name)) return;


        int i = 222;
        int j = i - 108;
        int ySize = j + (((ChestMenu)(((ContainerScreen) Minecraft.getInstance().screen).getMenu())).getContainer().getContainerSize() / 9) * 18;
        int left = (rendered.gui.width - 176) / 2;
        int top = (rendered.gui.height - ySize ) / 2;
        // D126: drawn through the event's RenderingContext
        RenderingContext context = rendered.context;
        context.pose().pushMatrix();
        context.pose().translate(left, top);
        Font fr = Minecraft.getInstance().font;
        try {

            for (int i1 = 0; i1 < Integer.min(54, cont.slots.size()); i1++) {
                Slot s = cont.slots.get(i1);
                if (s.getItem().isEmpty()) continue;
                if (!s.getItem().is(Items.PLAYER_HEAD)) continue;
                // 1.8.9 skipped heads without nbt and stopped at one without a display compound:
                // the custom name / lore components on 26.1 (D125)
                if (s.getItem().getComponentsPatch().isEmpty()) continue;
                if (!s.getItem().has(DataComponents.CUSTOM_NAME) && !s.getItem().has(DataComponents.LORE)) return;

                PartyFinderParty party = PartyFinderParty.fromItemStack(s.getItem());

                int x = s.x;
                int y = s.y;
                if (!party.canJoin) {
                    context.drawRect(x, y, x + 16, y + 16, 0x77AA0000);
                } else {


                    if (party.note.toLowerCase().contains("car")) {
                        context.drawString("C", x + 1, y + 1, 0xFFFF0000, true);
                    } else if (party.note.toLowerCase().replace(" ", "").contains("s/s+")) {
                        context.drawString("S+", x + 1, y + 1, 0xFFFFFF00, true);
                    } else if (party.note.toLowerCase().contains("s+")) {
                        context.drawString("S+", x + 1, y + 1, 0xFF00FF00, true);
                    } else if (party.note.toLowerCase().contains(" s") || party.note.toLowerCase().contains(" s ")) {
                        context.drawString("S", x + 1, y + 1, 0xFFFFFF00, true);
                    } else if (party.note.toLowerCase().contains("rush")) {
                        context.drawString("R", x + 1, y + 1, 0xFFFF0000, true);
                    }
                    context.drawString("§e"+Integer.max(party.requiredClassLevel, party.requiredDungeonLevel), x + 1, y + fr.lineHeight, 0xFFFFFFFF, true);
                }


            }
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        context.pose().popMatrix();
    }
}
