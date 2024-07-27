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

import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.config.types.TCEnum;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.MarkerData;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteTooltip.WidgetInvite;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.ElementTreeWalkIterator;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Wrap;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WidgetMapConfiguration extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "demo")
    public final BindableAttribute<Widget> widgetBindableAttribute = new BindableAttribute<>(Widget.class);



    @Bind(variableName = "mapscale")
    public final BindableAttribute<Widget> mapscale = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Widget> backgroundColor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "borderThickness")
    public final BindableAttribute<Widget> borderThickness = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "borderColor")
    public final BindableAttribute<Widget> borderColor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "mapRotation")
    public final BindableAttribute<Widget> mapRotation = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "iconscale")
    public final BindableAttribute<Widget> iconscale = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "iconrotation")
    public final BindableAttribute<Widget> iconrotation = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "iconcenter")
    public final BindableAttribute<Widget> iconcenter = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "iconstyle")
    public final BindableAttribute<Widget> iconstyle = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "iconstyledescription")
    public final BindableAttribute<String> iconstyledescription = new BindableAttribute<>(String.class, "");


    @Bind(variableName = "drawname")
    public final BindableAttribute<Widget> drawname = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "selfscale")
    public final BindableAttribute<Widget> selfscale = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "otherscale")
    public final BindableAttribute<Widget> otherscale = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "selfstyle")
    public final BindableAttribute<Widget> selfstyle = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "otherstyle")
    public final BindableAttribute<Widget> otherstyle = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "namecolor")
    public final BindableAttribute<Widget> namecolor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "namestyle")
    public final BindableAttribute<Widget> namestyle = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "namesize")
    public final BindableAttribute<Widget> namesize = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "namepadding")
    public final BindableAttribute<Widget> namepadding = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "drawnameToggle")
    public final BindableAttribute<String> toggle = new BindableAttribute<>(String.class);


    @Bind(variableName = "presetList")
    public final BindableAttribute<Widget> presetList = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "overrides")
    public final BindableAttribute<List<Widget>> overrides = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "overrideApi")
    public final BindableAttribute<Wrap> overrideApi = new BindableAttribute<>(Wrap.class);

    @Bind(variableName = "waypoints")
    public final BindableAttribute<List<Widget>> waypointSettings =  new BindableAttribute(WidgetList.class);
    @Bind(variableName = "waypointsApi")
    public final BindableAttribute<Wrap> waypointSettingsApi = new BindableAttribute<>(Wrap.class);


    private <T> Widget generateConfigWidget(FeatureDungeonMap2 featureDungeonMap2, String key, Function<FeatureParameter<T>, Widget> converter) {
        FeatureParameter<T> featureParameter = featureDungeonMap2.getParameter(key);
        return converter.apply(featureParameter);
    }


    private FeatureDungeonMap2 dungeonMap2;
    public WidgetMapConfiguration(FeatureDungeonMap2 featureDungeonMap2) {
        super(new ResourceLocation("dungeonsguide:gui/features/map/editor.gui"));
        this.dungeonMap2 = featureDungeonMap2;

        widgetBindableAttribute.setValue(featureDungeonMap2.instantiateDemoWidget());

        setupThings(featureDungeonMap2);
    }

    public void setupThings(FeatureDungeonMap2 featureDungeonMap2) {
        backgroundColor.setValue(generateConfigWidget(featureDungeonMap2, "background_color", TCAColor.ColorEditWidget::new));
        borderThickness.setValue(this.<Double>generateConfigWidget(featureDungeonMap2, "border_thickness", TCDouble.DoubleEditWidget::new));
        borderColor.setValue(generateConfigWidget(featureDungeonMap2, "border_color", TCAColor.ColorEditWidget::new));
        mapRotation.setValue(this.<MapConfiguration.MapRotation>generateConfigWidget(featureDungeonMap2, "map_rotation", (a) -> new TCEnum.EnumEditWidget<>(((TCEnum<MapConfiguration.MapRotation>)a.getFeatureTypeHandler()).getValues(), a)));

        drawname.setValue(generateConfigWidget(featureDungeonMap2, "drawname", TCBoolean.BooleanEditWidget::new));

        selfstyle.setValue(this.<MapConfiguration.PlayerHeadSettings.IconType>generateConfigWidget(featureDungeonMap2, "selfstyle", (a) -> new TCEnum.EnumEditWidget<>( ( (TCEnum<MapConfiguration.PlayerHeadSettings.IconType>) a.getFeatureTypeHandler()).getValues(), a)));
        otherstyle.setValue(this.<MapConfiguration.PlayerHeadSettings.IconType>generateConfigWidget(featureDungeonMap2, "otherstyle", (a) -> new TCEnum.EnumEditWidget<>( ( (TCEnum<MapConfiguration.PlayerHeadSettings.IconType>) a.getFeatureTypeHandler()).getValues(), a)));

        mapscale.setValue(this.<Double>generateConfigWidget(featureDungeonMap2, "scale", (a) -> new WidgetScalebar(a, 0.5, 3.0)));
        selfscale.setValue(this.<Double>generateConfigWidget(featureDungeonMap2, "selfscale", (a) -> new WidgetScalebar(a, 0.5, 3.0)));
        otherscale.setValue(this.<Double>generateConfigWidget(featureDungeonMap2, "otherscale", (a) -> new WidgetScalebar(a, 0.5, 3.0)));

        iconscale.setValue(this.<Double>generateConfigWidget(featureDungeonMap2, "iconScale", (a) -> new WidgetScalebar(a, 0.5, 3.0)));
        iconrotation.setValue(this.<MapConfiguration.RoomInfoSettings.IconRotation>generateConfigWidget(featureDungeonMap2, "iconRotation", (a) -> new TCEnum.EnumEditWidget<>(((TCEnum<MapConfiguration.RoomInfoSettings.IconRotation>)a.getFeatureTypeHandler()).getValues(), a)));
        iconcenter.setValue(generateConfigWidget(featureDungeonMap2, "iconCenter", TCBoolean.BooleanEditWidget::new));
        iconstyle.setValue(this.<MapConfiguration.RoomInfoSettings.Style>generateConfigWidget(featureDungeonMap2, "iconStyle", (a) -> new TCEnum.EnumEditWidget<>(((TCEnum<MapConfiguration.RoomInfoSettings.Style>) a.getFeatureTypeHandler()).getValues(), a)));

        namecolor.setValue(generateConfigWidget(featureDungeonMap2, "name_color", TCAColor.ColorEditWidget::new));
        namepadding.setValue(this.<Double>generateConfigWidget(featureDungeonMap2, "name_padding", TCDouble.DoubleEditWidget::new));
        namestyle.setValue(this.<MapConfiguration.NameSettings.NameRotation>generateConfigWidget(featureDungeonMap2, "name_style", (a) -> new TCEnum.EnumEditWidget<>( ( (TCEnum<MapConfiguration.NameSettings.NameRotation>) a.getFeatureTypeHandler()).getValues(), a)));
        namesize.setValue(this.<Double>generateConfigWidget(featureDungeonMap2, "name_scale", (a) -> new WidgetScalebar(a, 0.5, 3.0)));

        presetList.setValue(new WidgetPresetList(featureDungeonMap2, this));

        TCBoolean.BooleanEditWidget booleanEditWidget = (TCBoolean.BooleanEditWidget) drawname.getValue();
        booleanEditWidget.isEnabled.addOnUpdate((old, neu) -> {
            toggle.setValue(neu ? "true" : "false");
        });
        toggle.setValue(booleanEditWidget.isEnabled.getValue() ? "true" : "false");

        ((TCEnum.EnumEditWidget<MapConfiguration.RoomInfoSettings.Style>)iconstyle.getValue()).value.addOnUpdate((old, neu) -> {
            String desc = MapConfiguration.RoomInfoSettings.Style.valueOf(neu).getDescription();
            iconstyledescription.setValue(desc);
        });
        iconstyledescription.setValue(featureDungeonMap2.<MapConfiguration.RoomInfoSettings.Style>getParameter("iconStyle").getValue().getDescription());


        if (waypointSettingsApi.getValue() != null) {
            waypointSettingsApi.getValue().removeAllWidget();;

            for (MarkerData.MobType value : MarkerData.MobType.values()) {
                MapConfiguration.PlayerHeadSettings headSettings = featureDungeonMap2.getMapConfiguration().getHeadSettingsMap().get(value);
                waypointSettingsApi.getValue().addWidget(new WidgetSimpleField(value.name()+" Scale: ", new WidgetScalebar(headSettings.getIconSize(), headSettings::setIconSize, 0.5, 3.0)));
                waypointSettingsApi.getValue().addWidget(new WidgetSimpleField(value.name()+" Style: ", new EnumEditWidget<>(
                        Stream.of(MapConfiguration.PlayerHeadSettings.IconType.values()).filter(a -> a != MapConfiguration.PlayerHeadSettings.IconType.ARROW).collect(Collectors.toList()).toArray(new MapConfiguration.PlayerHeadSettings.IconType[3]), headSettings.getIconType(), headSettings::setIconType)));
            }
        } else {
            List<Widget> widgets = new ArrayList<>();
            for (MarkerData.MobType value : MarkerData.MobType.values()) {
                MapConfiguration.PlayerHeadSettings headSettings = featureDungeonMap2.getMapConfiguration().getHeadSettingsMap().get(value);
                widgets.add(new WidgetSimpleField(value.name()+" Scale: ", new WidgetScalebar(headSettings.getIconSize(), headSettings::setIconSize, 0.5, 3.0)));
                widgets.add(new WidgetSimpleField(value.name()+" Style: ", new EnumEditWidget<>(
                        Stream.of(MapConfiguration.PlayerHeadSettings.IconType.values()).filter(a -> a != MapConfiguration.PlayerHeadSettings.IconType.ARROW).collect(Collectors.toList()).toArray(new MapConfiguration.PlayerHeadSettings.IconType[3]), headSettings.getIconType(), headSettings::setIconType)));
            }
            waypointSettings.setValue(widgets);
        }

        rebuildOverrides();
    }

    @On(functionName = "addOverride")
    public void addOverride() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        WidgetAddRoomPopup modalMessage = new WidgetAddRoomPopup(this);
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Add New Room", modalMessage, true), (a) -> {
            if (a instanceof UUID)
                addOverride((UUID)a);
        });
    }
    @On(functionName = "resetscale")
    public void resetScale() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        for (MapConfiguration.PlayerHeadSettings value : dungeonMap2.getMapConfiguration().getHeadSettingsMap().values()) {
            value.setIconSize(1.0);
        }
    }

    public void addOverride(UUID uuid) {
        dungeonMap2.getMapConfiguration().getRoomOverrides().put(uuid, new MapConfiguration.RoomOverride());
        rebuildOverrides();
    }

    public boolean check(UUID uuid) {
        return dungeonMap2.getMapConfiguration().getRoomOverrides().containsKey(uuid);
    }


    @On(functionName = "reset")
    public void reset() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        for (FeatureParameter parameter : dungeonMap2.getParameters()) {
            parameter.setValue(parameter.getDefault_value());
        }
        dungeonMap2.getMapConfiguration().getRoomOverrides().clear();

        Preset preset = new Gson().fromJson(FeatureDungeonMap2.DEFAULT_OVERRIDES, Preset.class);
        dungeonMap2.getMapConfiguration().getRoomOverrides().putAll(preset.getOverrides());

        setupThings(dungeonMap2);
    }

    public void rebuildOverrides() {
        List<Widget> widgets = new ArrayList<>();
        for (Map.Entry<UUID, MapConfiguration.RoomOverride> uuidRoomOverrideEntry : dungeonMap2.getMapConfiguration().getRoomOverrides().entrySet()) {
            widgets.add(new WidgetRoomOverride(uuidRoomOverrideEntry.getKey(), uuidRoomOverrideEntry.getValue(), this));
        }
        if (overrideApi.getValue() != null) {
            overrideApi.getValue().removeAllWidget();
            for (Widget widget : widgets) {
                overrideApi.getValue().addWidget(widget);
            }
        } else {
            overrides.setValue(widgets);
        }
    }

    public void deleteOverride(UUID uuid) {
        dungeonMap2.getMapConfiguration().getRoomOverrides().remove(uuid);
        rebuildOverrides();
    }

    public static class WidgetSimpleField extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "fieldName")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class);
        @Bind(variableName = "fieldValue")
        public final BindableAttribute<Widget> value = new BindableAttribute<>(Widget.class);

        public WidgetSimpleField(String name, Widget editor) {
            super(new ResourceLocation("dungeonsguide:gui/features/map/field.gui"));
            this.name.setValue(name);
            this.value.setValue(editor);
        }
    }
    public static class WidgetRoomOverride extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "iconLocation")
        public final BindableAttribute<String> iconLocation = new BindableAttribute<>(String.class);
        @Bind(variableName = "textureLocation")
        public final BindableAttribute<String> textureLocation = new BindableAttribute<>(String.class);
        @Bind(variableName = "name")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class);
        @Bind(variableName = "drawName")
        public final BindableAttribute<Widget> drawName = new BindableAttribute<>(Widget.class);
        @Bind(variableName = "roomName")
        public final BindableAttribute<String> roomName = new BindableAttribute<>(String.class);

        @Bind(variableName = "defaultName")
        public final BindableAttribute<String> defaultName = new BindableAttribute<>(String.class);

        @Bind(variableName = "iconStyle")
        public final BindableAttribute<Widget> iconStyle = new BindableAttribute<>(Widget.class);
        @Bind(variableName = "iconRotation")
        public final BindableAttribute<Widget> iconRotation = new BindableAttribute<>(Widget.class);

        @Bind(variableName = "roomColor")
        public final BindableAttribute<Integer> roomColor = new BindableAttribute<>(Integer.class);

        private WidgetMapConfiguration widgetMapConfiguration;
        private UUID uuid;
        public WidgetRoomOverride(UUID uuid, MapConfiguration.RoomOverride roomOverride, WidgetMapConfiguration configuration) {
            super(new ResourceLocation("dungeonsguide:gui/features/map/roomoverride.gui"));

            this.widgetMapConfiguration = configuration;
            this.uuid = uuid;

            this.iconLocation.setValue(roomOverride.getIconLocation());
            this.iconLocation.addOnUpdate((old, neu) -> roomOverride.setIconLocation(neu));
            this.textureLocation.setValue(roomOverride.getTextureLocation());
            this.textureLocation.addOnUpdate((old, neu) -> roomOverride.setTextureLocation(neu));
            this.name.setValue(roomOverride.getNameOverride());
            this.name.addOnUpdate((old, neu) -> roomOverride.setNameOverride(neu));

            this.iconStyle.setValue(new EnumEditWidget<>(MapConfiguration.RoomInfoSettings.Style.values(),
                    roomOverride.getStyle(), roomOverride::setStyle));
            this.iconRotation.setValue(new EnumEditWidget<>(MapConfiguration.RoomInfoSettings.IconRotation.values(),
                    roomOverride.getIconRotation(), roomOverride::setIconRotation));
            this.drawName.setValue(new BooleanEditWidget(roomOverride.isDrawName(), roomOverride::setDrawName));

            DungeonRoomInfo info = DungeonRoomInfoRegistry.getByUUID(uuid);
            this.defaultName.setValue(info == null ? "Unknown Room" : DungeonRoomInfoRegistry.getByUUID(uuid).getName());

            if (info == null) {
                this.roomColor.setValue(0xFFFF0000);
            } else {
                int j = info.getColor() & 255;

                int color;
                if (j / 4 == 0) {
                    color = 0x00000000;
                } else {
                    color = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
                }

                this.roomColor.setValue(color);
            }
            this.uuid = uuid;

            if (info != null) {
                short shapeShort = info.getShape();
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
                shape += info.getTotalSecrets();
                shape += " ";
                shape += info.getName();
                this.roomName.setValue(shape);
            } else {
                this.roomName.setValue("Unknown Room");
            }
        }

        @On(functionName = "delete")
        public void delete() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            widgetMapConfiguration.deleteOverride(uuid);
        }
    }

    @On(functionName = "syncscale")
    public void syncScale() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        FeatureParameter<Double> param1 = dungeonMap2.<Double>getParameter("selfscale");
        FeatureParameter<Double> param2 = dungeonMap2.<Double>getParameter("otherscale");
        double newVal = Math.max(param1.getValue(), param2.getValue());

        param2.setValue(newVal);
        param1.setValue(newVal);

        selfscale.setValue(this.<Double>generateConfigWidget(dungeonMap2, "selfscale", (a) -> new WidgetScalebar(a, 0.5, 3.0)));
        otherscale.setValue(this.<Double>generateConfigWidget(dungeonMap2, "otherscale", (a) -> new WidgetScalebar(a, 0.5, 3.0)));
    }


    public static class BooleanEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "enabled")
        public final BindableAttribute<Boolean> isEnabled = new BindableAttribute<>(Boolean.class);
        public BooleanEditWidget(Boolean defaultValue, Consumer<Boolean> onUpdate) {
            super(new ResourceLocation("dungeonsguide:gui/config/parameter/boolean.gui"));
            isEnabled.setValue(defaultValue);
            isEnabled.addOnUpdate((old, neu) -> {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                onUpdate.accept(neu);
            });
        }
    }

    public static class EnumEditWidget<T extends Enum<T>> extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "value")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);
        private T[] values;
        private int idx;
        private Consumer<T> onUpdate;
        public EnumEditWidget(T[] values, T defaultValue, Consumer<T> onUpdate) {
            super(new ResourceLocation("dungeonsguide:gui/config/parameter/stringChoice.gui"));
            this.idx = defaultValue.ordinal();
            this.values = values;
            this.onUpdate = onUpdate;
            value.setValue(defaultValue.name());
        }

        @On(functionName = "inc")
        public void inc() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            idx = (idx + 1) % values.length;
            value.setValue(values[idx].name());
            onUpdate.accept(values[idx]);
            for (DomElement dom : new ElementTreeWalkIterator(getDomElement())) {
                if (dom.getWidget() instanceof ParameterItem) {
                    ((ParameterItem) dom.getWidget()).update();
                    break;
                }
            }
        }
        @On(functionName = "dec")
        public void dec() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            idx = (values.length + idx - 1) % values.length;
            value.setValue(values[idx].name());
            onUpdate.accept(values[idx]);
            for (DomElement dom : new ElementTreeWalkIterator(getDomElement())) {
                if (dom.getWidget() instanceof ParameterItem) {
                    ((ParameterItem) dom.getWidget()).update();
                    break;
                }
            }
        }
    }
}
