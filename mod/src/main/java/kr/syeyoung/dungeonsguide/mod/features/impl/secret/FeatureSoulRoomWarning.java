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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonFairySoul;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.TCStringList;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.features.richtext.TextHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextSpan;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.List;
import java.util.*;

public class FeatureSoulRoomWarning extends TextHUDFeature {

    public FeatureSoulRoomWarning() {
        super("Dungeon HUD.Alerts","Secret Soul Alert", "Alert if there is an fairy soul in the room", "secret.fairysoulwarn");
        registerDefaultStyle("warning", DefaultingDelegatingTextStyle.derive("Feature Default - Warning", () -> FeatureRegistry.DEFAULT_STYLE.getStyle(DefaultTextHUDFeatureStyleFeature.Styles.WARNING)));

        addParameter("roomuids", new FeatureParameter<>("roomuids", "Disabled room Names", "Disable for these rooms", new ArrayList<>(), TCStringList.INSTANCE)
                .setWidgetGenerator(RoomConfiguration::new));
    }

    @Override
    public boolean isHUDViewable() {
        return warning > System.currentTimeMillis() && SkyblockStatus.isOnDungeon();
    }


    private UUID lastRoomUID = UUID.randomUUID();
    private long warning = 0;


    @Override
    public TextSpan getText() {
        return new TextSpan(getStyle("warning"), "There is a fairy soul in this room!");
    }


    @DGEventHandler
    public void onTick(DGTickEvent event) {
        if (!SkyblockStatus.isOnDungeon()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null || DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext().getScaffoldParser() == null) return;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        if (thePlayer == null) return;
        Point roomPt = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getScaffoldParser().getRoomMap().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;
        if (dungeonRoom.getDungeonRoomInfo() == null) return;

        if (!dungeonRoom.getDungeonRoomInfo().getUuid().equals(lastRoomUID)) {
            for (DungeonMechanic value : dungeonRoom.getMechanics().values()) {
                if (value instanceof DungeonFairySoul)
                    warning = System.currentTimeMillis() + 2500;
            }
            lastRoomUID = dungeonRoom.getDungeonRoomInfo().getUuid();
        }
    }

    public static class RoomConfiguration extends AnnotatedImportOnlyWidget {

        @Bind(
                variableName = "rooms"
        )
        public final BindableAttribute rooms = new BindableAttribute(WidgetList.class);

        public final Map<String, BindableAttribute<Boolean>> ifOrnot = new HashMap<>();

        FeatureParameter<List<String>> uids;
        private List<RoomSwitch> switches;
        public RoomConfiguration(FeatureParameter<List<String>> uids) {
            super(new ResourceLocation("dungeonsguide:gui/features/fairysoul/roomconfiguration.gui"));

            for (String s : uids.getValue()) {
                ifOrnot.put(s, new BindableAttribute<>(Boolean.class, true));
            }

            rooms.setValue(switches = buildRooms());

            for (Map.Entry<String, BindableAttribute<Boolean>> value : ifOrnot.entrySet()) {
                value.getValue().addOnUpdate((old, neu) -> {
                    Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    if (neu) uids.getValue().add(value.getKey());
                    else uids.getValue().remove(value.getKey());
                });
            }
        }

        public List<RoomSwitch> buildRooms() {
            List<RoomSwitch> switches1 = new LinkedList<>();
            for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                boolean found = false;
                for (DungeonMechanic value : dungeonRoomInfo.getMechanics().values()) {
                    if (value instanceof DungeonFairySoul) {
                        found = true;
                        break;
                    }
                }
                if (!found) continue;

                if (!ifOrnot.containsKey(dungeonRoomInfo.getUuid().toString()))
                    ifOrnot.put(dungeonRoomInfo.getUuid().toString(), new BindableAttribute<>(Boolean.class, false));
                RoomSwitch roomSwitch = new RoomSwitch(dungeonRoomInfo, ifOrnot.get(dungeonRoomInfo.getUuid().toString()));
                switches1.add(roomSwitch);
            }
            return switches1;
        }

        @On(functionName = "eall")
        public void enableAll() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            for (BindableAttribute<Boolean> value : ifOrnot.values()) {
                value.setValue(true);
            }
        }
        @On(functionName = "dall")
        public void disableAll() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            for (BindableAttribute<Boolean> value : ifOrnot.values()) {
                value.setValue(false);
            }
        }
    }

    public static class RoomSwitch extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "name")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class);
        @Bind(variableName = "enabled")
        public final BindableAttribute<Boolean> enabled= new BindableAttribute<>(Boolean.class);
        public RoomSwitch(DungeonRoomInfo dungeonRoomInfo, BindableAttribute<Boolean> linkTo) {
            super(new ResourceLocation("dungeonsguide:gui/features/fairysoul/roomswitch.gui"));
            name.setValue(dungeonRoomInfo.getName());
            enabled.exportTo(linkTo);
        }
    }
}
