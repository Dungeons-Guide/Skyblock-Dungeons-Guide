package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.WidgetPresetRoomDetails;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetPresetRoomDetailsSecret extends AnnotatedImportOnlyWidget {

    private String mechanicName;
    private WidgetPresetRoomDetails parent;
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    @Bind(variableName = "mechanicName")
    public final BindableAttribute<String> mechanicNameV = new BindableAttribute<>(String.class);
    @Bind(variableName = "image")
    public final BindableAttribute<String> image = new BindableAttribute<>(String.class);


    @Bind(variableName = "requests")
    public final BindableAttribute<List<Widget>> requests = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute<>(Column.class);

    public WidgetPresetRoomDetailsSecret(String mechanicName, WidgetPresetRoomDetails parent, AdditionalInfoCaculatedDungeonRoomInfo roomInfo) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/secretview.gui"));

        this.mechanicName = mechanicName;
        this.parent = parent;
        this.roomInfo = roomInfo;


        this.mechanicNameV.setValue(mechanicName);

        DungeonMechanicData dungeonMechanicState = roomInfo.getDungeonRoomInfo().getMechanics().get(mechanicName);
        if (dungeonMechanicState instanceof DungeonSecretChestState.DungeonSecretChestData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/chest.png");
        } else if (dungeonMechanicState instanceof DungeonSecretBatState.DungeonSecretBatData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/bat.png");
        } else if (dungeonMechanicState instanceof DungeonSecretEssenceState.DungeonSecretEssenceData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/essence.png");
        } else if (dungeonMechanicState instanceof DungeonSecretItemDropState.DungeonSecretItemDropData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/itemdrop.png");
        } else if (dungeonMechanicState instanceof DungeonSecretDoubleChestState.DungeonSecretDoubleChestData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/largechest.png");
        } else if (dungeonMechanicState instanceof DungeonLeverState.DungeonLeverData || dungeonMechanicState instanceof DungeonOnewayLeverState.DungeonOnewayLeverData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/lever.png");
        } else if (dungeonMechanicState instanceof DungeonPressurePlateState.DungeonPressurePlateData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/pressureplate.png");
        } else if (dungeonMechanicState instanceof DungeonDoorState.DungeonDoorData || dungeonMechanicState instanceof DungeonOnewayDoorState.DungeonOnewayDoorData) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/door.png");
        } else if (dungeonMechanicState instanceof DungeonRoomDoor2State.DungeonRoomDoor2Data) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/witherdoor.png");
        } else {
            this.image.setValue("dungeonsguide:textures/darklogo.png");
        }

        List<Widget> toBeAdded = new ArrayList<>();

        for (AdditionalInfoCaculatedDungeonRoomInfo.RoomStateInfo stateInfo : roomInfo.getStateInfos()) {
            toBeAdded.add(new WidgetPresetRoomDetailsSecretPFCategory(mechanicName, stateInfo.getStateIdentifier(),
                    stateInfo.getMechanicPrecalculationMap().get(mechanicName).getRequiredPrecalculationHash(), roomInfo, this));
        }
        this.requests.setValue(toBeAdded);
    }

    public void setDetailsWidget(Widget w) {
        this.parent.setDetailsWidget(w);
    }


}
