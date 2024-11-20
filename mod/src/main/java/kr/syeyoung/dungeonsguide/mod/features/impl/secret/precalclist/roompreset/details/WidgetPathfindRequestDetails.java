package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.AlgorithmSettings;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.stream.Collectors;

public class WidgetPathfindRequestDetails extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "id")
    public final BindableAttribute<String> id = new BindableAttribute<>(String.class);
    @Bind(variableName = "hash")
    public final BindableAttribute<String> hash = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomState")
    public final BindableAttribute<String> roomState = new BindableAttribute<>(String.class);

    @Bind(variableName = "algorithmSettings")
    public final BindableAttribute<Widget> algorithmSettings = new BindableAttribute<>(Widget.class);


    private PathfindRequest request;
    private BindableAttribute<AlgorithmSettings> algorithmSettingsBindableAttribute = new BindableAttribute<>(AlgorithmSettings.class);
    public WidgetPathfindRequestDetails(PathfindRequest request) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/pathfindrequestdetails.gui"));
        this.request = request;
        this.id.setValue(request.getId().substring(36, Math.min(request.getId().length(), 66)));
        this.hash.setValue(request.getHash());
        String mech = request.getOpenMech().stream().sorted(String::compareTo).collect(Collectors.joining(","));
        this.roomState.setValue(mech.isEmpty() ? "(empty)" : mech);
        this.algorithmSettingsBindableAttribute.setValue(request.getAlgorithmSettings());
        this.algorithmSettings.setValue(new WidgetAbilitySettings(this.algorithmSettingsBindableAttribute));
    }

    @On(functionName = "copyId")
    public void copyId() {
        StringSelection selection = new StringSelection(request.getId());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
