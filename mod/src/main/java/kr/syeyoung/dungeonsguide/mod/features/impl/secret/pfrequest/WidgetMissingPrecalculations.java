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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindCache;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class WidgetMissingPrecalculations extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "shape")
    public final BindableAttribute<String> shape = new BindableAttribute<String>(String.class);
    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<String>(String.class);
    @Bind(variableName = "uuid")
    public final BindableAttribute<String> uuid = new BindableAttribute<String>(String.class);
    @Bind(variableName = "roomColor")
    public final BindableAttribute<Integer> roomColor = new BindableAttribute<Integer>(Integer.class);
    @Bind(variableName = "loaded")
    public final BindableAttribute<String> loaded = new BindableAttribute<String>(String.class);
    @Bind(variableName = "missing")
    public final BindableAttribute<String> missing = new BindableAttribute<String>(String.class);

    public WidgetMissingPrecalculations(UUID uuid2, List<PathfindCache> pathfindCaches, List<PathfindRequest> required) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/missingprecalculations.gui"));
        DungeonRoomInfo dungeonRoomInfo = DungeonRoomInfoRegistry.getByUUID(uuid2);

        name.setValue(dungeonRoomInfo.getName());
        uuid.setValue(dungeonRoomInfo.getUuid().toString());
        StringBuilder builder = new StringBuilder();

        if (dungeonRoomInfo.getShape() == 1) {
            // 1x1
            this.shape.setValue("1x1-" + dungeonRoomInfo.getTotalSecrets());
        } else if (dungeonRoomInfo.getShape() == 3 || dungeonRoomInfo.getShape() == 17) {
            // 1x2
            this.shape.setValue("1x2-" + dungeonRoomInfo.getTotalSecrets());
        } else if (dungeonRoomInfo.getShape() == 7 || dungeonRoomInfo.getShape() == 273) {
            // 1x3
            this.shape.setValue("1x3-" + dungeonRoomInfo.getTotalSecrets());
        } else if (dungeonRoomInfo.getShape() == 15 || dungeonRoomInfo.getShape() == 4369) {
            // 1x4
            this.shape.setValue("1x4-" + dungeonRoomInfo.getTotalSecrets());
        } else if (dungeonRoomInfo.getShape() == 51) {
            // 2x2
            this.shape.setValue("2x2-" + dungeonRoomInfo.getTotalSecrets());
        } else if (dungeonRoomInfo.getShape() == 50 || dungeonRoomInfo.getShape() == 49 || dungeonRoomInfo.getShape() == 35 || dungeonRoomInfo.getShape() == 19) {
            // L
            this.shape.setValue("L-" + dungeonRoomInfo.getTotalSecrets());
        } else {
            this.shape.setValue("?-" + dungeonRoomInfo.getTotalSecrets());
        }

        int j = dungeonRoomInfo.getColor() & 255;

        int color;
        if (j / 4 == 0) {
            color = 0x00000000;
        } else {
            color = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
        }

        this.roomColor.setValue(color);

        Set<String> loadedIds = pathfindCaches.stream().map(a -> a.getId()).collect(Collectors.toSet());
        this.missing.setValue(required.stream().map(a -> a.getId()).filter(a -> !loadedIds.contains(a))
                .map(a -> a.substring(36, Math.min(136, a.length()))).collect(Collectors.joining("\n")));
        this.loaded.setValue(pathfindCaches.stream().map(a -> a.getId().substring(36, Math.min(136, a.getId().length()))).collect(Collectors.joining("\n")));
    }
}
