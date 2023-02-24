/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.gui.screen.version;

import kr.syeyoung.dungeonsguide.launcher.branch.UpdateBranch;
import kr.syeyoung.dungeonsguide.launcher.branch.UpdateRetrieverUtil;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.WidgetPrivacyPolicy;
import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.launcher.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WidgetChooseBranch extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "versionListVisibility")
    public final BindableAttribute<String> visibility = new BindableAttribute<>(String.class, "loading");
    @Bind(variableName = "versionList")
    public final BindableAttribute<Column> versionList = new BindableAttribute<>(Column.class);

    @Bind(variableName = "widgetList")
    public final BindableAttribute<List<Widget>> widgetList = new BindableAttribute(WidgetList.class);

    public Consumer<UpdateBranch> onBranchChoose;
    public Runnable onLocalChoose;
    public Runnable onJarChoose;

    public WidgetChooseBranch(Consumer<UpdateBranch> onBranch, Runnable onLocal, Runnable onJar) {
        super(new ResourceLocation("dungeons_guide_loader:gui/versions/branchList.gui"));
        this.onBranchChoose = onBranch;
        this.onLocalChoose = onLocal;
        this.onJarChoose = onJar;
        widgetList.setValue(new ArrayList<>());
        reload0();
    }

    @On(functionName = "reload")
    public void reload() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        reload0();
    }

    private volatile boolean isLoading = false;
    private void reload0() {
        if (isLoading) return;
        isLoading = true;
        visibility.setValue("loading");
        WidgetPrivacyPolicy.executor.submit(() -> {
            try {
                List<Widget> widgets = new ArrayList<>();
                if (this.getClass().getResource("/kr/syeyoung/dungeonsguide/mod/DungeonsGuide.class") != null)
                    widgets.add(new BranchButton("Local", () -> {
                        onLocalChoose.run();
                    }));
                if (this.getClass().getResource("/mod.jar") != null)
                    widgets.add(new BranchButton("Jar", () -> {
                        onJarChoose.run();
                    }));

                try {
                    List<UpdateBranch> branches = UpdateRetrieverUtil.getUpdateBranches().stream()
                            .filter(updateBranch ->
                                    Optional.ofNullable(updateBranch.getMetadata())
                                            .filter(a -> a.has("additionalMeta"))
                                            .map(a -> a.getJSONObject("additionalMeta"))
                                            .filter(a -> a.has("type"))
                                            .map(a -> a.getString("type")).orElse("").equals("mod"))
                            .collect(Collectors.toList());
                    for (UpdateBranch branch : branches) {
                        widgets.add(new BranchButton("Remote: "+branch.getName(), () -> {
                            onBranchChoose.accept(branch);
                        }));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    widgets.add(new Text("Remote Error\n\n"+e.getClass().getName()+": "+e.getMessage(), 0xFFFFFFFF, Text.TextAlign.CENTER, Text.WordBreak.WORD, 1.0, 8.0));
                }
                if (widgets.isEmpty()) {
                    widgets.add(new Text("Seems Empty", 0xFFFFFFFF, Text.TextAlign.CENTER, Text.WordBreak.WORD, 1.0, 8.0));
                }

                widgetList.setValue(widgets);
                if (versionList.getValue() != null && versionList.getValue().getDomElement().getWidget() != null) {
                    versionList.getValue().removeAll();
                    for (Widget widget : widgets) {
                        versionList.getValue().addWidget(widget);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                visibility.setValue("loaded");
                isLoading = false;
            }
        });
    }
}
