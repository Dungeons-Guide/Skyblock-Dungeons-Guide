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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3;

import kr.syeyoung.dungeonsguide.mod.config.Config;
import kr.syeyoung.dungeonsguide.mod.gui.CustomGuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.modapi.gui.UGuiScreen;

public class ConfigCustomGuiScreenAdapter extends CustomGuiScreenAdapter {
    public ConfigCustomGuiScreenAdapter(UGuiScreen parent) {
        super(new GlobalHUDScale(new MainConfigWidget()), parent);
    }
    public ConfigCustomGuiScreenAdapter(UGuiScreen parent, Widget widget) {
        super(widget, parent);
    }


    @Override
    public void onRemoved() {
        super.onRemoved();
        Config.scheduleConfigSave();
    }
}
