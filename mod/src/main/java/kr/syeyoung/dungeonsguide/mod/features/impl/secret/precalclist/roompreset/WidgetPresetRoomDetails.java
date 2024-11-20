package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset;

import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor2;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.ISecret;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics.WidgetPresetRoomDetailsUnused;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics.WidgetPresetRoomDetailsSecret;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WidgetPresetRoomDetails extends AnnotatedImportOnlyWidget {
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    @Bind(variableName = "metadata")
    public final BindableAttribute<Widget> metadata = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "secrets")
    public final BindableAttribute<List<Widget>> secrets = new BindableAttribute(WidgetList.class);


    @Bind(variableName = "details")
    public final BindableAttribute<Widget> details = new BindableAttribute<>(Widget.class);


    public WidgetPresetRoomDetails(AdditionalInfoCaculatedDungeonRoomInfo roomInfo) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/roompresetview.gui"));
        this.roomInfo = roomInfo;
        this.metadata.setValue(new WidgetPresetRoomDetailsMetadata(roomInfo, this));

        AdditionalInfoCaculatedDungeonRoomInfo.RoomStateInfo stateInfo = roomInfo.getStateInfos().get(0);


        List<Widget> secrets = new ArrayList<>();

        if (!roomInfo.getUnused().isEmpty())
            secrets.add(new WidgetPresetRoomDetailsUnused(this, roomInfo));

        for (Map.Entry<String, DungeonMechanic> stringDungeonMechanicEntry : roomInfo.getDungeonRoomInfo().getMechanics().entrySet().stream().sorted(
                Comparator.<Map.Entry<String, DungeonMechanic>, Integer>comparing(a -> a.getValue() instanceof ISecret ? 0 : a.getValue() instanceof DungeonRoomDoor2 ? 2 : 1)
                        .thenComparing(a -> a.getKey())
        ).collect(Collectors.toList())) {
            if (!stateInfo.getMechanicPrecalculationMap().containsKey(stringDungeonMechanicEntry.getKey()))
                continue;
            secrets.add(new WidgetPresetRoomDetailsSecret(stringDungeonMechanicEntry.getKey(), this, roomInfo));
        }

        this.secrets.setValue(secrets);
    }

    public void setDetailsWidget(Widget widget) {
        this.details.setValue(widget);
    }
}
