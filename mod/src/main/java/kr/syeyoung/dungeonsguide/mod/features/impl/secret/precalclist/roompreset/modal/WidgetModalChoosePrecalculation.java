package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.roompreset.modal;

import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class WidgetModalChoosePrecalculation extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "list")
    public final BindableAttribute<List<Widget>> list = new BindableAttribute(WidgetList.class);
    public WidgetModalChoosePrecalculation(List<PathfindPrecalculation> options) {
        super(Identifier.parse("dungeonsguide:gui/features/precalclist/modal_choose_precalculation.gui"));

        List<Widget> optionsList = new ArrayList<>();
        for (PathfindPrecalculation option : options) {
            optionsList.add(new WidgetPrecalcOption(option));
        }
        this.list.setValue(optionsList);
    }

    @On(functionName = "cancel")
    public void cancel() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        PopupMgr.getPopupMgr(getDomElement()).closePopup(null);
    }
}
