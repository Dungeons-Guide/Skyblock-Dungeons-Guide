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

package kr.syeyoung.dungeonsguide.config.types;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class AColor extends Color {
    private boolean chroma;
    private float chromaSpeed;

    public AColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public AColor(int rgba, boolean hasalpha) {
        super(rgba, hasalpha);
    }

    public AColor(AColor clone) {
        super(clone.getRGB(), true);
        chroma = clone.isChroma();
        chromaSpeed = clone.getChromaSpeed();
    }

    public AColor multiplyAlpha(double multiplier) {
        AColor aColor = new AColor(getRed(), getGreen(), getBlue(), (int) (getAlpha() * multiplier));
        aColor.chroma = this.chroma;
        aColor.chromaSpeed = this.chromaSpeed;
        return aColor;
    }

    @Override
    public String toString() {
        return "AColor{" +
                ", r="+getRed()+
                ", g="+getGreen()+
                ", b="+getBlue()+
                ", a="+getAlpha()+
                ", chromaSpeed=" + chromaSpeed +
                '}';
    }
}
