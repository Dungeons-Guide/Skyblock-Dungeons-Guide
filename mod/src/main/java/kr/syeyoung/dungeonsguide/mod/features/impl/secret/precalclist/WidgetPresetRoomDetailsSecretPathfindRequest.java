package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

public class WidgetPresetRoomDetailsSecretPathfindRequest extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "whatever")
    public final BindableAttribute<String> whatever = new BindableAttribute<>(String.class);
    @Bind(variableName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);


    private PathfindRequest pathfindRequest;
    private WidgetPresetRoomDetailsSecretPFCategory parent;
    public WidgetPresetRoomDetailsSecretPathfindRequest(PathfindRequest request,
                                                        AdditionalInfoCaculatedDungeonRoomInfo dungeonRoomInfo,
                                                        WidgetPresetRoomDetailsSecretPFCategory widgetPresetRoomDetailsSecretPFCategory) {

        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/pathfindrequest.gui"));
        this.pathfindRequest = request;
        this.parent = widgetPresetRoomDetailsSecretPFCategory;

        this.whatever.setValue(pathfindRequest.getHash());

        // check if loaded?
        if (dungeonRoomInfo.getMissing().contains(request)) {
            this.color.setValue(0xFF551111);
        } else {
            this.color.setValue(0xFF115511);
        }

    }
}
