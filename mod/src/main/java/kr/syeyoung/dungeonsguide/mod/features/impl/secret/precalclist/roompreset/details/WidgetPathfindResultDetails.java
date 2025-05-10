package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class WidgetPathfindResultDetails extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "modifiable")
    public final BindableAttribute<String> modifiable = new BindableAttribute<>(String.class);

    @Bind(variableName = "id")
    public final BindableAttribute<String> id = new BindableAttribute<>(String.class);
    @Bind(variableName = "generatedFrom")
    public final BindableAttribute<String> generatedFrom = new BindableAttribute<>(String.class);
    @Bind(variableName = "storedAt")
    public final BindableAttribute<String> storedAt = new BindableAttribute<>(String.class);
    @Bind(variableName = "algorithmSetting")
    public final BindableAttribute<Widget> algorithmSetting = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "targetHash")
    public final BindableAttribute<String> targetHash = new BindableAttribute<>(String.class);
    @Bind(variableName = "targetId")
    public final BindableAttribute<String> targetId = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomUID")
    public final BindableAttribute<String> roomUID = new BindableAttribute<>(String.class);
    @Bind(variableName = "roomState")
    public final BindableAttribute<String> roomState = new BindableAttribute<>(String.class);

    private final BindableAttribute<AlgorithmSetting> algorithmSettingBindableAttribute = new BindableAttribute<>(AlgorithmSetting.class);

    private final Runnable onDelete;
    private PathfindPrecalculation linked;
    public WidgetPathfindResultDetails(PathfindPrecalculation linkedResult, Runnable onDelete) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/pathfindprecalculationdetails.gui"));

        this.modifiable.setValue(onDelete == null ? "false" : "true");
        this.onDelete = onDelete;
        this.linked = linkedResult;

        this.id.setValue(linkedResult.getId());
        this.generatedFrom.setValue(linkedResult.getGeneratedFrom());
        this.storedAt.setValue(linkedResult.getFile());
        this.algorithmSettingBindableAttribute.setValue(linkedResult.getAlgorithmSetting());
        this.algorithmSetting.setValue(new WidgetAbilitySettings(this.algorithmSettingBindableAttribute));
        this.targetHash.setValue(linkedResult.getTargetHash());
        this.targetId.setValue(linkedResult.getTargetId().substring(36, Math.min(66, linkedResult.getTargetId().length())));
        this.roomUID.setValue(linkedResult.getRoomUID().toString());
        this.roomState.setValue(linkedResult.getRoomState());
    }

    @On(functionName = "unlink")
    public void unlink() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        if (this.onDelete != null)
            this.onDelete.run();
    }

    @On(functionName = "copyId")
    public void copyId() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        StringSelection selection = new StringSelection(linked.getTargetId());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    @On(functionName = "view")
    public void view() {
        FeatureRegistry.DEBUG_PFRES.precalcs.add(linked);
    }
}
