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

package kr.syeyoung.dungeonsguide.mod;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class WidgetUpdateLog extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "updatelog")
    public final BindableAttribute<String> updateLog = new BindableAttribute<>(String.class);
    @Bind(variableName = "version")
    public final BindableAttribute<String> version = new BindableAttribute<>(String.class);

    @Bind(variableName = "disabled")
    public final BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class, false);

    public WidgetUpdateLog(String version, String updateLog, boolean autoupdate) {
        super(new ResourceIdentifier("dungeonsguide:gui/update.gui"));
        this.version.setValue(version);
        this.updateLog.setValue(updateLog);
        this.disabled.setValue(!autoupdate);
    }

    @On(functionName = "continue")
    public void continueB() {
        ModAPI.getAPI().displayGuiScreen(null);
    }

    @On(functionName = "unload")
    public void unload() {
        DungeonsGuide.getDungeonsGuide().getLoaderAPI().requestUnload();
    }
}
