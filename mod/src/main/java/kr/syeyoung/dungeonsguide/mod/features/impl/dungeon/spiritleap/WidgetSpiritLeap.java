package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.events.impl.WindowUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.WidgetDungeonMap;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui.PartyFinderParty;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui.WidgetPartyElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Placeholder;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetSpiritLeap extends AnnotatedImportOnlyWidget {
    public WidgetSpiritLeap() {
        super(new ResourceLocation("dungeonsguide:gui/features/spiritleap/spiritleap.gui"));
        MapConfiguration mapConfiguration = new MapConfiguration();
        mapConfiguration.setMapScale(1.0);
        mapConfiguration.setBorder(new AColor(0xFF000000, true));
        mapConfiguration.setBackgroundColor(new AColor(0x33000000, true));
        mapConfiguration.setMapRotation(MapConfiguration.MapRotation.VERTICAL);
        mapConfiguration.setBorderWidth(1.0);
        mapConfiguration.setDrawName(true);
        mapConfiguration.getSelfSettings().setIconType(MapConfiguration.PlayerHeadSettings.IconType.ARROW);
        mapConfiguration.getSelfSettings().setIconSize(5.0);
        mapConfiguration.getTeammateSettings().setIconType(MapConfiguration.PlayerHeadSettings.IconType.HEAD);
        mapConfiguration.getTeammateSettings().setIconSize(5.0);
        mapConfiguration.getCheckmarkSettings().setCenter(true);
        mapConfiguration.getCheckmarkSettings().setScale(2.0);
        mapConfiguration.getCheckmarkSettings().setStyle(MapConfiguration.RoomInfoSettings.Style.CHECKMARK_AND_COUNT);
        mapConfiguration.getNameSettings().setNameRotation(MapConfiguration.NameSettings.NameRotation.SNAP_LONG);
        mapConfiguration.getNameSettings().setPadding(5.0);
        mapConfiguration.getNameSettings().setDrawName(true);
        mapConfiguration.getNameSettings().setTextColor(new AColor(0xFFFFFFFF,true));

        map.setValue(new WidgetDungeonMap(mapConfiguration));
    }

    @Bind(variableName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute(Column.class);

    @Bind(variableName = "map")
    public final BindableAttribute<Widget> map = new BindableAttribute(Widget.class, new Placeholder());

    private Map<Integer, WarpTarget> slotMap = new HashMap<>();


    public void onChestUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (windowUpdateEvent == null) {
            GuiChest guiChest = GuiScreenAdapterChestOverride.getAdapter(getDomElement()).getGuiChest();
            if (guiChest == null) {
                slotMap.clear();
            } else {
                for (int x = 1; x<=7; x++) {
                    int y = 1;
                    int i = y * 9 + x;
                    Slot s = guiChest.inventorySlots.getSlot(i);
                    WarpTarget prev = slotMap.remove(i);
                    if (s == null || !s.getHasStack() || s.getStack().getItem() != Items.skull) { continue; }

                    slotMap.put(i, new WarpTarget(s.getStack(), i));
                }

            }
        } else {
            if (windowUpdateEvent.getPacketSetSlot() != null) {
                int i = windowUpdateEvent.getPacketSetSlot().func_149173_d();

                ItemStack stack = windowUpdateEvent.getPacketSetSlot().func_149174_e();

                if (i / 9 < 3) {
                    WarpTarget prev = slotMap.remove(i);
                    if (stack != null && stack.getItem() == Items.skull) {
//                    if (prev == null) prev = new WidgetPartyElement(this, i);
//                    prev.update(PartyFinderParty.fromItemStack(stack));
                        System.out.println(stack.getTagCompound());
                        slotMap.put(i, new WarpTarget(stack, i));
                    }
                }
            } else if (windowUpdateEvent.getWindowItems() != null) {
                for (int x = 1; x<=7; x++) {
                    int y = 1;
                    int i = y * 9 + x;

                    ItemStack item = windowUpdateEvent.getWindowItems().getItemStacks()[i];
                    WarpTarget prev = slotMap.remove(i);
                    if (item == null || item.getItem() != Items.skull) { continue; }

                    slotMap.put(i, new WarpTarget(item, i));
                }
            }
        }
        update();
    }

    public void update() {
        this.api.getValue().removeAllWidget();
        for (Map.Entry<Integer, WarpTarget> integerWarpTargetEntry : slotMap.entrySet()) {
            this.api.getValue().addWidget(new WidgetLeapPlayer(integerWarpTargetEntry.getValue()));
        }
    }
}
