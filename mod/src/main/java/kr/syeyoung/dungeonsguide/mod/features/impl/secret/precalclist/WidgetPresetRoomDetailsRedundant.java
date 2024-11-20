package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetPresetRoomDetailsRedundant extends AnnotatedImportOnlyWidget {

    private WidgetPresetRoomDetails parent;
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    @Bind(variableName = "mechanicName")
    public final BindableAttribute<String> mechanicNameV = new BindableAttribute<>(String.class);
    @Bind(variableName = "image")
    public final BindableAttribute<String> image = new BindableAttribute<>(String.class);

    @Bind(variableName = "requests")
    public final BindableAttribute<List<Widget>> requests = new BindableAttribute(WidgetList.class);

    public WidgetPresetRoomDetailsRedundant(WidgetPresetRoomDetails parent, AdditionalInfoCaculatedDungeonRoomInfo roomInfo) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/secretview.gui"));

        this.parent = parent;
        this.roomInfo = roomInfo;


        this.mechanicNameV.setValue("Redunant");
        this.image.setValue("dungeonsguide:textures/darklogo.png");

        List<Widget> toBeAdded = new ArrayList<>();

        for (PathfindPrecalculation precalculation : roomInfo.getUnused()) {
            toBeAdded.add(new WidgetPresetRoomDetailsSecretPrecalculation(
                    precalculation,
                    roomInfo, this));
        }
        this.requests.setValue(toBeAdded);
    }


}
