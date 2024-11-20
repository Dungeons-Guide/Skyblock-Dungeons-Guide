package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details.WidgetPresetRoomRequestAndCalcView;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

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
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/pathfindrequest.gui"));
        this.pathfindRequest = request;
        this.roomInfo = dungeonRoomInfo;
        this.parent = widgetPresetRoomDetailsSecretPFCategory;

        this.whatever.setValue(pathfindRequest.getHash());

        // check if loaded?
        if (dungeonRoomInfo.getMissing().contains(request)) {
            this.color.setValue(0xFF551111);
        } else {
            if (dungeonRoomInfo.getLoaded().get(request).size() > 1)
                this.color.setValue(0xFF335511);
            else
                this.color.setValue(0xFF115511);
        }
    }


    @On(functionName = "view")
    public void view() {
        parent.setDetailsWidget(new WidgetPresetRoomRequestAndCalcView(pathfindRequest, roomInfo, this));
    }

    public void updateStatus() {
        if (roomInfo.getMissing().contains(pathfindRequest)) {
            this.color.setValue(0xFF551111);
        } else {
            if (roomInfo.getLoaded().get(pathfindRequest).size() > 1)
                this.color.setValue(0xFF335511);
            else
                this.color.setValue(0xFF115511);
        }
    }
}
