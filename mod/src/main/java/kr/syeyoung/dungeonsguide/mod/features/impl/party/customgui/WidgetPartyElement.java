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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class WidgetPartyElement extends AnnotatedImportOnlyWidget {
    private final int slot;
    private WidgetPartyFinder widgetPartyFinder;

    @Bind(variableName = "item")
    public final BindableAttribute<ItemStack> itemstack = new BindableAttribute<>(ItemStack.class);

    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "note")
    public final BindableAttribute<String> note = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "sidenote")
    public final BindableAttribute<String> sidenote = new BindableAttribute<>(String.class, "");

    private PartyFinderParty party;

    public WidgetPartyElement(WidgetPartyFinder widgetPartyFinder, int slot) {
        super(new ResourceLocation("dungeonsguide:gui/features/partyFinder/party_element.gui"));
        this.slot = slot;
        this.widgetPartyFinder = widgetPartyFinder;
    }

    public void update(PartyFinderParty party) {

        itemstack.setValue(party.itemStack);
            String note = party.note;
            boolean notFound = false;
            boolean cantJoin = !party.canJoin;
            if (itemstack.getValue().getItem() == Item.getItemFromBlock(Blocks.bedrock)) {
                cantJoin = true;
                notFound = true;
            }
            int minClass = party.requiredClassLevel, minDungeon = party.requiredDungeonLevel;
            boolean nodupe = note.toLowerCase().contains("nodupe") || note.toLowerCase().contains("no dupe") || (note.toLowerCase().contains("nd") && (note.toLowerCase().indexOf("nd") == 0 || note.charAt(note.toLowerCase().indexOf("nd")-1) == ' '));

            note = note.replaceAll("(?i)(S\\+)", "§6$1§r");
            note = note.replaceAll("(?i)(carry)", "§4$1§r");

            try {
                if (!widgetPartyFinder.highlight.getValue().isEmpty()) {
                    for (String s1 : widgetPartyFinder.highlight.getValue().split(",")) {
                        note = note.replaceAll("(?i)(" + s1 + ")", "§e§l$1§r");
                    }
                }
            } catch (Exception e) {}

            if (nodupe && party.members.stream().anyMatch(a -> a.getClazz().equals(FeatureRegistry.PARTYKICKER_CUSTOM.getLastClass()))) {
                note = note.replace("nodupe", "§cnodupe§r").replace("no dupe", "§cno dupe§r").replace("nd", "§cnd§r");
            }

            this.name.setValue(party.leader);

            if (!notFound)
                note = "§7("+party.members.size()+") §f"+note;

            String sideNote = "";
            if (minClass > 0) sideNote += "§7CLv ≥§b"+minClass+" ";
            if (minDungeon > 0) sideNote += "§7DLv ≥§b"+minDungeon+" ";
            if (cantJoin && !notFound) sideNote = "§cCan't join";
            sideNote = sideNote.trim();

        if (notFound) note = "§cList is empty";

        this.sidenote.setValue(sideNote);
        this.note.setValue(note);
    }
}
