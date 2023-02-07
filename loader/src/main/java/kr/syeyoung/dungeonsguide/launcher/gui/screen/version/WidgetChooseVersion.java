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
import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class WidgetChooseVersion extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "branchWidget")
    public final BindableAttribute<Widget> branchWidget = new BindableAttribute<>(Widget.class, new WidgetChooseBranch(this::branch, this::local, this::jar));
    @Bind(variableName = "versionWidget")
    public final BindableAttribute<Widget> versionWidget = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "listVisibility")
    public final BindableAttribute<String> listVisibility = new BindableAttribute<>(String.class, "branch");
    @Bind(variableName = "infoVisibility")
    public final BindableAttribute<String> infoVisibility = new BindableAttribute<>(String.class, "hide");

    @Bind(variableName = "infoWidget")
    public final BindableAttribute<Widget> infoWidget = new BindableAttribute<>(Widget.class);
    private void back() {
        listVisibility.setValue("branch");
    }

    private void jar() {
    }

    private void local() {
    }

    private void remote(UpdateBranch branch, Update update, boolean isLatest) {
        infoWidget.setValue(new WidgetInfoRemote(branch, update, isLatest));
        infoVisibility.setValue("show");
    }

    private void branch(UpdateBranch updateBranch) {
        versionWidget.setValue(new WidgetChooseBranchVersion(this::remote, updateBranch, this::back));
        listVisibility.setValue("version");
    }

    public WidgetChooseVersion() {
        super(new ResourceLocation("dungeons_guide_loader:gui/versions/versionChooser.gui"));
    }

    @On(functionName = "exit")
    public void exit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
}
