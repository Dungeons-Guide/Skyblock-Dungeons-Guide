package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.WidgetPresetRoomDetails;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
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

        DungeonMechanic dungeonMechanic = roomInfo.getDungeonRoomInfo().getMechanics().get(mechanicName);
        if (dungeonMechanic instanceof DungeonSecretChest) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/chest.png");
        } else if (dungeonMechanic instanceof DungeonSecretBat) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/bat.png");
        } else if (dungeonMechanic instanceof DungeonSecretEssence) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/essence.png");
        } else if (dungeonMechanic instanceof DungeonSecretItemDrop) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/itemdrop.png");
        } else if (dungeonMechanic instanceof DungeonSecretDoubleChest) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/largechest.png");
        } else if (dungeonMechanic instanceof DungeonLever || dungeonMechanic instanceof DungeonOnewayLever) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/lever.png");
        } else if (dungeonMechanic instanceof DungeonPressurePlate) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/pressureplate.png");
        } else if (dungeonMechanic instanceof DungeonDoor || dungeonMechanic instanceof DungeonOnewayDoor) {
            this.image.setValue("dungeonsguide:textures/features/precalclist/door.png");
        } else if (dungeonMechanic instanceof DungeonRoomDoor2) {
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
