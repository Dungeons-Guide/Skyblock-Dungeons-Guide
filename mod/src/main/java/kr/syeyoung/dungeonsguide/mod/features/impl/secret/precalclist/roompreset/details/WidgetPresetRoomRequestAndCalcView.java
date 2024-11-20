package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindResultRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetPrecalcList;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset.WidgetViewPreset;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.mechanics.WidgetPresetRoomDetailsSecretPathfindRequest;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.modal.WidgetModalChoosePrecalculation;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WidgetPresetRoomRequestAndCalcView extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "requestdetails")
    public final BindableAttribute<Widget> requestDetails = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "visible")
    public final BindableAttribute<String> visible = new BindableAttribute<>(String.class);
    @Bind(variableName = "precalculations")
    public final BindableAttribute<List<Widget>> precalculations = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "precalculationsApi")
    public final BindableAttribute<Column> precalculationsApi = new BindableAttribute(Column.class);

    private AdditionalInfoCaculatedDungeonRoomInfo roomInfo;
    private WidgetPresetRoomDetailsSecretPathfindRequest parent;
    private PathfindRequest request;

    public WidgetPresetRoomRequestAndCalcView(PathfindRequest request,
                                              AdditionalInfoCaculatedDungeonRoomInfo roomInfo,
                                              WidgetPresetRoomDetailsSecretPathfindRequest parent) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/pathfindrequestandcalcview.gui"));

        this.request = request;
        this.parent = parent;
        this.roomInfo = roomInfo;

        this.requestDetails.setValue(new WidgetPathfindRequestDetails(
                request
        ));

        this.visible.setValue(roomInfo.getRoomPreset().getParent().isEditable() ? "none" : "has");
        List<Widget> precalculations = new ArrayList<>();
        for (PathfindPrecalculation linkedResult : roomInfo.getLoaded().getOrDefault(request, Collections.emptyList())) {
            precalculations.add(new WidgetPathfindResultDetails(
                    linkedResult,
                    roomInfo.getRoomPreset().getParent().isEditable() ?
                    () -> {
                        roomInfo.getRoomPreset().removePrecalculation(linkedResult.getId());
                        update();
                    } : null
            ));
        }
        this.precalculations.setValue(precalculations);
    }

    @On(functionName = "link")
    public void link() {
        WidgetModalChoosePrecalculation choosePrecalculation = new WidgetModalChoosePrecalculation(
                PathfindResultRegistry.getINSTANCE().getsByHash(request.getHash())
                        .stream().filter(a -> !roomInfo.getRoomPreset().getPrecalculations().contains(a.getId())).collect(Collectors.toList())
        );
        PopupMgr.getPopupMgr(getDomElement())
                .openPopup(new Modal(300, 200, "Choose Precalculation", choosePrecalculation, true), this::actuallyLink);
    }
    private void actuallyLink(Object precalculation) {
        if (precalculation == null) return;
        roomInfo.getRoomPreset().addPrecalculation(((PathfindPrecalculation)precalculation).getId());
        update();
    }

    public void update() {
        if (this.precalculationsApi.getValue() != null)
            this.precalculationsApi.getValue().removeAllWidget();
        WidgetViewPreset.calculator.submit(() -> {
            try {
                this.roomInfo.rematchWithRoomPreset();

                List<Widget> precalculations = new ArrayList<>();
                for (PathfindPrecalculation linkedResult : this.roomInfo.getLoaded().getOrDefault(request, Collections.emptyList())) {
                    precalculations.add(new WidgetPathfindResultDetails(
                            linkedResult,
                            roomInfo.getRoomPreset().getParent().isEditable() ?
                                    () -> {
                                        roomInfo.getRoomPreset().removePrecalculation(linkedResult.getId());
                                        update();
                                    } : null
                    ));
                }

                Minecraft.getMinecraft().addScheduledTask(() -> {
                    this.precalculations.setValue(precalculations);
                    parent.updateStatus();
                    if (this.precalculationsApi.getValue() != null)
                        for (Widget precalculation : precalculations) {
                            this.precalculationsApi.getValue().addWidget(precalculation);
                        }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
