/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class WidgetSecret extends AnnotatedWidget {

    @Bind(variableName = "secretName")
    public final BindableAttribute<String> secretName = new BindableAttribute<>(String.class);

    private String id;
    private DungeonRoom room;
    private DungeonMechanic mechanic;
    private Consumer<String> onSelect;
    public WidgetSecret(String name, DungeonRoom room, DungeonMechanic dungeonMechanic, Consumer<String> selectedId) {
        super(new ResourceLocation("dungeonsguide:gui/features/mechanicBrowser/secret.gui"));
        secretName.setValue(name+" §7("+ dungeonMechanic.getCurrentState(room) +", "+
                (dungeonMechanic.getRepresentingPoint(room) != null ?
                        String.format("%.1f", MathHelper.sqrt_double(dungeonMechanic.getRepresentingPoint(room).getBlockPos(room).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                +"m)");
        this.id = name;
        this.mechanic = dungeonMechanic;
        this.room = room;
        this.onSelect = selectedId;
    }

    @Override
    public void onMount() {
        super.onMount();
        secretName.setValue(id+" §7("+ mechanic.getCurrentState(room) +", "+
                (mechanic.getRepresentingPoint(room) != null ?
                        String.format("%.1f", MathHelper.sqrt_double(mechanic.getRepresentingPoint(room).getBlockPos(room).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) : "")
                +"m)");
    }

    private AbsLocationPopup popup;
    @On(functionName = "toggleStates")
    public void openStates() {
        Rect abs = getDomElement().getAbsBounds();
        double x = abs.getX() + abs.getWidth();
        double y = abs.getY();

        if (!PopupMgr.getPopupMgr(getDomElement()).getDomElement().getAbsBounds().contains(x + 120 , y))
            x = abs.getX() - 120;


        onSelect.accept(id);

        if (popup == null) {
            PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
            popupMgr.openPopup(popup = new AbsLocationPopup(x, y, new WidgetStateTooltip(room, mechanic, id), true), (val) -> {
                if (val == null)
                    onSelect.accept(null);
                popup = null;
            });
        }
    }

    @Override
    public void onUnmount() {
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        if (popup != null) {
            popupMgr.closePopup(popup, "a");
            popup = null;
        }
        super.onUnmount();
    }
}
