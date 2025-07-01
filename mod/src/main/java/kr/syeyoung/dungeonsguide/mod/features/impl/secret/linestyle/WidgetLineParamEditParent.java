/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle;

import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.PathfindLineProperties;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class WidgetLineParamEditParent extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "parentToggle")
    public final BindableAttribute<String> parentToggle = new BindableAttribute<>(String.class, "true");

    @Bind(variableName = "globalToggle")
    public final BindableAttribute<String> globalToggle = new BindableAttribute<>(String.class, "true");

    @Bind(variableName = "useParent")
    public final BindableAttribute<Widget> useParent = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "editor")
    public final BindableAttribute<Widget> editor2 = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "pathDisplayEngine")
    public final BindableAttribute<Widget> pathDisplayEngine = new BindableAttribute<>(Widget.class);



    private <T> Widget generateConfigWidget(PathfindLineProperties lineProperties, String key, Function<FeatureParameter<T>, Widget> converter) {
        FeatureParameter<T> featureParameter = lineProperties.getParameter(key);
        return converter.apply(featureParameter);
    }

    private WidgetLinePropertiesEditor editor;
    public WidgetLineParamEditParent(WidgetLinePropertiesEditor editor, PathfindLineProperties lineProperties) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/lineProperties/paramEditor.gui"));
        this.editor = editor;

        if (lineProperties.getParent() == null || !lineProperties.isGlobal()) {
            parentToggle.setValue("true");
        } else {
            parentToggle.setValue("false");
        }
        globalToggle.setValue(lineProperties.getParent() != null ? "true" : "false");

        if (lineProperties.getParent() != null) {
            useParent.setValue(generateConfigWidget(lineProperties, "useGlobal", TCBoolean.BooleanEditWidget::new));

            ((TCBoolean.BooleanEditWidget)useParent.getValue()).isEnabled.addOnUpdate((old, neu) -> {
                parentToggle.setValue(neu ? "false" : "true");
                if (!neu) {
                    editor2.setValue(lineProperties.getSetting().createSettingWidget());
                    editor.updatePreview();
                } else {
                    editor2.setValue(null);
                    editor.updatePreview();
                }
            });
        }

        if (lineProperties.getParent() == null || !lineProperties.isGlobal())
            editor2.setValue(lineProperties.getSetting().createSettingWidget());
        List<PathDisplayEngineSettingRegistration<?>> registrationList = new ArrayList<>(PathDisplayEngineSettingRegistry.getRegistrationList().values());
        pathDisplayEngine.setValue(new CycleWidget(
                registrationList, registrationList.indexOf(lineProperties.getSetting().getRegistration()),
                (t) -> {
                    PathDisplayEngineSetting setting = lineProperties.getOldSetting(t.getJsonName());
                    if (setting == null) setting = t.createConfiguration();

                    lineProperties.setSetting(setting);
                    if (!lineProperties.isGlobal() && lineProperties.getParent() == null)
                        editor2.setValue(lineProperties.getSetting().createSettingWidget());
                    editor.updatePreview();
                }
        ));
    }


    public static class CycleWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "value")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);
        private List<PathDisplayEngineSettingRegistration<?>> values;
        private int idx;
        private Consumer<PathDisplayEngineSettingRegistration<?>> onUpdate;
        public CycleWidget(List<PathDisplayEngineSettingRegistration<?>> values, int defaultIndex, Consumer<PathDisplayEngineSettingRegistration<?>> onUpdate) {
            super(new ResourceIdentifier("dungeonsguide:gui/config/parameter/stringChoice.gui"));
            this.idx = defaultIndex;
            this.values = values;
            this.onUpdate = onUpdate;
            value.setValue(values.get(idx).getName());
        }

        @On(functionName = "inc")
        public void inc() {
            ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
            idx = (idx + 1) % values.size();
            value.setValue(values.get(idx).getName());
            onUpdate.accept(values.get(idx));
        }
        @On(functionName = "dec")
        public void dec() {
            ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
            idx = (values.size() + idx - 1) % values.size();
            value.setValue(values.get(idx).getName());
            onUpdate.accept(values.get(idx));
        }
    }
}
