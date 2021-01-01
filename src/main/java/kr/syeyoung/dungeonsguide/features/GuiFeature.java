package kr.syeyoung.dungeonsguide.features;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.types.TypeConverterRegistry;
import kr.syeyoung.dungeonsguide.features.listener.ScreenRenderListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import javax.sound.midi.MidiEvent;
import java.awt.*;

@Getter
public abstract class GuiFeature extends AbstractFeature implements ScreenRenderListener {
    @Setter
    private Rectangle featureRect;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean keepRatio;
    @Setter(value = AccessLevel.PROTECTED)
    private int defaultWidth;
    @Setter(value = AccessLevel.PROTECTED)
    private int defaultHeight;
    private double defaultRatio;

    protected GuiFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key);
        this.keepRatio = keepRatio;
        this.defaultWidth = width;
        this.defaultHeight = height;
        this.defaultRatio = defaultWidth / (double)defaultHeight;
        this.featureRect = new Rectangle(0, 0, width, height);
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (!isEnabled()) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        clip(new ScaledResolution(Minecraft.getMinecraft()), featureRect.x, featureRect.y, featureRect.width, featureRect.height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

//        GL11.glEnable(GL11.GL_BLEND);
//        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glPushMatrix();
        GL11.glTranslated(featureRect.x, featureRect.y, 0);
        drawHUD(partialTicks);
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }


    public abstract void drawHUD(float partialTicks);

    public void drawDemo(float partialTicks) {
        drawHUD(partialTicks);
    }

    private void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    public static FontRenderer getFontRenderer() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        return fr;
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
        featureRect = TypeConverterRegistry.getTypeConverter("rect", Rectangle.class).deserialize(jsonObject.get("$bounds"));
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject object = super.saveConfig();
        object.add("$bounds", TypeConverterRegistry.getTypeConverter("rect", Rectangle.class).serialize(featureRect));
        return object;
    }
}
