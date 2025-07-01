package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.WidgetDungeonMap;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlay;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Placeholder;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.events.WindowUpdateEvent;
import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.gui.UContainerSlot;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.paralleluniverse.tablist.UTabListEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetSpiritLeap extends AnnotatedImportOnlyWidget {
    private MapConfiguration mapConfiguration = new MapConfiguration();
    public WidgetSpiritLeap() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/spiritleap/spiritleap.gui"));
        MapConfiguration defaultConfig = FeatureRegistry.DUNGEON_MAP2.getMapConfiguration();
        mapConfiguration.setMapScale(1.0);
        mapConfiguration.setBorder(defaultConfig.getBorder());
        mapConfiguration.setBackgroundColor(defaultConfig.getBackgroundColor());
        mapConfiguration.setMapRotation(MapConfiguration.MapRotation.VERTICAL);
        mapConfiguration.setBorderWidth(defaultConfig.getBorderWidth());
        mapConfiguration.setDrawName(defaultConfig.isDrawName());
        mapConfiguration.getSelfSettings().setIconType(MapConfiguration.PlayerHeadSettings.IconType.ARROW);
        mapConfiguration.getSelfSettings().setIconSize(1.4);
        mapConfiguration.getTeammateSettings().setIconType(MapConfiguration.PlayerHeadSettings.IconType.HEAD);
        mapConfiguration.getTeammateSettings().setIconSize(1.4);
        mapConfiguration.setCheckmarkSettings(defaultConfig.getCheckmarkSettings());
        mapConfiguration.setNameSettings(defaultConfig.getNameSettings());
        mapConfiguration.setRoomOverrides(FeatureRegistry.DUNGEON_MAP2.getMapConfiguration().getRoomOverrides());
        map.setValue(new WidgetDungeonMap(mapConfiguration, this::getOverlays));
    }

    private List<MapOverlay> getOverlays() {
        List<MapOverlay> overlays = new ArrayList<>();


        int i = 0;
        for (UTabListEntry playerInfo : ModAPI.getAPI().getTabList().getTabListEntries()) {
            if (++i >= 20) break;

            String name = TabListUtil.getPlayerNameWithChecks(playerInfo);
            if (name == null) continue;

            UEntityPlayer entityplayer = ModAPI.getAPI().getWorld().getUPlayerEntityByName(name);

            overlays.add(new MapOverlayPlayerClickable(playerInfo, entityplayer != null && entityplayer.equals(ModAPI.getAPI().getPlayer()) ? mapConfiguration.getSelfSettings() : mapConfiguration.getTeammateSettings(), nameMap.get(name)));
        }

        return overlays;
    }

    @Bind(variableName = "api")
    public final BindableAttribute<Column> api = new BindableAttribute(Column.class);

    @Bind(variableName = "map")
    public final BindableAttribute<Widget> map = new BindableAttribute(Widget.class, new Placeholder());

    private Map<Integer, WarpTarget> slotMap = new HashMap<>();
    private Map<String, WarpTarget> nameMap = new HashMap<>();


    public void onChestUpdate(WindowUpdateEvent windowUpdateEvent) {
        if (windowUpdateEvent == null) {
            UContainerChest guiChest = GuiScreenAdapterChestOverride.getAdapter(getDomElement()).getGuiChest();
            if (guiChest == null) {
                slotMap.clear();
            } else {
                for (int x = 1; x<=7; x++) {
                    int y = 1;
                    int i = y * 9 + x;
                    UContainerSlot s = guiChest.getChestSlotAt(i);
                    WarpTarget prev = slotMap.remove(i);
                    if (s == null || s.getItemStack() == null || s.getItemStack().getItem() != Item.SKULL) { continue; }

                    slotMap.put(i, new WarpTarget(s.getItemStack(), i));
                }

            }
        } else {
            for (WindowUpdateEvent.SlotUpdate slotUpdate : windowUpdateEvent.getSlotUpdateList()) {

                int i = slotUpdate.getSlotId();
                UItemStack stack = slotUpdate.getItemStack();

                if (i / 9 < 3) {
                    WarpTarget prev = slotMap.remove(i);
                    if (stack != null && stack.getItem() == Item.SKULL) {
                        slotMap.put(i, new WarpTarget(stack, i));
                    }
                }
            }
        }
        nameMap.clear();
        for (Map.Entry<Integer, WarpTarget> integerWarpTargetEntry : slotMap.entrySet()) {
            nameMap.put(TextUtils.stripColor(integerWarpTargetEntry.getValue().getItemStack().getDisplayName()), integerWarpTargetEntry.getValue());
        }
        update();
    }

    public void update() {
        this.api.getValue().removeAllWidget();

        Map<String, UTabListEntry> map = new HashMap<>();
        int i = 0;
        for (UTabListEntry playerInfo : ModAPI.getAPI().getTabList().getTabListEntries()) {
            if (++i >= 20) break;

            String name = TabListUtil.getPlayerNameWithChecksIncludingDead(playerInfo);
            if (name == null) continue;

            map.put(name, playerInfo);
        }

        for (Map.Entry<Integer, WarpTarget> integerWarpTargetEntry : slotMap.entrySet()) {
            this.api.getValue().addWidget(new WidgetLeapPlayer(integerWarpTargetEntry.getValue(), map.get(TextUtils.stripColor(integerWarpTargetEntry.getValue().getItemStack().getDisplayName()))));
        }
    }
}
