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

import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class WidgetMechanicBrowser extends AnnotatedWidget implements Layouter {
    @Bind(variableName = "current")
    public final BindableAttribute<String> current = new BindableAttribute<>(String.class);
    @Bind(variableName = "children")
    public final BindableAttribute children = new BindableAttribute<>(WidgetList.class);
    @Bind(variableName = "scale")
    public final BindableAttribute<Double> scale = new BindableAttribute<>(Double.class);
    @Bind(variableName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);


    @Setter @Getter
    private String selectedId;

    private static final List<String> order = Arrays.asList(
            "Wizard", "Redstone Key", "Fairy Soul", "Secrets", "Crypts", "NPC", "Journals", "Gates", "ETC", "Dummy"
    );
    private String map(Class t) {
        if (t == DungeonFairySoul.class) return "Fairy Soul";
        if (t == DungeonSecret.class) return "Secrets";
        if (t == DungeonTomb.class) return "Crypts";
        if (t == DungeonNPC.class) return "NPC";
        if (t == DungeonJournal.class) return "Journals";
        if (t == DungeonRoomDoor.class) return "Gates";
        if (t == DungeonDummy.class) return "Dummy";
        if (t == DungeonWizard.class) return "Wizard";
        if (t == DungeonRedstoneKey.class) return "Redstone Key";
        if (t == DungeonRedstoneKeySlot.class) return "ETC";
        if (t == DungeonWizardCrystal.class) return "Wizard";
        return "ETC";
    }

    private DungeonRoom dungeonRoom;
    public WidgetMechanicBrowser(DungeonRoom dungeonRoom) {
        super(new ResourceLocation("dungeonsguide:gui/features/mechanicBrowser/browser.gui"));
        scale.setValue(FeatureRegistry.SECRET_BROWSE.getScale());
        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
        this.dungeonRoom =dungeonRoom;
        if (grp.getPath("MECH-BROWSER") == null) {
            current.setValue("Nothing");
            color.setValue(0xFFAA0000);
        } else {
            ActionRoute route = grp.getPath("MECH-BROWSER");
            current.setValue(route.toString());
            color.setValue(0xFFFFFF00);
        }
        Map<String, Map<String, DungeonMechanic>> map = new HashMap<>();
        for (Map.Entry<String, DungeonMechanic> stringDungeonMechanicEntry : dungeonRoom.getMechanics().entrySet()) {
            String name = map(stringDungeonMechanicEntry.getValue().getClass());
            if (!map.containsKey(name))
                map.put(name, new HashMap<>());
            map.get(name).put(stringDungeonMechanicEntry.getKey(), stringDungeonMechanicEntry.getValue());
        }

        List<Widget> widgets = new ArrayList<>();
        for (String s : order) {
            if (!FeatureRegistry.DEBUG.isEnabled() && s.equals("Dummy")) continue;
            if (map.containsKey(s))
                widgets.add(new WidgetCategory(s, dungeonRoom, map.get(s), this::setSelectedId));
        }
        children.setValue(widgets);
    }

    public void update() {
        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
        if (grp.getPath("MECH-BROWSER") == null) {
            current.setValue("Nothing");
            color.setValue(0xFFAA0000);
        } else {
            ActionRoute route = grp.getPath("MECH-BROWSER");
            current.setValue(route.toString());
            color.setValue(0xFFFFFF00);
        }
    }

    @On(functionName = "cancel")
    public void cancel() {
        GeneralRoomProcessor grp = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
        grp.cancel("MECH-BROWSER");
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        FeatureMechanicBrowse featureMechanicBrowse = FeatureRegistry.SECRET_BROWSE;
        Double ratio = featureMechanicBrowse.getRatio();
        Size size=  new Size(featureMechanicBrowse.getFeatureRect().getWidth(),
                ratio != null ? featureMechanicBrowse.getFeatureRect().getWidth() * ratio : featureMechanicBrowse.getFeatureRect().getHeight());

        if (buildContext.getChildren().isEmpty()) {
            return size;
        }

        DomElement childCtx = buildContext.getChildren().get(0);

        Size dim = childCtx.getLayouter().layout(childCtx, new ConstraintBox(size.getWidth(), size.getWidth(), size.getHeight(), size.getHeight()));
        childCtx.setRelativeBound(new Rect(0,0, dim.getWidth(), dim.getHeight()));
        return dim;
    }
}
