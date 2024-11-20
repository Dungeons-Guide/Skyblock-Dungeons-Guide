package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.stream.Collectors;

public class WidgetPresetRoomDetailsMetadata extends AnnotatedImportOnlyWidget {
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;

    private WidgetPresetRoomDetails details;

    @Bind(variableName = "roomName")
    public final BindableAttribute<String> roomName = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomUID")
    public final BindableAttribute<String> roomUID = new BindableAttribute<>(String.class);
    @Bind(variableName = "secretCount")
    public final BindableAttribute<String> secretCount = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomShape")
    public final BindableAttribute<String> roomShape = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomType")
    public final BindableAttribute<String> roomType = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomStateCount")
    public final BindableAttribute<String> roomStateCount = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomStates")
    public final BindableAttribute<String> roomStates = new BindableAttribute<>(String.class);

    public WidgetPresetRoomDetailsMetadata(AdditionalInfoCaculatedDungeonRoomInfo roomInfo, WidgetPresetRoomDetails parent) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/metadata.gui"));
        this.roomInfo = roomInfo;
        this.details = parent;

        this.roomName.setValue(roomInfo.getDungeonRoomInfo().getName());
        this.roomUID.setValue(roomInfo.getDungeonRoomInfo().getUuid().toString());
        this.secretCount.setValue(roomInfo.getDungeonRoomInfo().getTotalSecrets()+"");
        this.roomShape.setValue(roomInfo.getRoomShape());
        this.roomType.setValue(roomInfo.getRoomType());
        this.roomStateCount.setValue(roomInfo.getStateInfos().size()+"");
        this.roomStates.setValue(
                roomInfo.getStateInfos().stream()
                        .map(AdditionalInfoCaculatedDungeonRoomInfo.RoomStateInfo::getStateIdentifier)
                        .map(a -> a.isEmpty() ? "(empty)" : a).collect(Collectors.joining(" | ")));
    }
}
