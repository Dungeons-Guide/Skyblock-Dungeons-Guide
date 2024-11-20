package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

public class WidgetPresetRoomDetailsSecretPrecalculation extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "whatever")
    public final BindableAttribute<String> whatever = new BindableAttribute<>(String.class);
    @Bind(variableName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);


    private PathfindPrecalculation pathfindPrecalculation;
    private WidgetPresetRoomDetailsRedundant parent;
    public WidgetPresetRoomDetailsSecretPrecalculation(PathfindPrecalculation calculation,
                                                       AdditionalInfoCaculatedDungeonRoomInfo dungeonRoomInfo,
                                                       WidgetPresetRoomDetailsRedundant widgetPresetRoomDetailsSecret) {

        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/pathfindrequest.gui"));
        this.pathfindPrecalculation = calculation;
        this.parent = widgetPresetRoomDetailsSecret;

        this.whatever.setValue(calculation.getTargetHash());

        // check if loaded?
        this.color.setValue(0xFF553311);

    }
}
