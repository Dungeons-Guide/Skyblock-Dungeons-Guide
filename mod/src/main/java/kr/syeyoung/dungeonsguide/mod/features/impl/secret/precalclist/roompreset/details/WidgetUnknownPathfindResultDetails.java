package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class WidgetUnknownPathfindResultDetails extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "modifiable")
    public final BindableAttribute<String> modifiable = new BindableAttribute<>(String.class);

    @Bind(variableName = "id")
    public final BindableAttribute<String> id = new BindableAttribute<>(String.class);

    private final Runnable onDelete;
    public WidgetUnknownPathfindResultDetails(String linkedResult, Runnable onDelete) {
        super(Identifier.parse("dungeonsguide:gui/features/precalclist/roompresetview/unknownpathfindprecalculationdetails.gui"));

        this.modifiable.setValue(onDelete == null ? "false" : "true");
        this.onDelete = onDelete;

        this.id.setValue(linkedResult);
    }

    @On(functionName = "unlink")
    public void unlink() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        if (this.onDelete != null)
            this.onDelete.run();
    }
}
