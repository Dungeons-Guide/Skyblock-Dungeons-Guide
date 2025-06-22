package kr.syeyoung.dungeonsguide.mod.config.onboarding;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.IFeature;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.ParserElement;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnboardingCard extends AnnotatedWidget {
    @Getter
    private OnboardingCardSettings settings;

    @Export(attributeName = "id")
    public final BindableAttribute<String> id = new BindableAttribute<>(String.class);

    @Export(attributeName = "controller")
    public final BindableAttribute<OnboardingPage> controller = new BindableAttribute<>(OnboardingPage.class);

    @Export(attributeName = "toggleGroup")
    public final BindableAttribute<String> toggleGroup = new BindableAttribute<>(String.class, "");

    @Export(attributeName = "_body")
    @Bind(variableName = "_body")
    public final BindableAttribute<Widget> body = new BindableAttribute<>(Widget.class);
    @Export(attributeName = "_setting")
    public final BindableAttribute<ParserElement> parserElementBindableAttribute = new BindableAttribute<>(ParserElement.class);



    @Export(attributeName = "selected")
    public final BindableAttribute<Boolean> selected = new BindableAttribute<>(Boolean.class, false);



    @Bind(variableName = "borderColor")
    public final BindableAttribute<Integer> border = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Integer> background = new BindableAttribute<>(Integer.class);


    public OnboardingCard() {
        super(new ResourceLocation("dungeonsguide:gui/onboarding/card.gui"));

        parserElementBindableAttribute.addOnUpdate((old, neu) -> {
            settings = new OnboardingCardSettings();
            Gson gson = new Gson();
            for (ParserElement child : neu.getChildren()) {
                if (!child.getNodeName().equals("feature")) continue;
                String featureId = child.getAttributeValue("id");
                OnboardingCardSettings.FeatureSettings featureSettings = new OnboardingCardSettings.FeatureSettings();
                featureSettings.setKey(featureId);
                featureSettings.setConfigOverride(new HashMap<>());
                for (ParserElement attribs : child.getChildren()) {
                    String key = attribs.getNodeName();
                    JsonElement elem = gson.fromJson(attribs.getBody(), JsonElement.class);

                    featureSettings.getConfigOverride().put(key, elem);
                }
                settings.getList().add(featureSettings);
            }
        });

        border.setValue(0xFF1D1D20);
        background.setValue(0xFF1D1D20);
        selected.addOnUpdate((old, neu) -> {
            if (neu) {
                border.setValue(0xFF284580);
                background.setValue(0xFF0B1120);
            } else {
                border.setValue(0xFF1D1D20);
                background.setValue(0xFF1D1D20);
            }
        });

        controller.addOnUpdate((old, neu) -> {
            if (neu != null) neu.register(id.getValue(), this);
        });
    }


    @Data
    public static class OnboardingCardSettings {
        private List<FeatureSettings> list = new ArrayList<>();


        @Data
        public static class FeatureSettings {
            private String key;
            private Map<String, JsonElement> configOverride;
        }

        public void apply() {
            for (FeatureSettings featureSettings : list) {
                IFeature feature = FeatureRegistry.getFeatureByKey(featureSettings.getKey());
                if (feature instanceof AbstractFeature) {
                    AbstractFeature feature1 = (AbstractFeature) feature;
                    for (Map.Entry<String, JsonElement> configEntry : featureSettings.configOverride.entrySet()) {
                        if (configEntry.getKey().equals("_enabled")) {
                            feature1.setEnabled(configEntry.getValue().getAsBoolean());
                        } else if (configEntry.getKey().equals("_")) {
                            feature1.loadConfig(configEntry.getValue().getAsJsonObject());
                        } else {
                            FeatureParameter featureParameter = feature1.getParameter(configEntry.getKey());
                            Object value = featureParameter.getFeatureTypeHandler().deserialize(configEntry.getValue());
                            featureParameter.setValue(value);
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("Uhoh, "+featureSettings.getKey());
                }
            }

        }
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        if (childHandled) return false;
        getDomElement().setCursor(EnumCursor.POINTING_HAND);
        return true;
    }


    @Override
    public void mouseEntered(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        // hover on
        border.setValue(0xFF284580);
        background.setValue(0xFF0B1120);
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        // unhover on
        if (selected.getValue()) { // 40 69 128
            border.setValue(0xFF284580);
            background.setValue(0xFF0B1120);
        } else {
            border.setValue(0xFF1D1D20);
            background.setValue(0xFF1D1D20);
        }
    }

    private boolean isPressed;

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (childHandled) return false;
        getDomElement().obtainFocus();
        isPressed = true;
        return true;
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (!isPressed) return;
        isPressed = false;
        if (!getDomElement().getAbsBounds().contains(absMouseX, absMouseY)) return;
        // do stuff
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        if (selected.getValue())
            controller.getValue().unselect(id.getValue());
        else
            controller.getValue().select(id.getValue());
    }

}
