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

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import com.google.gson.stream.JsonReader;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.ModalConfirm;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class WidgetPresetList extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "presetList")
    public final BindableAttribute<List<WidgetPreset>> widgetList = new BindableAttribute(WidgetList.class);

    private FeatureDungeonMap2 dungeonMap2;
    private WidgetMapConfiguration mapConfiguration;
    public WidgetPresetList(FeatureDungeonMap2 dungeonMap2, WidgetMapConfiguration configuration) {
        super(Identifier.parse("dungeonsguide:gui/features/map/presetlist.gui"));

        this.dungeonMap2 = dungeonMap2;
        this.mapConfiguration = configuration;

        List<Preset> foundPresets = new ArrayList<>();
        try {
            for (Resource allResource : Minecraft.getInstance().getResourceManager().getResourceStack(Identifier.parse("dungeonsguide:map/presets.json"))) {
                try (InputStream inputStream = allResource.open();
                     JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream))) {

                        Preset[] preset = new Gson().fromJson(jsonReader, Preset[].class);
                        for (Preset preset1 : preset) {
                            preset1.setTexturePack(allResource.sourcePackId());
                            foundPresets.add(preset1);
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<WidgetPreset> presets = foundPresets.stream().map(a -> new WidgetPreset(a, this)).collect(Collectors.toList());
        widgetList.setValue(presets);
    }
    private void load(Preset preset) {
        ModalConfirm modalMessage = new ModalConfirm("Loading this preset will clear previous room overrides");
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(300, 200, "Are you sure?", modalMessage, true), (a) -> {
            if (a == null) return;
            if (a == Boolean.TRUE) {
                dungeonMap2.getMapConfiguration().getRoomOverrides().clear();
                for (Map.Entry<UUID, MapConfiguration.RoomOverride> uuidRoomOverrideEntry : preset.getOverrides().entrySet()) {
                    dungeonMap2.getMapConfiguration().getRoomOverrides().put(
                            uuidRoomOverrideEntry.getKey(),
                            uuidRoomOverrideEntry.getValue()
                    );
                }
                mapConfiguration.rebuildOverrides();
            }
        });

    }

    public static class WidgetPreset extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "location")
        public final BindableAttribute<String> icon = new BindableAttribute<>(String.class, "");
        @Bind(variableName = "name")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class, "");
        @Bind(variableName = "description")
        public final BindableAttribute<String> description = new BindableAttribute<>(String.class, "");

        private WidgetPresetList presetList;
        private Preset preset;
        public WidgetPreset(Preset preset, WidgetPresetList widgetPresetList) {
            super(Identifier.parse("dungeonsguide:gui/features/map/preset.gui"));

            this.name.setValue(preset.getName());
            this.description.setValue(preset.getDescription());
            this.icon.setValue("dungeonsguide:textures/dglogox128.png");
            // 1.8.9's pack names are file names, 26.1's pack ids are prefixed with "file/".
            // the icon is loaded the way vanilla's pack screen does, under a dungeonsguide identifier
            for (Pack pack : Minecraft.getInstance().getResourcePackRepository().getAvailablePacks()) {
                if (pack.getId().equals("file/" + preset.getTexturePack())) {
                    try (PackResources packResources = pack.open()) {
                        IoSupplier<InputStream> resource = packResources.getRootResource("pack.png");
                        if (resource != null) {
                            Identifier resourceLocation = Identifier.fromNamespaceAndPath("dungeonsguide", "preset/" + Hashing.sha1().hashUnencodedChars(pack.getId()) + "/icon");
                            try (InputStream stream = resource.get()) {
                                Minecraft.getInstance().getTextureManager().register(resourceLocation, new DynamicTexture(resourceLocation::toString, NativeImage.read(stream)));
                            }
                            this.icon.setValue(resourceLocation.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            presetList = widgetPresetList;
            this.preset = preset;
        }

        @On(functionName = "load")
        public void load() {
            presetList.load(preset);
        }
    }

}
