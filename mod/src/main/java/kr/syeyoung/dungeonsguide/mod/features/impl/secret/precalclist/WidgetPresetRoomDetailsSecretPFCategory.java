package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetPresetRoomDetailsSecretPFCategory extends AnnotatedImportOnlyWidget {

    private String mechanicName;
    private WidgetPresetRoomDetailsSecret parent;
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    @Bind(variableName = "state")
    public final BindableAttribute<String> state = new BindableAttribute<>(String.class);
    @Bind(variableName = "precalculations")
    public final BindableAttribute<List<Widget>> pathfindrequests = new BindableAttribute(WidgetList.class);



    public WidgetPresetRoomDetailsSecretPFCategory(String mechanicName, String state,
                                                   List<PathfindRequest> requests,
                                                   AdditionalInfoCaculatedDungeonRoomInfo roomInfo, WidgetPresetRoomDetailsSecret parent) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/secretroomstate.gui"));

        this.mechanicName = mechanicName;
        this.parent = parent;
        this.roomInfo = roomInfo;


        this.state.setValue(state.isEmpty() ? "(empty)" : state);


        List<Widget> toBeAdded = new ArrayList<>();
        for (PathfindRequest request : requests) {
            toBeAdded.add(new WidgetPresetRoomDetailsSecretPathfindRequest(request, roomInfo, this));
        }
        this.pathfindrequests.setValue(toBeAdded);
    }


}
