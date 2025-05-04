package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings;

import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class WidgetToolEdit extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "efficiencyOpen")
    public final BindableAttribute<String> efficiencyOpen = new BindableAttribute<>(String.class);

    @Bind(variableName = "efficiencies")
    public final BindableAttribute<List<Widget>> efficiencyButtons = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "tools")
    public final BindableAttribute<List<Widget>> tools = new BindableAttribute(WidgetList.class);
    /*
    *     <kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetIconButton bind:iconOffset="offset" iconIdx="3" bind:selected="sel_no" disabled="false" on:click="no"/>
                        <kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetIconButton bind:iconOffset="offset" iconIdx="0" bind:selected="sel_wood" disabled="false" on:click="wood"/>
                        <kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetIconButton bind:iconOffset="offset" iconIdx="8" bind:selected="sel_stone" disabled="false" on:click="stone"/>
                        <kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetIconButton bind:iconOffset="offset" iconIdx="16" bind:selected="sel_iron" disabled="false" on:click="iron"/>
                        <kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetIconButton bind:iconOffset="offset" iconIdx="24" bind:selected="sel_diamond" disabled="false" on:click="diamond"/>
                        <kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings.WidgetIconButton bind:iconOffset="offset" iconIdx="32" bind:selected="sel_gold" disabled="false" on:click="gold"/>

                    * */

    @AllArgsConstructor
    public enum ToolType {
        PICKAXE(0, (a) -> a.pickaxe), SHOVEL(1, (a) -> a.shovel), AXE(2, (a) -> a.axe);
        public int offset;
        public Function<ToolMaterialType, ItemTool> converter;
    }

    @AllArgsConstructor
    public enum ToolMaterialType {
        NONE(3, null, null, null, null),
        WOOD(0, Item.ToolMaterial.WOOD, (ItemTool) Items.wooden_pickaxe, (ItemTool) Items.wooden_shovel, (ItemTool) Items.wooden_axe),
        STONE(8, Item.ToolMaterial.STONE, (ItemTool) Items.stone_pickaxe, (ItemTool) Items.stone_shovel, (ItemTool) Items.stone_axe),
        IRON(16, Item.ToolMaterial.IRON, (ItemTool) Items.iron_pickaxe, (ItemTool) Items.iron_shovel, (ItemTool) Items.iron_axe),
        DIAMOND(24, Item.ToolMaterial.EMERALD, (ItemTool) Items.diamond_pickaxe, (ItemTool) Items.diamond_shovel, (ItemTool) Items.diamond_axe),
        GOLD(32, Item.ToolMaterial.GOLD, (ItemTool) Items.golden_pickaxe, (ItemTool) Items.golden_shovel, (ItemTool) Items.golden_axe);

        public int index;

        public Item.ToolMaterial material;

        public ItemTool pickaxe;
        public ItemTool shovel;
        public ItemTool axe;
    }

    public final BindableAttribute<AlgorithmSetting.ToolSettings> currentToolSettings = new BindableAttribute<>(AlgorithmSetting.ToolSettings.class);

    private final BindableAttribute<Boolean>[] selected = new BindableAttribute[ToolMaterialType.values().length];

    private final BindableAttribute<Boolean>[] effSelected = new BindableAttribute[11];

    private ToolType type;
    public WidgetToolEdit(ToolType type, BindableAttribute<AlgorithmSetting.ToolSettings> toolSettingsBindableAttribute) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/abilityedit/tooledit.gui"));
        currentToolSettings.exportTo(toolSettingsBindableAttribute);
        this.type = type;

        AlgorithmSetting.ToolSettings toolSettings = toolSettingsBindableAttribute.getValue();

        BindableAttribute<Integer> offset = new BindableAttribute<>(Integer.class, type.offset);
        BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class, false);

        List<Widget> tools = new ArrayList<>();
        ToolMaterialType[] values = ToolMaterialType.values();
        for (int i = 0; i < values.length; i++) {
            ToolMaterialType material = values[i];
            WidgetIconButton iconButton = new WidgetIconButton();
            selected[i] = new BindableAttribute<Boolean>(Boolean.class,
                    toolSettings == null && material == ToolMaterialType.NONE
                    || toolSettings != null && material.material == toolSettings.getTool().getToolMaterial());
            iconButton.selected.exportTo(selected[i]);
            iconButton.iconOffset.exportTo(offset);

            iconButton.iconIdx.exportTo(new BindableAttribute<Integer>(Integer.class, material.index));
            iconButton.<Runnable>getExportedAttribute("click").exportTo(new BindableAttribute<>(Runnable.class, () -> {
                updateChosen(material);
            }));
            iconButton.<Boolean>getExportedAttribute("disabled").exportTo(disabled);

            tools.add(iconButton);
        }
        this.tools.setValue(tools);


        List<Widget> efficiencies = new ArrayList<>();
        for (int i = 0; i < effSelected.length; i++) {

            WidgetEfficiencyButton efficiencyButton = new WidgetEfficiencyButton();
            effSelected[i] = new BindableAttribute<>(Boolean.class, toolSettings != null && toolSettings.getEfficiency() == i);
            efficiencyButton.selected.exportTo(effSelected[i]);
            efficiencyButton.<Boolean>getExportedAttribute("disabled").exportTo(disabled);
            int finalI = i;
            efficiencyButton.<Runnable>getExportedAttribute("click").exportTo(new BindableAttribute<>(Runnable.class, () -> {
                updateEfficiency(finalI);
            }));
            efficiencyButton.<String>getExportedAttribute("text").exportTo(new BindableAttribute<>(String.class, i == 0 ? "X" : String.valueOf(i)));
            efficiencies.add(efficiencyButton);
        }
        this.efficiencyButtons.setValue(efficiencies);

        efficiencyOpen.setValue(toolSettings == null ? "close" : "open");

    }

    private void updateEfficiency(int i) {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        for (BindableAttribute<Boolean> booleanBindableAttribute : effSelected) {
            booleanBindableAttribute.setValue(false);
        }
        effSelected[i].setValue(true);

        currentToolSettings.setValue(new AlgorithmSetting.ToolSettings(
                this.currentToolSettings.getValue().getTool(), i
        ));
    }

    public void updateChosen(ToolMaterialType material) {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        for (BindableAttribute<Boolean> booleanBindableAttribute : selected) {
            booleanBindableAttribute.setValue(false);
        }
        selected[material.ordinal()].setValue(true);

        if (material == ToolMaterialType.NONE) {
            currentToolSettings.setValue(null);
            efficiencyOpen.setValue("close");

            for (BindableAttribute<Boolean> booleanBindableAttribute : effSelected) {
                booleanBindableAttribute.setValue(false);
            }
        } else {
            if (currentToolSettings.getValue() == null) {
                currentToolSettings.setValue(new AlgorithmSetting.ToolSettings(this.type.converter.apply(material), 0));
                effSelected[0].setValue(true);
            } else {
                currentToolSettings.setValue(new AlgorithmSetting.ToolSettings(this.type.converter.apply(material), currentToolSettings.getValue().getEfficiency()));
            }
            efficiencyOpen.setValue("open");
        }
    }


}
