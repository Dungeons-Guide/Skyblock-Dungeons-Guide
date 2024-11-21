package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset.WidgetViewPreset;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details.WidgetPathfindResultDetails;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details.WidgetUnknownPathfindResultDetails;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class WidgetPresetRoomDetailsSecretUnknownPrecalculation extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "whatever")
    public final BindableAttribute<String> whatever = new BindableAttribute<>(String.class);
    @Bind(variableName = "color")
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);


    private String pathfindPrecalculation;
    private WidgetPresetRoomDetailsUnknown parent;
    private AdditionalInfoCaculatedDungeonRoomInfo info;
    public WidgetPresetRoomDetailsSecretUnknownPrecalculation(String calculation,
                                                              AdditionalInfoCaculatedDungeonRoomInfo dungeonRoomInfo,
                                                              WidgetPresetRoomDetailsUnknown widgetPresetRoomDetailsSecret) {

        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/pathfindrequest.gui"));
        this.pathfindPrecalculation = calculation;
        this.parent = widgetPresetRoomDetailsSecret;
        this.info = dungeonRoomInfo;

        this.whatever.setValue(calculation);

        // check if loaded?
        this.color.setValue(0xFF553311);

    }


    @On(functionName = "view")
    public void view() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        parent.setDetailsWidget(new WidgetUnknownPathfindResultDetails(pathfindPrecalculation,
                info.getRoomPreset().getParent().isEditable() ?
                () -> {
            this.info.getRoomPreset().removePrecalculation(pathfindPrecalculation);
            parent.setDetailsWidget(null);
            parent.remove(this);

            WidgetViewPreset.calculator.submit(() -> {
                info.rematchWithRoomPreset();
            });
        } : null));
    }
}
