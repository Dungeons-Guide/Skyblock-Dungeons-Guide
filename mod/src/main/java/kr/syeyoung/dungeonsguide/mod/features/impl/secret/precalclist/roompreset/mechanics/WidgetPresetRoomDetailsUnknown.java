package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics;

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

public class WidgetPresetRoomDetailsUnknown extends AnnotatedImportOnlyWidget {

    private WidgetPresetRoomDetails parent;
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    @Bind(variableName = "mechanicName")
    public final BindableAttribute<String> mechanicNameV = new BindableAttribute<>(String.class);
    @Bind(variableName = "image")
    public final BindableAttribute<String> image = new BindableAttribute<>(String.class);

    @Bind(variableName = "requests")
    public final BindableAttribute<List<Widget>> requests = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute(Column.class);

    public WidgetPresetRoomDetailsUnknown(WidgetPresetRoomDetails parent, AdditionalInfoCaculatedDungeonRoomInfo roomInfo) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/secretview.gui"));

        this.parent = parent;
        this.roomInfo = roomInfo;


        this.mechanicNameV.setValue("UNKNOWN");
        this.image.setValue("dungeonsguide:textures/darklogo.png");

        List<Widget> toBeAdded = new ArrayList<>();

        for (String precalculation : roomInfo.getMissingPrecalculation()) {
            toBeAdded.add(new WidgetPresetRoomDetailsSecretUnknownPrecalculation(
                    precalculation,
                    roomInfo, this));
        }
        this.requests.setValue(toBeAdded);
    }


    public void setDetailsWidget(Widget w) {
        this.parent.setDetailsWidget(w);
    }

    public void remove(WidgetPresetRoomDetailsSecretUnknownPrecalculation widgetPresetRoomDetailsSecretPrecalculation) {
        if (this.api.getValue() != null)
            this.api.getValue().removeWidget(widgetPresetRoomDetailsSecretPrecalculation);
        this.requests.getValue().remove(widgetPresetRoomDetailsSecretPrecalculation);
    }
}
