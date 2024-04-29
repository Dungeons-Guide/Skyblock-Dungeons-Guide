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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.discord.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteTooltip.WidgetDiscordFriend;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;

public class WidgetAddRoomPopup extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "search")
    public final BindableAttribute<String> search = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "roomList")
    public final BindableAttribute<Column> friendList = new BindableAttribute<>(Column.class);

    @Bind(variableName = "initialRoomList")
    public final BindableAttribute initialFriendList = new BindableAttribute(WidgetList.class);


    public void add(UUID uuid) {
        PopupMgr.getPopupMgr(getDomElement()).closePopup(uuid);
    }

    private WidgetMapConfiguration widgetMapConfiguration;
    public WidgetAddRoomPopup(WidgetMapConfiguration mapConfiguration) {
        super(new ResourceLocation("dungeonsguide:gui/features/map/add_room_popup.gui"));
        this.widgetMapConfiguration =mapConfiguration;
        List<Widget> widgets = new ArrayList<>();

        for (DungeonRoomInfo value : DungeonRoomInfoRegistry.getRegistered()) {
            if (widgetMapConfiguration.check(value.getUuid())) continue;
            widgets.add(new WidgetRoom(value, this::add));
        }

        initialFriendList.setValue(widgets);
        search.addOnUpdate((old,neu) -> resetListContent());
    }


    private void resetListContent() {
        if (!getDomElement().isMounted()) return;
        friendList.getValue().removeAllWidget();

        String searchTxt = search.getValue().trim().toLowerCase();
        for (DungeonRoomInfo value : DungeonRoomInfoRegistry.getRegistered()) {
            if (widgetMapConfiguration.check(value.getUuid())) continue;


            short shapeShort = value.getShape();
            String shape = "";
            switch (shapeShort){
                case 0x1:
                    shape = "1x1";
                    break;
                case 0x3:
                case 0x11:
                    shape = "1x2";
                    break;
                case 0x33:
                    shape = "2x2";
                    break;
                case 0x7:
                case 0x111:
                    shape = "1x3";
                    break;
                case 0xF:
                case 0x1111:
                    shape = "1x4";
                    break;
                case 0x13:
                case 0x31:
                case 0x23:
                case 0x32:
                    shape = "L";
                    break;
                default:
                    shape = shapeShort+"?";
            }
            shape += "-";
            shape += value.getTotalSecrets();
            shape += " ";
            shape += value.getName();

            if (!shape.toLowerCase().contains(searchTxt)) continue;
            friendList.getValue().addWidget(new WidgetRoom(value, this::add));
        }
    }

    public static class WidgetRoom extends AnnotatedImportOnlyWidget {

        @Bind(variableName = "color")
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0);
        @Bind(variableName = "name")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class, "");

        private UUID uuid;
        private Consumer<UUID> add;
        public WidgetRoom(DungeonRoomInfo dungeonRoomInfo, Consumer<UUID> add) {
            super(new ResourceLocation("dungeonsguide:gui/features/map/room.gui"));
            this.uuid = dungeonRoomInfo.getUuid();

            short shapeShort = dungeonRoomInfo.getShape();
            String shape = "";
            switch (shapeShort){
                case 0x1:
                    shape = "1x1";
                    break;
                case 0x3:
                case 0x11:
                    shape = "1x2";
                    break;
                case 0x33:
                    shape = "2x2";
                    break;
                case 0x7:
                case 0x111:
                    shape = "1x3";
                    break;
                case 0xF:
                case 0x1111:
                    shape = "1x4";
                    break;
                case 0x13:
                case 0x31:
                case 0x23:
                case 0x32:
                    shape = "L";
                    break;
                default:
                    shape = shapeShort+"?";
            }


            this.name.setValue(shape+"-"+dungeonRoomInfo.getTotalSecrets() + " " + dungeonRoomInfo.getName());

            int j = dungeonRoomInfo.getColor() & 255;

            int color;
            if (j / 4 == 0) {
                color = 0x00000000;
            } else {
                color = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
            }

            this.color.setValue(color);
            this.add = add;
        }

        @On(functionName = "add")
        public void add() {
            this.add.accept(uuid);
        }
    }
}
