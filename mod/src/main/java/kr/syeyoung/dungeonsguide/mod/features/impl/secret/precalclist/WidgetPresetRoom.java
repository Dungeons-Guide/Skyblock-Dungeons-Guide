package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import lombok.Getter;
import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

public class WidgetPresetRoom extends AnnotatedImportOnlyWidget {

    private PathfindPreset preset;
    @Getter
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;
    private WidgetPresetRoomList parent;

    @Bind(variableName = "roomColor")
    public final BindableAttribute<Integer> roomColor = new BindableAttribute<>(Integer.class);

    @Bind(variableName = "roomName")
    public final BindableAttribute<String> roomName = new BindableAttribute<>(String.class);

    @Bind(variableName = "abilityOverride")
    public final BindableAttribute<String> abilityOverride = new BindableAttribute<>(String.class);
    @Bind(variableName = "missingPrecalc")
    public final BindableAttribute<String> missingPrecalc = new BindableAttribute<>(String.class);
    @Bind(variableName = "redundantPrecalc")
    public final BindableAttribute<String> redundantPrecalc = new BindableAttribute<>(String.class);
    @Bind(variableName = "requiredPrecalculations")
    public final BindableAttribute<String> requiredPrecalculations = new BindableAttribute<>(String.class);
    @Bind(variableName = "loadedPrecalculations")
    public final BindableAttribute<String> loadedPrecalculations = new BindableAttribute<>(String.class);
    @Bind(variableName = "redundantPrecalculations")
    public final BindableAttribute<String> redundantPrecalculations = new BindableAttribute<>(String.class);



    public WidgetPresetRoom(AdditionalInfoCaculatedDungeonRoomInfo dungeonRoomInfo, PathfindPreset preset, WidgetPresetRoomList roomList) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/presetview/room.gui"));

        this.roomInfo = dungeonRoomInfo;
        this.preset = preset;
        this.parent = roomList;


        this.roomName.setValue(roomInfo.getRoomShape()+"-"+dungeonRoomInfo.getDungeonRoomInfo().getTotalSecrets() + " " + dungeonRoomInfo.getDungeonRoomInfo().getName());
        this.roomColor.setValue(roomInfo.getRoomColor());


        abilityOverride.setValue(dungeonRoomInfo.getRoomPreset().isOverridingParentAlgorithmSettings() ? "true" : "false");
        missingPrecalc.setValue(dungeonRoomInfo.getMissing().isEmpty() ? "false" : "true");
        redundantPrecalc.setValue((dungeonRoomInfo.getDuplicate().isEmpty() && dungeonRoomInfo.getUnused().isEmpty()) ? "false" : "true");
        requiredPrecalculations.setValue(dungeonRoomInfo.getTotalRequiredPrecalculation().size()+"");
        loadedPrecalculations.setValue(dungeonRoomInfo.getLoaded().size()+"");
        redundantPrecalculations.setValue((dungeonRoomInfo.getDuplicate().size() + dungeonRoomInfo.getUnused().size())+"");
    }

    @On(functionName = "edit")
    public void edit() {
        Navigator.getNavigator(getDomElement()).openPage(new WidgetPresetRoomDetails(roomInfo));
    }
}
