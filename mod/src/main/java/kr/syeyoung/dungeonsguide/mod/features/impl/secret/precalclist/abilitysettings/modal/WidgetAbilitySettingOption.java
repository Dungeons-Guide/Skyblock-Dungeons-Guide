package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.modal;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetCreateAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSettingRegistry;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class WidgetAbilitySettingOption extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "ability")
    public final BindableAttribute<Widget> widgetBindableAttribute = new BindableAttribute<>(Widget.class);

    AlgorithmSetting algorithmSetting;

    private WidgetModalChooseAbilitySettings parent;
    public WidgetAbilitySettingOption(AlgorithmSetting algorithmSetting, WidgetModalChooseAbilitySettings parent) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/precalclist/abilityedit/choose_ability_dummy.gui"));

        this.algorithmSetting = algorithmSetting;
        this.widgetBindableAttribute.setValue(new WidgetAbilitySettings(
                new BindableAttribute<>(AlgorithmSetting.class, algorithmSetting)
        ));
        this.parent = parent;
    }

    @On(functionName = "select")
    public void select() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        PopupMgr.getPopupMgr(getDomElement()).closePopup(algorithmSetting);
    }


    @On(functionName = "derive")
    public void create() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);

        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(500, 500, "Create New Algorithm Setting", new WidgetCreateAbilitySettings(algorithmSetting), true), (a) -> {
            if (a != null) {
                AlgorithmSettingRegistry.registerAlgorithmSetting((AlgorithmSetting) a);
                this.parent.onNew((AlgorithmSetting) a);
            }
        });
    }
}
