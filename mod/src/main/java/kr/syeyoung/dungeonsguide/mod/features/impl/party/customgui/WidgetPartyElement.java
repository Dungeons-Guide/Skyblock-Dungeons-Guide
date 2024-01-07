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
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MouseTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.List;

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

    @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Integer> backgroundColor = new BindableAttribute<>(Integer.class, 0);
    @Bind(variableName = "hoverColor")
    public final BindableAttribute<Integer> hoverColor = new BindableAttribute<>(Integer.class, 0);
    @Bind(variableName = "pressColor")
    public final BindableAttribute<Integer> pressColor = new BindableAttribute<>(Integer.class, 0);
    @Bind(variableName = "disabled")
    public final BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class, false);


    @Bind(variableName = "tooltip")
    public final BindableAttribute<Widget> tooltip = new BindableAttribute<>(Widget.class);

    @Getter
    private PartyFinderParty party;

    public WidgetPartyElement(WidgetPartyFinder widgetPartyFinder, int slot) {
        super(new ResourceLocation("dungeonsguide:gui/features/partyFinder/party_element.gui"));
        this.slot = slot;
        this.widgetPartyFinder = widgetPartyFinder;
        WidgetHoverTooltip hoverTooltip;
        this.tooltip.setValue(hoverTooltip = new WidgetHoverTooltip(this::createTooltip));
    }

    @Getter
    private boolean highlighted = false;
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

            if (nodupe && party.members.stream().anyMatch(a -> a.getClazz().equalsIgnoreCase(FeatureRegistry.PARTYKICKER_CUSTOM.getLastClass()))) {
                note = note.replace("nodupe", "§cnodupe§r").replace("no dupe", "§cno dupe§r").replace("nd", "§cnd§r");
            }

            if (party.members.stream().anyMatch(a -> a.getClazz().equalsIgnoreCase(FeatureRegistry.PARTYKICKER_CUSTOM.getHighlightClass()))) {
                note = note+"§e";
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
        this.disabled.setValue(!party.canJoin);


        boolean nodupechk = nodupe && party.members.stream().anyMatch(a -> a.getClazz().equalsIgnoreCase(FeatureRegistry.PARTYKICKER_CUSTOM.getLastClass()));
        int mixColor = 0;
        highlighted = false;
        if (cantJoin) {}
        else if (nodupechk) {
            mixColor = 0x44FF0000;
        } else if (note.contains("§e")) {
            mixColor = 0x44FFFF00;
            highlighted = true;
        } else if (note.contains("§6")){
            mixColor = 0x44FFAA00;
        }

        this.backgroundColor.setValue(RenderUtils.blendTwoColors(0xff141414, mixColor));
        this.hoverColor.setValue(RenderUtils.blendTwoColors(0xff2f2f2f, mixColor));
        this.pressColor.setValue(RenderUtils.blendTwoColors(0xff2b2b2b, mixColor));
        this.party = party;
    }

    public MinecraftTooltip createTooltip() {
        if (party == null) return new MinecraftTooltip();
        List<String> toHover = party.itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
        for (int i = 0; i < toHover.size(); ++i) {
            if (i == 0) {
                toHover.set(i, party.itemStack.getRarity().rarityColor + toHover.get(i));
            } else {
                toHover.set(i, EnumChatFormatting.GRAY + toHover.get(i));
            }
        }

        MinecraftTooltip minecraftTooltip =  new MinecraftTooltip();
        minecraftTooltip.setTooltip(toHover);
        return minecraftTooltip;
    }

    @On(functionName = "click")
    public void onClick() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(slot, 0, 0);
    }



}
