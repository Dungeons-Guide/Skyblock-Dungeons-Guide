package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.preset;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind.PathfindPreset;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.WidgetPrecalcList;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WidgetViewPreset extends AnnotatedImportOnlyWidget {

    @Getter
    private PathfindPreset preset;
    private WidgetPrecalcList presetList;

    @Bind(variableName = "metadata")
    public final BindableAttribute<Widget> metadata = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "roompreset")
    public final BindableAttribute<Widget> roompreset = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "crunchData")
    public final BindableAttribute<String> crunchData = new BindableAttribute<>(String.class, "calculating");


    public static final ExecutorService calculator = DungeonsGuide.getDungeonsGuide().registerExecutorService(
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-ViewPresetCalc-%d").build()));

    public WidgetViewPreset(PathfindPreset preset, WidgetPrecalcList presetList) {
        super(new ResourceLocation("dungeonsguide:gui/features/precalclist/presetview.gui"));
        this.preset = preset;
        this.presetList = presetList;

        this.metadata.setValue(new WidgetPresetMetadata(preset, this));


    }

    @Override
    public void onMount() {

        roompreset.setValue(null);
        crunchData.setValue("calculating");
        calculator.submit(() -> {
            try {
                List<AdditionalInfoCaculatedDungeonRoomInfo> additionalInfoCaculatedDungeonRoomInfoList = new ArrayList<>();
                for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                    additionalInfoCaculatedDungeonRoomInfoList.add(new AdditionalInfoCaculatedDungeonRoomInfo(dungeonRoomInfo, preset));
                }

                Minecraft.getMinecraft().addScheduledTask(() -> {

                    roompreset.setValue(new WidgetPresetRoomList(preset, this, additionalInfoCaculatedDungeonRoomInfoList));
                    crunchData.setValue("done");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void notifyDelete(PathfindPreset preset) {
        presetList.notifyDelete(preset);
    }

    public void notifyNew(PathfindPreset preset1) {
        presetList.addPreset(preset1);
    }

    public void notifyNameUpdate(PathfindPreset preset) {
        presetList.update(preset);
    }
}
