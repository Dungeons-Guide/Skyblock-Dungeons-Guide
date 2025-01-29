package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.pathfindcache.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.WidgetPresetRoomDetails;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
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
    @Bind(variableName = "unusedPrecalc")
    public final BindableAttribute<String> unusedPrecalc = new BindableAttribute<>(String.class);
    @Bind(variableName = "warning")
    public final BindableAttribute<String> warning = new BindableAttribute<>(String.class);
    @Bind(variableName = "requiredPrecalculations")
    public final BindableAttribute<String> requiredPrecalculations = new BindableAttribute<>(String.class);
    @Bind(variableName = "loadedPrecalculations")
    public final BindableAttribute<String> loadedPrecalculations = new BindableAttribute<>(String.class);
    @Bind(variableName = "unusedPrecalculations")
    public final BindableAttribute<String> unusedPrecalculations = new BindableAttribute<>(String.class);
    @Bind(variableName = "missingPrecalculations")
    public final BindableAttribute<String> missingPrecalculations = new BindableAttribute<>(String.class);
    @Bind(variableName = "unknownPrecalc")
    public final BindableAttribute<String> unknownPrecalc = new BindableAttribute<>(String.class);


    public WidgetPresetRoom(AdditionalInfoCaculatedDungeonRoomInfo dungeonRoomInfo, PathfindPreset preset, WidgetPresetRoomList roomList) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/presetview/room.gui"));

        this.roomInfo = dungeonRoomInfo;
        this.preset = preset;
        this.parent = roomList;


        this.roomName.setValue(roomInfo.getRoomShape()+"-"+dungeonRoomInfo.getDungeonRoomInfo().getTotalSecrets() + " " + dungeonRoomInfo.getDungeonRoomInfo().getName());
        this.roomColor.setValue(roomInfo.getRoomColor());


        abilityOverride.setValue(dungeonRoomInfo.getRoomPreset().isOverridingParentAlgorithmSetting() ? "true" : "false");
        missingPrecalc.setValue(dungeonRoomInfo.getMissing().isEmpty() ? "false" : "true");
        unusedPrecalc.setValue((dungeonRoomInfo.getDuplicate().isEmpty() && dungeonRoomInfo.getUnused().isEmpty()) ? "false" : "true");
        unknownPrecalc.setValue(dungeonRoomInfo.getMissingPrecalculation().isEmpty() ? "false" : "true");
        warning.setValue(dungeonRoomInfo.getWarnings() > 0 ? "true" : "false");

        requiredPrecalculations.setValue(dungeonRoomInfo.getTotalRequiredPrecalculation().size()+"");
        loadedPrecalculations.setValue(dungeonRoomInfo.getLoaded().size()+"");
        unusedPrecalculations.setValue((dungeonRoomInfo.getDuplicate().size() + dungeonRoomInfo.getUnused().size())+"");
        missingPrecalculations.setValue(dungeonRoomInfo.getMissingPrecalculation().size() + "");
    }

    @On(functionName = "edit")
    public void edit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Navigator.getNavigator(getDomElement()).openPage(new WidgetPresetRoomDetails(roomInfo));
    }
}
