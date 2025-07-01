/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.event.Cancelable;
import kr.syeyoung.modapi.event.UEvent;
import lombok.Getter;
import lombok.Setter;

public class PlayerInteractEntityEvent extends UEvent implements Cancelable {

    @Getter @Setter
    private boolean attack;
    @Getter @Setter
    private boolean interactAt;
    @Getter @Setter
    private UEntity entity;

    public PlayerInteractEntityEvent(boolean attack, boolean interactAt, UEntity entity) {
        this.attack = attack;
        this.interactAt = interactAt;
        this.entity = entity;
    }


    private boolean canceled = false;
    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
