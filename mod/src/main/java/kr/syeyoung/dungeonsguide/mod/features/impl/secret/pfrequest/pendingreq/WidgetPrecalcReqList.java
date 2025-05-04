package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.pendingreq;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest.PathfindPrecalculationRequestSet;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WidgetPrecalcReqList extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "list")
    public final BindableAttribute<List<Widget>> widgetList = new BindableAttribute(WidgetList.class);

    private PathfindPrecalculationRequestSet requestSet;
    public WidgetPrecalcReqList(PathfindPrecalculationRequestSet requestSet) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/list.gui"));
        this.requestSet = requestSet;

        List<Widget> widgets=  new ArrayList<>();
        for (PathfindRequest pathfindRequest : requestSet.getRequestList()) {
            widgets.add(new WidgetPrecalcReqListElement(pathfindRequest));
        }
        this.widgetList.setValue(widgets);
    }

    public static class WidgetPrecalcReqListElement extends  AnnotatedImportOnlyWidget {
        @Bind(variableName = "hash")
        public final BindableAttribute<String> hash = new BindableAttribute<>(String.class);
        @Bind(variableName = "roomName")
        public final BindableAttribute<String> roomName = new BindableAttribute<>(String.class);
        @Bind(variableName = "roomState")
        public final BindableAttribute<String> roomState = new BindableAttribute<>(String.class);
        @Bind(variableName = "credits")
        public final BindableAttribute<String> credits = new BindableAttribute<>(String.class);


        public WidgetPrecalcReqListElement(PathfindRequest pathfindRequest) {
            super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/pendingreq/precalcreqelement.gui"));

            this.hash.setValue(pathfindRequest.getHash());
            this.roomName.setValue(pathfindRequest.getDungeonRoomInfo().getName());
            this.roomState.setValue(pathfindRequest.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(",")));

            DungeonRoomInfo dri = pathfindRequest.getDungeonRoomInfo();
            int bitCount = dri.getWidth() * dri.getLength() / 1024;

            this.credits.setValue(bitCount+"");
        }
    }
}
