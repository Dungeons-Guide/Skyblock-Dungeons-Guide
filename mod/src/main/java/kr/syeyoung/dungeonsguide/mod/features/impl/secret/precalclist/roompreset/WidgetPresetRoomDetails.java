package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics.WidgetPresetRoomDetailsSecret;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics.WidgetPresetRoomDetailsUnknown;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics.WidgetPresetRoomDetailsUnused;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class WidgetPresetRoomDetails extends AnnotatedImportOnlyWidget {
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    @Bind(variableName = "metadata")
    public final BindableAttribute<Widget> metadata = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "secrets")
    public final BindableAttribute<List<Widget>> secrets = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "secretsApi")
    public final BindableAttribute<Column> secretsApi = new BindableAttribute<>(Column.class);

    @Bind(variableName = "details")
    public final BindableAttribute<Widget> details = new BindableAttribute<>(Widget.class);


    private Set<Class> secretClazz = Sets.newHashSet(
            DungeonSecretChestState.DungeonSecretChestData.class,
            DungeonSecretBatState.DungeonSecretBatData.class,
            DungeonSecretDoubleChestState.DungeonSecretDoubleChestData.class,
            DungeonSecretItemDropState.DungeonSecretItemDropData.class,
            DungeonSecretEssenceState.DungeonSecretEssenceData.class
    );

    public WidgetPresetRoomDetails(AdditionalInfoCaculatedDungeonRoomInfo roomInfo) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/roompresetview.gui"));
        this.roomInfo = roomInfo;
        this.metadata.setValue(new WidgetPresetRoomDetailsMetadata(roomInfo, this));

        AdditionalInfoCaculatedDungeonRoomInfo.RoomStateInfo stateInfo = roomInfo.getStateInfos().get(0);


        List<Widget> secrets = new ArrayList<>();

        if (!roomInfo.getMissingPrecalculation().isEmpty())
            secrets.add(new WidgetPresetRoomDetailsUnknown(this, roomInfo));

        if (!roomInfo.getUnused().isEmpty())
            secrets.add(new WidgetPresetRoomDetailsUnused(this, roomInfo));

        for (Map.Entry<String, DungeonMechanicData> stringDungeonMechanicEntry : roomInfo.getDungeonRoomInfo().getMechanics().entrySet().stream().sorted(
                Comparator.<Map.Entry<String, DungeonMechanicData>, Integer>comparing(a -> secretClazz.contains(a.getValue().getClass()) ? 0 : a.getValue() instanceof DungeonRoomDoor2State.DungeonRoomDoor2Data ? 2 : 1)
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

    public void refresh() {
        if (this.secretsApi.getValue() != null)
            this.secretsApi.getValue().removeAllWidget();

        AdditionalInfoCaculatedDungeonRoomInfo.RoomStateInfo stateInfo = roomInfo.getStateInfos().get(0);

        List<Widget> secrets = new ArrayList<>();

        if (!roomInfo.getMissingPrecalculation().isEmpty())
            secrets.add(new WidgetPresetRoomDetailsUnknown(this, roomInfo));

        if (!roomInfo.getUnused().isEmpty())
            secrets.add(new WidgetPresetRoomDetailsUnused(this, roomInfo));

        for (Map.Entry<String, DungeonMechanicData> stringDungeonMechanicEntry : roomInfo.getDungeonRoomInfo().getMechanics().entrySet().stream().sorted(
                Comparator.<Map.Entry<String, DungeonMechanicData>, Integer>comparing(a -> secretClazz.contains(a.getValue().getClass()) ? 0 : a.getValue() instanceof DungeonRoomDoor2State.DungeonRoomDoor2Data ? 2 : 1)
                        .thenComparing(a -> a.getKey())
        ).collect(Collectors.toList())) {
            if (!stateInfo.getMechanicPrecalculationMap().containsKey(stringDungeonMechanicEntry.getKey()))
                continue;
            secrets.add(new WidgetPresetRoomDetailsSecret(stringDungeonMechanicEntry.getKey(), this, roomInfo));
        }

        if (this.secretsApi.getValue() != null)
            for (Widget secret : secrets) {
                this.secretsApi.getValue().addWidget(secret);
            }
        else
            this.secrets.setValue(secrets);
    }
}
