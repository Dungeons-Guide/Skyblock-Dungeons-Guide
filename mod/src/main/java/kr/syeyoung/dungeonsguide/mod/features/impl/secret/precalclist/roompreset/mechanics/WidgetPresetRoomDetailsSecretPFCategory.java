package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.modapi.data.ResourceIdentifier;

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

    @Bind(variableName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute(Column.class);


    public WidgetPresetRoomDetailsSecretPFCategory(String mechanicName, String state,
                                                   List<PathfindRequest> requests,
                                                   AdditionalInfoCaculatedDungeonRoomInfo roomInfo, WidgetPresetRoomDetailsSecret parent) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/roompresetview/secretroomstate.gui"));

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

    public void setDetailsWidget(Widget w) {
        this.parent.setDetailsWidget(w);
    }


}
