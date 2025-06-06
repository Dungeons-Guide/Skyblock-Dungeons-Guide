package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings;

import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.function.Predicate;

public class WidgetCreateAbilitySettings extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "txtEtherwarp")
    public final BindableAttribute<String> _etherwarp = new BindableAttribute<>(String.class);


    @Bind(variableName = "pickaxeButton")
    public final BindableAttribute<DomElement> pickaxeButton = new BindableAttribute<>(DomElement.class);
    @Bind(variableName = "shovelButton")
    public final BindableAttribute<DomElement> shovelButton = new BindableAttribute<>(DomElement.class);
    @Bind(variableName = "axeButton")
    public final BindableAttribute<DomElement> axeButton = new BindableAttribute<>(DomElement.class);
    @Bind(variableName = "hasteButton")
    public final BindableAttribute<DomElement> hasteButton = new BindableAttribute<>(DomElement.class);

    @Bind(variableName = "pickaxeIndex")
    public final BindableAttribute<Integer> pickaxeIndex = new BindableAttribute<>(Integer.class, 3);
    @Bind(variableName = "shovelIndex")
    public final BindableAttribute<Integer> shovelIndex = new BindableAttribute<>(Integer.class, 4);
    @Bind(variableName = "axeIndex")
    public final BindableAttribute<Integer> axeIndex = new BindableAttribute<>(Integer.class, 5);



    @Bind(variableName = "pickaxeEfficiency")
    public final BindableAttribute<String> pickaxeEfficiency = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "shovelEfficiency")
    public final BindableAttribute<String> shovelEfficiency = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "axeEfficiency")
    public final BindableAttribute<String> axeEfficiency = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "hasteLevel")
    public final BindableAttribute<String> hasteLevel = new BindableAttribute<>(String.class, "X");


    @Bind(variableName = "txtMaxEtherwarp")
    public final BindableAttribute<String> txtMaxEtherwarp = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "txtEtherwarpOffset")
    public final BindableAttribute<String> txtEtherwarpOffset = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "txtEtherwarpLeeway")
    public final BindableAttribute<String> txtEtherwarpLeeway = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "maxEtherwarp")
    public final BindableAttribute<Integer> maxEtherwarp = new BindableAttribute<>(Integer.class, 0);
    @Bind(variableName = "etherwarpOffset")
    public final BindableAttribute<Double> etherwarpOffset = new BindableAttribute<>(Double.class, 0.4);
    @Bind(variableName = "etherwarpLeeway")
    public final BindableAttribute<Double> etherwarpLeeway = new BindableAttribute<>(Double.class, 0.0625);


    @Bind(variableName = "etherwarp")
    public final BindableAttribute<Boolean> etherwarp = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "stair")
    public final BindableAttribute<Boolean> stair = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "teleportdown")
    public final BindableAttribute<Boolean> teleportdown = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "enderchest")
    public final BindableAttribute<Boolean> enderchest = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "tntpearl")
    public final BindableAttribute<Boolean> tntpearl = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "enderpearl")
    public final BindableAttribute<Boolean> enderpearl = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "stonkLength")
    public final BindableAttribute<String> stonkLength = new BindableAttribute<>(String.class);
    @Bind(variableName = "slowstonk")
    public final BindableAttribute<Boolean> slowstonk = new BindableAttribute<>(Boolean.class);


    @Bind(variableName = "offsetValidator")
    public final BindableAttribute offsetValidator = new BindableAttribute(Predicate.class, (Predicate<String>) val -> {
        double dbl = Double.parseDouble(val);
        return 0 <= dbl && dbl <= 0.5;
    });
    @Bind(variableName = "etherwarpValidator")
    public final BindableAttribute etherwarpValidator = new BindableAttribute(Predicate.class, (Predicate<String>) val -> {
        int dbl = Integer.parseInt(val);
        return 0 < dbl && dbl <= 99;
    });

    public final BindableAttribute<AlgorithmSetting.ToolSettings> pickaxeSettings = new BindableAttribute<>(AlgorithmSetting.ToolSettings.class);
    public final BindableAttribute<AlgorithmSetting.ToolSettings> shovelSettings = new BindableAttribute<>(AlgorithmSetting.ToolSettings.class);
    public final BindableAttribute<AlgorithmSetting.ToolSettings> axeSettings = new BindableAttribute<>(AlgorithmSetting.ToolSettings.class);
    public final BindableAttribute<Integer> hasteSettings = new BindableAttribute<>(Integer.class, 0);

    public WidgetCreateAbilitySettings(AlgorithmSetting defaultAlgorithm) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/abilityedit/abilitycreate.gui"));

        pickaxeSettings.addOnUpdate((old ,neu) -> {
            pickaxeIndex.setValue(neu == null ? 3 : neu.getTool().getToolMaterial().ordinal() * 8);
            pickaxeEfficiency.setValue(neu == null ? "" : "Efficiency Level: "+neu.getEfficiency());
        });
        shovelSettings.addOnUpdate((old ,neu) -> {
            shovelIndex.setValue(neu == null ? 4 : neu.getTool().getToolMaterial().ordinal() * 8 + 1);
            shovelEfficiency.setValue(neu == null ? "" : "Efficiency Level: "+neu.getEfficiency());
        });
        axeSettings.addOnUpdate((old ,neu) -> {
            axeIndex.setValue(neu == null ? 5 : neu.getTool().getToolMaterial().ordinal() * 8 + 2);
            axeEfficiency.setValue(neu == null ? "" : "Efficiency Level: "+neu.getEfficiency());
        });

        hasteSettings.addOnUpdate((old, neu) -> {
            hasteLevel.setValue(neu == 0 ? "X" : String.valueOf(neu));
        });

        txtEtherwarpLeeway.addOnUpdate((old, neu) -> {
            etherwarpLeeway.setValue(Double.parseDouble(neu));
        });
        txtEtherwarpOffset.addOnUpdate((old, neu) -> {
            etherwarpOffset.setValue(Double.parseDouble(neu));
        });
        txtMaxEtherwarp.addOnUpdate((old, neu) -> {
            maxEtherwarp.setValue(Integer.parseInt(neu));
        });

        etherwarp.setValue(defaultAlgorithm.isRouteEtherwarp());
        stair.setValue(defaultAlgorithm.isStonkDown());
        teleportdown.setValue(defaultAlgorithm.isStonkTeleport());
        enderchest.setValue(defaultAlgorithm.isStonkEChest());
        tntpearl.setValue(defaultAlgorithm.isTntpearl());
        enderpearl.setValue(defaultAlgorithm.isEnderpearl());
        stonkLength.setValue(defaultAlgorithm.getMaxStonk()+"");
        pickaxeSettings.setValue(defaultAlgorithm.getPickaxe());
        shovelSettings.setValue(defaultAlgorithm.getShovel());
        axeSettings.setValue(defaultAlgorithm.getAxe());
        hasteSettings.setValue(defaultAlgorithm.getHasteLevel());

        txtMaxEtherwarp.setValue(String.valueOf(defaultAlgorithm.getEtherwarpRadius()));
        txtEtherwarpOffset.setValue(String.valueOf(defaultAlgorithm.getEtherwarpOffset()));
        txtEtherwarpLeeway.setValue(String.valueOf(defaultAlgorithm.getEtherwarpLeeway()));

        etherwarp.addOnUpdate((old, neu) -> Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)));
        stair.addOnUpdate((old, neu) -> Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)));
        teleportdown.addOnUpdate((old, neu) -> Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)));
        enderchest.addOnUpdate((old, neu) -> Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)));
        tntpearl.addOnUpdate((old, neu) -> Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)));
        enderpearl.addOnUpdate((old, neu) -> Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)));
        slowstonk.addOnUpdate((old, neu) -> Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F)));
    }


    @On(functionName = "pickaxeEdit")
    public void pickaxeEdit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Rect rect = pickaxeButton.getValue().getAbsBounds();
        AbsLocationPopup absLocationPopup = new AbsLocationPopup(
                rect.getX(), rect.getY()+rect.getHeight(), new WidgetToolEdit(WidgetToolEdit.ToolType.PICKAXE, pickaxeSettings), true
        );
        absLocationPopup.cursorPassthrough = true;
        PopupMgr.getPopupMgr(getDomElement()).openPopup(absLocationPopup, null);
    }
    @On(functionName = "shovelEdit")
    public void shovelEdit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Rect rect = shovelButton.getValue().getAbsBounds();
        AbsLocationPopup absLocationPopup = new AbsLocationPopup(
                rect.getX(), rect.getY()+rect.getHeight(), new WidgetToolEdit(WidgetToolEdit.ToolType.SHOVEL, shovelSettings), true
        );
        absLocationPopup.cursorPassthrough = true;
        PopupMgr.getPopupMgr(getDomElement()).openPopup(absLocationPopup, null);
    }
    @On(functionName = "axeEdit")
    public void axeEdit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Rect rect = axeButton.getValue().getAbsBounds();
        AbsLocationPopup absLocationPopup = new AbsLocationPopup(
                rect.getX(), rect.getY()+rect.getHeight(), new WidgetToolEdit(WidgetToolEdit.ToolType.AXE, axeSettings), true
        );
        absLocationPopup.cursorPassthrough = true;
        PopupMgr.getPopupMgr(getDomElement()).openPopup(absLocationPopup, null);
    }

    @On(functionName = "hasteEdit")
    public void hasteEdit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Rect rect = hasteButton.getValue().getAbsBounds();
        AbsLocationPopup absLocationPopup = new AbsLocationPopup(
                rect.getX(), rect.getY()+rect.getHeight(), new WidgetHasteEdit(hasteSettings), true
        );
        absLocationPopup.cursorPassthrough = true;
        PopupMgr.getPopupMgr(getDomElement()).openPopup(absLocationPopup, null);
    }

    @On(functionName = "create")
    public void create() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        AlgorithmSetting algorithmSetting = new AlgorithmSetting(
                pickaxeSettings.getValue(),
                shovelSettings.getValue(),
                axeSettings.getValue(),
                hasteSettings.getValue(),
                stair.getValue(),
                teleportdown.getValue(),
                enderchest.getValue(),
                etherwarp.getValue(),
                Integer.parseInt(stonkLength.getValue()),
                enderpearl.getValue(),
                tntpearl.getValue(),
                etherwarpOffset.getValue(),
                maxEtherwarp.getValue(),
                etherwarpLeeway.getValue(),
                slowstonk.getValue()
        );
        PopupMgr.getPopupMgr(getDomElement()).closePopup(algorithmSetting);
    }
}
