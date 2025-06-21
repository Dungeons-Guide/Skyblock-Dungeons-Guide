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

import kr.syeyoung.dungeonsguide.launcher.branch.Update;
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
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WidgetChooseBranchVersion extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "versionListVisibility")
    public final BindableAttribute<String> visibility = new BindableAttribute<>(String.class, "loading");
    @Bind(variableName = "versionList")
    public final BindableAttribute<Column> versionList = new BindableAttribute<>(Column.class);

    @Bind(variableName = "widgetList")
    public final BindableAttribute<List<Widget>> widgetList = new BindableAttribute(WidgetList.class);
    @Bind(variableName = "branch")
    public final BindableAttribute<String> branchName = new BindableAttribute<>(String.class);

    public TriConsumer<UpdateBranch, Update, Boolean> onVersionChoose;
    private UpdateBranch branch;

    @Bind(variableName = "back")
    public final BindableAttribute<Runnable> back = new BindableAttribute<>(Runnable.class);

    public WidgetChooseBranchVersion(TriConsumer<UpdateBranch, Update, Boolean> onVersion, UpdateBranch branch, Runnable back) {
        super(new ResourceLocation("dungeons_guide_loader:gui/versions/branchVersionList.gui"));
        this.onVersionChoose = onVersion;
        this.branch = branch;
        this.branchName.setValue(branch.getName());
        this.back.setValue(back);

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
                try {
                    List<Update> branches = UpdateRetrieverUtil.getLatestUpdates(branch.getId(), 0);
                    if (!branches.isEmpty()) {
                        widgets.add(new BranchButton("Latest", () -> {
                            onVersionChoose.accept(branch, branches.get(0), true);
                        }));
                    }

                    for (Update update : branches) {
                        widgets.add(new BranchButton(update.getName(), () -> {
                            onVersionChoose.accept(branch, update, false);
                        }));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    widgets.add(new Text("Remote Error", 0xFFFFFFFF, Text.TextAlign.CENTER, Text.WordBreak.WORD, 1.0, 8.0));
                }
                if (widgets.isEmpty()) {
                    widgets.add(new Text("Seems Empty", 0xFFFFFFFF, Text.TextAlign.CENTER, Text.WordBreak.WORD, 1.0, 8.0));
                }

                widgetList.setValue(widgets);
                if (versionList.getValue().getDomElement().getWidget() != null) {
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
