package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details.WidgetPresetRoomRequestAndCalcView;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class WidgetPresetRoomDetailsSecretPathfindRequest extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "whatever")
    public final BindableAttribute<String> whatever = new BindableAttribute<>(String.class);
    @Bind(variableName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);


    private PathfindRequest pathfindRequest;
    private WidgetPresetRoomDetailsSecretPFCategory parent;
    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;
    public WidgetPresetRoomDetailsSecretPathfindRequest(PathfindRequest request,
                                                        AdditionalInfoCaculatedDungeonRoomInfo dungeonRoomInfo,
                                                        WidgetPresetRoomDetailsSecretPFCategory widgetPresetRoomDetailsSecretPFCategory) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/roompresetview/pathfindrequest.gui"));
        this.pathfindRequest = request;
        this.roomInfo = dungeonRoomInfo;
        this.parent = widgetPresetRoomDetailsSecretPFCategory;

        this.whatever.setValue(pathfindRequest.getHash());

        // check if loaded?
        if (dungeonRoomInfo.getMissing().contains(request)) {
            this.color.setValue(0xFF551111);
        } else {
            if (roomInfo.getLoaded().get(pathfindRequest).size() > 1 || !roomInfo.getLoaded().get(pathfindRequest).get(0).getAlgorithmSetting().equals(pathfindRequest.getAlgorithmSetting()))
                this.color.setValue(0xFF553311);
            else
                this.color.setValue(0xFF115511);
        }
    }


    @On(functionName = "view")
    public void view() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        parent.setDetailsWidget(new WidgetPresetRoomRequestAndCalcView(pathfindRequest, roomInfo, this));
    }

    public void updateStatus() {
        if (roomInfo.getMissing().contains(pathfindRequest)) {
            this.color.setValue(0xFF551111);
        } else {
            if (roomInfo.getLoaded().get(pathfindRequest).size() > 1
                    || !roomInfo.getLoaded().get(pathfindRequest).get(0).getAlgorithmSetting().equals(pathfindRequest.getAlgorithmSetting()))
                this.color.setValue(0xFF553311);
            else
                this.color.setValue(0xFF115511);
        }
    }
}
