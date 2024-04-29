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
import com.google.gson.stream.JsonReader;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.ModalConfirm;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.ModalMessage;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

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
        super(new ResourceLocation("dungeonsguide:gui/features/map/presetlist.gui"));

        this.dungeonMap2 = dungeonMap2;
        this.mapConfiguration = configuration;

        List<Preset> foundPresets = new ArrayList<>();
        try {
            for (IResource allResource : Minecraft.getMinecraft().getResourceManager().getAllResources(new ResourceLocation("dungeonsguide:map/presets.json"))) {
                try (InputStream inputStream = allResource.getInputStream();
                     JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream))) {

                        Preset[] preset = new Gson().fromJson(jsonReader, Preset[].class);
                        for (Preset preset1 : preset) {
                            preset1.setTexturePack(allResource.getResourcePackName());
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
            super(new ResourceLocation("dungeonsguide:gui/features/map/preset.gui"));

            this.name.setValue(preset.getName());
            this.description.setValue(preset.getDescription());
            this.icon.setValue("dungeonsguide:textures/dglogox128.png");
            for (ResourcePackRepository.Entry repositoryEntry : Minecraft.getMinecraft().getResourcePackRepository()
                    .getRepositoryEntries()) {
                if (repositoryEntry.getResourcePackName().equals(preset.getTexturePack())) {
                    repositoryEntry.bindTexturePackIcon(Minecraft.getMinecraft().getTextureManager());
                    ResourceLocation resourceLocation = ReflectionHelper.getPrivateValue(ResourcePackRepository.Entry.class, repositoryEntry, "locationTexturePackIcon", "field_5260", "f");
                    if (resourceLocation != null)
                        this.icon.setValue(resourceLocation.toString());
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
