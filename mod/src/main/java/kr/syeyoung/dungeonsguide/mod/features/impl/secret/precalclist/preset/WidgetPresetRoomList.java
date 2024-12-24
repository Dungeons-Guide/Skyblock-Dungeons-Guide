package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Wrap;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WidgetPresetRoomList extends AnnotatedImportOnlyWidget {

    private PathfindPreset preset;
    private WidgetViewPreset parent;

    @Bind(variableName = "filterText")
    public final BindableAttribute<String> filterText = new BindableAttribute<>(String.class);
    @Bind(variableName = "sortText")
    public final BindableAttribute<String> sortText = new BindableAttribute<>(String.class);

    @Bind(variableName = "search")
    public final BindableAttribute<String> search = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "rooms")
    public final BindableAttribute<List<Widget>> rooms = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "roomsApi")
    public final BindableAttribute<Wrap> roomsApi = new BindableAttribute<>(Wrap.class);

    private List<AdditionalInfoCaculatedDungeonRoomInfo> calculatedDRI;
    private List<WidgetPresetRoom> roomWidgets = new ArrayList<>();
    public WidgetPresetRoomList(PathfindPreset preset, WidgetViewPreset widgetViewPreset, List<AdditionalInfoCaculatedDungeonRoomInfo> roomInfoList) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/presetview/roomlist.gui"));
        this.preset = preset;
        this.parent = widgetViewPreset;

        this.filterText.setValue(currentFilter.display);
        this.sortText.setValue(currentSort.display);
        this.calculatedDRI =roomInfoList;


        for (AdditionalInfoCaculatedDungeonRoomInfo additionalInfoCaculatedDungeonRoomInfo : calculatedDRI) {
            roomWidgets.add(new WidgetPresetRoom(additionalInfoCaculatedDungeonRoomInfo, preset, this));
        }


        rooms.setValue(rebuildList());

        search.addOnUpdate((old, neu) -> {
            update();
        });
    }

    private RoomFilter currentFilter = RoomFilter.ALL;
    private RoomSort currentSort = RoomSort.ATOZ;



    @AllArgsConstructor
    public enum RoomFilter implements Predicate<AdditionalInfoCaculatedDungeonRoomInfo> {
        ALL("Filter: Show All") {
            @Override
            public boolean test(AdditionalInfoCaculatedDungeonRoomInfo additionalInfoCaculatedDungeonRoomInfo) {
                return true;
            }
        },
        MISSING("Filter: Show Rooms with missing precalculation") {
            @Override
            public boolean test(AdditionalInfoCaculatedDungeonRoomInfo additionalInfoCaculatedDungeonRoomInfo) {
                return !additionalInfoCaculatedDungeonRoomInfo.getMissing().isEmpty();
            }
        },
        REDUNANT("Filter: Show rooms with duplicate or unused precalculation") {
            @Override
            public boolean test(AdditionalInfoCaculatedDungeonRoomInfo additionalInfoCaculatedDungeonRoomInfo) {
                return !additionalInfoCaculatedDungeonRoomInfo.getDuplicate().isEmpty() || !additionalInfoCaculatedDungeonRoomInfo.getUnused().isEmpty();
            }
        },
        WARNING("Filter: Show Rooms with warnings") {
            @Override
            public boolean test(AdditionalInfoCaculatedDungeonRoomInfo additionalInfoCaculatedDungeonRoomInfo) {
                return additionalInfoCaculatedDungeonRoomInfo.getWarnings() > 0;
            }
        },
        OVERRIDEN("Filter: Show Rooms with ability overriden") {
            @Override
            public boolean test(AdditionalInfoCaculatedDungeonRoomInfo additionalInfoCaculatedDungeonRoomInfo) {
                return additionalInfoCaculatedDungeonRoomInfo.getRoomPreset().isOverridingParentAlgorithmSetting();
            }
        };

        private String display;

    }
    @AllArgsConstructor
    public enum RoomSort  implements Comparator<WidgetPresetRoom> {
        ATOZ("Sort: A to Z") {
            @Override
            public int compare(WidgetPresetRoom o1, WidgetPresetRoom o2) {
                return o1.getRoomInfo().getDungeonRoomInfo().getName().compareTo(o2.getRoomInfo().getDungeonRoomInfo().getName());
            }
        },
        ZTOA("Sort: Z to A") {
            @Override
            public int compare(WidgetPresetRoom o1, WidgetPresetRoom o2) {
                return -o1.getRoomInfo().getDungeonRoomInfo().getName().compareTo(o2.getRoomInfo().getDungeonRoomInfo().getName());
            }
        },
        ROOMTYPE("Sort: Room Type") {
            @Override
            public int compare(WidgetPresetRoom o1, WidgetPresetRoom o2) {
                int cmp1 = o1.getRoomInfo().getRoomType().compareTo(o2.getRoomInfo().getRoomType());
                if (cmp1 != 0) return cmp1;
                return -Integer.compare(o1.getRoomInfo().getDungeonRoomInfo().getTotalSecrets(), o2.getRoomInfo().getDungeonRoomInfo().getTotalSecrets());
            }
        },
        ROOMSHAPE("Sort: Room Shape") {
            @Override
            public int compare(WidgetPresetRoom o1, WidgetPresetRoom o2) {
                int cmp1 = -o1.getRoomInfo().getRoomShape().compareTo(o2.getRoomInfo().getRoomShape());
                if (cmp1 != 0) return cmp1;
                cmp1 = o1.getRoomInfo().getRoomType().compareTo(o2.getRoomInfo().getRoomType());
                if (cmp1 != 0) return cmp1;
                return -Integer.compare(o1.getRoomInfo().getDungeonRoomInfo().getTotalSecrets(), o2.getRoomInfo().getDungeonRoomInfo().getTotalSecrets());
            }
        },
        SECRET("Sort: # of secrets") {
            @Override
            public int compare(WidgetPresetRoom o1, WidgetPresetRoom o2) {
                return -Integer.compare(o1.getRoomInfo().getDungeonRoomInfo().getTotalSecrets(), o2.getRoomInfo().getDungeonRoomInfo().getTotalSecrets());
            }
        },
        MISSING("Sort: # of missing precalculation") {
            @Override
            public int compare(WidgetPresetRoom o1, WidgetPresetRoom o2) {
                return -Integer.compare(o1.getRoomInfo().getMissing().size(), o2.getRoomInfo().getMissing().size());
            }
        };

        private String display;

    }

    @On(functionName = "cycleFilter")
    public void cycleFilter() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        currentFilter = RoomFilter.values()[(currentFilter.ordinal() + 1) % RoomFilter.values().length];
        this.filterText.setValue(currentFilter.display);

        update();
    }

    @On(functionName = "cycleSort")
    public void cycleSort() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        currentSort = RoomSort.SECRET.values()[(currentSort.ordinal() + 1) % RoomSort.values().length];
        this.sortText.setValue(currentSort.display);

        update();
    }


    private volatile AtomicBoolean updating = new AtomicBoolean();
    public void update() {
        if (updating.getAndSet(true)) return;

        WidgetViewPreset.calculator.submit(() -> {
            List<Widget> widgets = rebuildList();
            Minecraft.getMinecraft().addScheduledTask(() -> {
                roomsApi.getValue().removeAllWidget();
                for (Widget widget : widgets) {
                    roomsApi.getValue().addWidget(widget);
                }
                updating.set(false);
            });
        });
    }

    public List<Widget> rebuildList() {
        String toSearch = search.getValue().trim().toLowerCase();
        return roomWidgets.stream()
                .filter(a -> currentFilter.test(a.getRoomInfo()))
                .filter(a -> toSearch.isEmpty() || a.roomName.getValue().toLowerCase().contains(toSearch))
                .sorted(currentSort)
                .collect(Collectors.toList());
    }
}
