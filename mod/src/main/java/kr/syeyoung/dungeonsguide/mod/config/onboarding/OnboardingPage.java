package kr.syeyoung.dungeonsguide.mod.config.onboarding;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class OnboardingPage extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "prevPage")
    public final BindableAttribute<String> prevPage = new BindableAttribute<>(String.class);
    @Bind(variableName = "nextPage")
    public final BindableAttribute<String> nextPage = new BindableAttribute<>(String.class);

    @Bind(variableName = "nextButtonDisabled")
    public final BindableAttribute<Boolean> nextBtnDisable = new BindableAttribute<>(Boolean.class);

    @Bind(variableName = "requiredGroup")
    public final BindableAttribute<String> requiredGroup = new BindableAttribute<>(String.class);

    @Bind(variableName = "controller")
    public final BindableAttribute<OnboardingPage> self = new BindableAttribute<>(OnboardingPage.class, this);

    public OnboardingPage(String name) {
        super(new ResourceLocation("dungeonsguide:gui/onboarding/"+name));

        requiredGroup.addOnUpdate((old, neu) -> check());
    }


    private Map<String, OnboardingCard> registered = new HashMap<>();
    private Map<String, String> idToToggleGroup = new HashMap<>();
    private Map<String, String> byToggleGroup = new HashMap<>();
    private Map<String, BindableAttribute<Boolean>> chosen = new HashMap<>();

    public void register(String id, OnboardingCard card) {
        registered.put(id, card);
        if (!card.toggleGroup.getValue().isEmpty())
            idToToggleGroup.put(id, card.toggleGroup.getValue());

        chosen.computeIfAbsent(id, (a) -> new BindableAttribute<>(Boolean.class));
        chosen.get(id).exportTo(card.selected);

        if (card.selected.getValue() == Boolean.TRUE)
            select(card.id.getValue());
    }

    public void select(String id) {
        String toggleGroup = idToToggleGroup.get(id);
        if (toggleGroup != null) {
            if (byToggleGroup.get(toggleGroup) != null) {
                String prev = byToggleGroup.get(toggleGroup);
                chosen.get(prev).setValue(false);
            }
        }

        chosen.get(id).setValue(true);
        byToggleGroup.put(toggleGroup, id);

        check();
    }

    public void unselect(String id) {
        String toggleGroup = idToToggleGroup.get(id);
        if (toggleGroup != null) {
            if (byToggleGroup.get(toggleGroup) != null) {
                String prev = byToggleGroup.get(toggleGroup);
                chosen.get(prev).setValue(false);
            }
        }

        chosen.get(id).setValue(false);
        byToggleGroup.put(toggleGroup, null);

        check();
    }

    public void check() {
        boolean check = true;
        if (requiredGroup.getValue() != null) {
            String[] groups = requiredGroup.getValue().split(";");
            for (String group : groups) {
                if (group.isEmpty()) continue;
                if (byToggleGroup.get(group) == null) check = false;
            }
        }

        nextBtnDisable.setValue(!check);
    }

    @On(functionName = "prev")
    public void prev() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        if (prevPage.getValue() == null) return;
        GuiScreen parent = getDomElement().getContext().getValue(GuiScreenAdapter.class, "screenAdapter").getParent();
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenAdapter(new GlobalHUDScale(new OnboardingPage(prevPage.getValue())), parent, false));
    }

    @On(functionName = "next")
    public void next() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreen parent = getDomElement().getContext().getValue(GuiScreenAdapter.class, "screenAdapter").getParent();
        // apply settings.

        if (registered.get("$default") != null) {
            registered.get("$default").getSettings().apply();
        }

        for (Map.Entry<String, BindableAttribute<Boolean>> stringBindableAttributeEntry : chosen.entrySet()) {
            if (!stringBindableAttributeEntry.getValue().getValue()) continue;
            String key = stringBindableAttributeEntry.getKey();
            OnboardingCard card = registered.get(key);
            card.getSettings().apply();;
        }


        if (nextPage.getValue() == null) return;
        if (nextPage.getValue().equals("quit")) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenAdapter(new GlobalHUDScale(new OnboardingPage(nextPage.getValue())), parent, false));
    }
}
