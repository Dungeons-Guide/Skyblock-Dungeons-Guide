package kr.syeyoung.dungeonsguide.config.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GUIRectangle {
    private double x;
    private double y;
    private double width;
    private double height;

    public Rectangle getRectangle() {
        return getRectangle(new ScaledResolution(Minecraft.getMinecraft()));
    }
    public Rectangle getRectangle(ScaledResolution scaledResolution) {
        return new Rectangle((int) (x * scaledResolution.getScaledWidth()), (int) (y * scaledResolution.getScaledHeight()), (int) (width * scaledResolution.getScaledWidth()), (int) (height * scaledResolution.getScaledHeight()));
    }
}
