package kr.syeyoung.dungeonsguide.features;

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.config.types.TypeConverterRegistry;
import kr.syeyoung.dungeonsguide.features.listener.ScreenRenderListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.w3c.dom.css.Rect;

import javax.sound.midi.MidiEvent;
import java.awt.*;

@Getter
public abstract class GuiFeature extends AbstractFeature implements ScreenRenderListener {
    @Setter
    private GUIRectangle featureRect;
    @Setter(value = AccessLevel.PROTECTED)
    private boolean keepRatio;
    @Setter(value = AccessLevel.PROTECTED)
    private double defaultWidth;
    @Setter(value = AccessLevel.PROTECTED)
    private double defaultHeight;
    private double defaultRatio;

    protected GuiFeature(String category, String name, String description, String key, boolean keepRatio, double width, double height) {
        super(category, name, description, key);
        this.keepRatio = keepRatio;
        this.defaultWidth = width;
        this.defaultHeight = height;
        this.defaultRatio = defaultWidth / (double)defaultHeight;
        if (width > 1) width = width / 720;
        if (height > 1) height = height / 480;
        this.featureRect = new GUIRectangle(0, 0, width, height);
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (!isEnabled()) return;
        GlStateManager.pushMatrix();
        GlStateManager.color(1,1,1,1);
        GlStateManager.disableFog();GL11.glDisable(GL11.GL_FOG);
        GlStateManager.disableLighting();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        Rectangle featureRect = this.featureRect.getRectangle(scaledResolution);
        clip(scaledResolution, featureRect.x, featureRect.y, featureRect.width, featureRect.height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GlStateManager.translate(featureRect.x, featureRect.y, 0);
        drawHUD(partialTicks);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();
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
        GUIRectangle featureRect = TypeConverterRegistry.getTypeConverter("guirect",GUIRectangle.class).deserialize(jsonObject.get("$bounds2"));
        if (featureRect != null && featureRect.getWidth() <= 1 && featureRect.getHeight() <= 1) this.featureRect = featureRect;
    }

    @Override
    public JsonObject saveConfig() {
        JsonObject object = super.saveConfig();
        object.add("$bounds2", TypeConverterRegistry.getTypeConverter("guirect", GUIRectangle.class).serialize(featureRect));
        return object;
    }
}
