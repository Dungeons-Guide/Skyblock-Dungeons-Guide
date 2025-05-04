package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq;

import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.PathfindPresetRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WidgetPrecalcReqSetMetadata extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "precalcReqs")
    public final BindableAttribute<String> precalcReqs = new BindableAttribute<>(String.class);

    @Bind(variableName = "requiredCredits")
    public final BindableAttribute<String> requiredCredits = new BindableAttribute<>(String.class);

    @Bind(variableName = "rooms")
    public final BindableAttribute<String> rooms = new BindableAttribute<>(String.class);

    @Bind(variableName = "presetName")
    public final BindableAttribute<String> presetName = new BindableAttribute<>(String.class);

    public WidgetPrecalcReqSetMetadata(PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/metadata.gui"));


        precalcReqs.setValue(requestSet.getRequestList().size()+"");
        requiredCredits.setValue(requestSet.getCredits()+"");
        Set<UUID> uuids = new HashSet<>();
        for (PathfindRequest pathfindRequest : requestSet.getRequestList()) {
            uuids.add(pathfindRequest.getDungeonRoomInfo().getUuid());
        }
        rooms.setValue(uuids.size()+" rooms");

        PathfindPreset preset = PathfindPresetRegistry.getINSTANCE().getPreset(requestSet.getLinkedPreset());
        presetName.setValue(preset == null ? "Unknown" : preset.getPresetName());
    }
}
