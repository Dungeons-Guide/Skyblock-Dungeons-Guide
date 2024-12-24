package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.details;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class WidgetUnknownPathfindResultDetails extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "modifiable")
    public final BindableAttribute<String> modifiable = new BindableAttribute<>(String.class);

    @Bind(variableName = "id")
    public final BindableAttribute<String> id = new BindableAttribute<>(String.class);

    private final Runnable onDelete;
    public WidgetUnknownPathfindResultDetails(String linkedResult, Runnable onDelete) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/roompresetview/unknownpathfindprecalculationdetails.gui"));

        this.modifiable.setValue(onDelete == null ? "false" : "true");
        this.onDelete = onDelete;

        this.id.setValue(linkedResult);
    }

    @On(functionName = "unlink")
    public void unlink() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        if (this.onDelete != null)
            this.onDelete.run();
    }
}
