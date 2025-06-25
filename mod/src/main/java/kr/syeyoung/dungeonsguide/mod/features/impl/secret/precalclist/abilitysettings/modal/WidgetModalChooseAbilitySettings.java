package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.modal;

import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetCreateAbilitySettings;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.Modal;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSettingRegistry;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetModalChooseAbilitySettings extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "list")
    public final BindableAttribute<List<Widget>> list = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute<>(Column.class);
    public WidgetModalChooseAbilitySettings() {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/abilityedit/modal_choose_algorithm.gui"));

        List<Widget> optionsList = new ArrayList<>();
        for (AlgorithmSetting option : AlgorithmSettingRegistry.getAlgorithmSettings()) {
            optionsList.add(new WidgetAbilitySettingOption(option, this));
        }
        this.list.setValue(optionsList);
    }

    @On(functionName = "createNew")
    public void create() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);


        PopupMgr.getPopupMgr(getDomElement()).openPopup(new Modal(500, 400, "Create New Algorithm Setting", new WidgetCreateAbilitySettings(AlgorithmSettingRegistry.STANDARD_DEFAULT_ALGORITHM_SETTING), true), (a) -> {
            if (a != null) {
                AlgorithmSettingRegistry.registerAlgorithmSetting((AlgorithmSetting) a);
                api.getValue().addWidget(new WidgetAbilitySettingOption((AlgorithmSetting) a, this));
            }
        });
    }

    public void onNew(AlgorithmSetting algorithmSetting) {
        api.getValue().addWidget(new WidgetAbilitySettingOption(algorithmSetting, this));
    }

    @On(functionName = "cancel")
    public void cancel() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        PopupMgr.getPopupMgr(getDomElement()).closePopup(null);
    }
}
