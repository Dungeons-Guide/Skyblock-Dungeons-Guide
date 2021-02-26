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
    public GUIRectangle(Rectangle rectangle) {

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (rectangle.x < sr.getScaledWidth() / 2) {
            this.x = rectangle.x;
            this.width = rectangle.width;
        } else {
            this.x = rectangle.x + rectangle.width - sr.getScaledWidth();
            this.width = -rectangle.width;
        }

        if (rectangle.y < sr.getScaledHeight() / 2) {
            this.y = rectangle.y;
            this.height = rectangle.height;
        } else {
            this.y = rectangle.y +rectangle.height - sr.getScaledHeight();
            this.height = -rectangle.height;
        }
    }

    private int x;
    private int y;
    private int width;
    private int height;

    public Rectangle getRectangle() {
        return getRectangle(new ScaledResolution(Minecraft.getMinecraft()));
    }
    public Rectangle getRectangle(ScaledResolution scaledResolution) {
        int realX = x < 0 ? scaledResolution.getScaledWidth() + x : x;
        int realY = y < 0 ? scaledResolution.getScaledHeight() + y : y;

        return new Rectangle(Math.min(realX + width, realX), Math.min(realY + height, realY),
                Math.abs(width), Math.abs(height));
    }
}
