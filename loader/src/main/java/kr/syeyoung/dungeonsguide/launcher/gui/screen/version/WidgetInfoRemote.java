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
import kr.syeyoung.dungeonsguide.launcher.loader.IDGLoader;
import net.minecraftforge.common.config.Configuration;

public class WidgetInfoRemote extends WidgetInfo {
    private UpdateBranch updateBranch;
    private Update update;
    private boolean isLatest;
    public WidgetInfoRemote(UpdateBranch branch, Update update, boolean isLatest) {
        super();
        this.updateBranch = branch;
        this.update = update;
        this.isLatest = isLatest;
        setUpdateLog(update.getUpdateLog());
        this.isLatest = isLatest;
        setVersion(branch.getName()+"/"+update.getName()+ " ("+branch.getId()+"/"+update.getId()+")");
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        configuration.get("loader", "modsource", "").set("remote");
        configuration.get("loader", "remoteBranch", "").set(updateBranch.getName());
        configuration.get("loader", "remoteVersion", "").set(isLatest ? "latest" : update.getName());
    }

    @Override
    public IDGLoader getLoader() {
        return null;
    }
}
