package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.roomedit.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.roomedit.elements.MColor;
import kr.syeyoung.dungeonsguide.roomedit.elements.MFloatSelectionButton;
import kr.syeyoung.dungeonsguide.roomedit.elements.MLabelAndElement;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ValueEditAColor extends MPanel implements ValueEdit<AColor> {
    private Parameter parameter;
    private final MFloatSelectionButton h;
    private final MFloatSelectionButton s;
    private final MFloatSelectionButton v;
    private final MFloatSelectionButton a;


    @Override
    public void renderWorld(float partialTicks) {

    }
    public ValueEditAColor(final Parameter parameter2) {
        this.parameter = parameter2;
        {
            MColor color = new MColor() {
                @Override
                public Color getColor() {
                    return (Color) parameter2.getPreviousData();
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("Prev",color);
            mLabelAndElement.setBounds(new Rectangle(0,0,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            MColor color = new MColor() {
                @Override
                public Color getColor() {
                    return (Color) parameter2.getNewData();
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("New",color);
            mLabelAndElement.setBounds(new Rectangle(0,20,getBounds().width,20));
            add(mLabelAndElement);
        }

        Color color = (Color) parameter2.getNewData();
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsv);
        alpha = color.getAlpha() / 255.0f;

        {
            h = new MFloatSelectionButton(hsv[0] * 360);
            h.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    hsv[0] = h.getData() / 360;
                    update();
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("H", h);
            mLabelAndElement.setBounds(new Rectangle(0,20,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            s = new MFloatSelectionButton(hsv[1] * 100);
            s.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    hsv[1] = s.getData() / 100;
                    update();
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("S", s);
            mLabelAndElement.setBounds(new Rectangle(0,20,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            v = new MFloatSelectionButton(hsv[2] * 100);
            v.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    hsv[2] = v.getData() / 100;
                    update();
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("V", v);
            mLabelAndElement.setBounds(new Rectangle(0,20,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            a = new MFloatSelectionButton(alpha * 100);
            a.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    alpha = a.getData() / 100;
                    update();
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("A", a);
            mLabelAndElement.setBounds(new Rectangle(0,20,getBounds().width,20));
            add(mLabelAndElement);
        }
    }

    private float[] hsv = new float[3];
    private float alpha = 0;

    public void update() {
        if (hsv[2] > 1) hsv[2] = 1;
        if (hsv[2] < 0) hsv[2] = 0;
        if (hsv[1] > 1) hsv[1] = 1;
        if (hsv[1] < 0) hsv[1] = 0;
        parameter.setNewData(new AColor(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) & 0xffffff | ((int)(alpha * 255)) << 24, true ));
        h.setData((float) Math.floor(hsv[0] * 360));
        s.setData((float) Math.floor(hsv[1] * 100));
        v.setData((float) Math.floor(hsv[2] * 100));
        a.setData((float) Math.floor(alpha * 100));
        h.updateSelected();
        s.updateSelected();
        v.updateSelected();
        a.updateSelected();
    }
    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        // draw CoolRect
        int width = getBounds().width - 60;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int shademodel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        worldrenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

        int rgb = Color.HSBtoRGB(hsv[0], hsv[1], 1);
        float r = (rgb >> 16 & 255) / 255.0f;
        float g = (rgb >> 8 & 255) / 255.0f;
        float b = (rgb & 255) / 255.0f;
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glColor4f(0,0,0,alpha);GL11.glVertex3i(25+width ,45, 0);
        GL11.glColor4f(0,0,0,alpha);GL11.glVertex3i(10+width , 45, 0);
        GL11.glColor4f(r,g,b,alpha);GL11.glVertex3i(25+width , 45+width, 0);

        GL11.glColor4f(0,0,0,alpha); GL11.glVertex3i(10+width , 45, 0);
        GL11.glColor4f(r,g,b,alpha);GL11.glVertex3i(10+width , 45 + width, 0);
        GL11.glColor4f(r,g,b,alpha);GL11.glVertex3i(25+width , 45+width, 0);
        GL11.glEnd();
        rgb = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
        r = (rgb >> 16 & 255) / 255.0f;
        g = (rgb >> 8 & 255) / 255.0f;
        b = (rgb & 255) / 255.0f;
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glColor4f(r,g,b,0);GL11.glVertex3i(50+width ,45, 0);
        GL11.glColor4f(r,g,b,0);GL11.glVertex3i(35+width , 45, 0);
        GL11.glColor4f(r,g,b,1);GL11.glVertex3i(50+width , 45+width, 0);

        GL11.glColor4f(r,g,b,0); GL11.glVertex3i(35+width , 45, 0);
        GL11.glColor4f(r,g,b,1);GL11.glVertex3i(35+width , 45 + width, 0);
        GL11.glColor4f(r,g,b,1);GL11.glVertex3i(50+width , 45+width, 0);
        GL11.glEnd();


        float radius = width/2f;
        float cx = 5 + radius;
        float cy = 45 + radius;

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glColor4f(1,1,1,alpha);
        GL11.glVertex3f(cx,cy,0);
        for (int i = 0; i <= 360; i++) {
            float rad = 3.141592653f * i / 180;
            int rgb2 = Color.HSBtoRGB(i / 360f, 1, hsv[2]);
            float r2 = (rgb2 >> 16 & 255) / 255.0f;
            float g2 = (rgb2 >> 8 & 255) / 255.0f;
            float b2 = (rgb2 & 255) / 255.0f;
            GL11.glColor4f(r2,g2,b2, alpha);
            GL11.glVertex3f(MathHelper.cos(rad) * radius + cx, MathHelper.sin(rad) * radius + cy, 0);
        }
        GL11.glEnd();
        GL11.glShadeModel(shademodel);

        GL11.glColor4f(1,1,1,1);
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        float rad2 = 2 * 3.141592653f * hsv[0] ;
        float x = 5 + radius + (MathHelper.cos(rad2)) * hsv[1] * radius;
        float y = 45 + radius + (MathHelper.sin(rad2))* hsv[1] * radius;
        for (int i = 0; i < 100; i++) {
            float rad = 2 * 3.141592653f * (i / 100f);
            worldrenderer.pos(MathHelper.sin(rad) * 2 + x, MathHelper.cos(rad) * 2 + y, 0).endVertex();
        }
        tessellator.draw();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(8+width, 45 + (hsv[2]) * width, 0.5).endVertex();
        worldrenderer.pos(27+width, 45 + (hsv[2]) * width, 0.5).endVertex();
        tessellator.draw();

        worldrenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldrenderer.pos(33+width, 45 + (alpha) * width, 0.5).endVertex();
        worldrenderer.pos(52+width, 45 + (alpha) * width, 0.5).endVertex();
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1,1,1,1);
        GlStateManager.color(1,1,1,1);
    }
    private int selected = 0;

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        int width = getBounds().width - 60;
        float radius = width / 2f;
        float circleX = 5 + radius;
        float circleY = 45 + radius;
        selected = 0;
        {
            // check circle
            float dx = relMouseX - circleX;
            float dy = circleY - relMouseY;
            if (dx * dx + dy * dy <= radius * radius) {
                double theta = (MathHelper.atan2(dx, dy) / Math.PI * 180 + 270) % 360;
                hsv[0] = (float) theta / 360f;
                hsv[1] = MathHelper.sqrt_float(dx * dx + dy * dy) / radius;
                selected = 1;
            }
        }
        {
            if (10+width <= relMouseX && relMouseX <= 25 + width &&
                    45 <= relMouseY && relMouseY <= 45 + width) {
                hsv[2] = (relMouseY - 45) / (float)width;
                selected = 2;
            }
        }
        {
            if (35+width <= relMouseX && relMouseX <= 50 + width &&
                    45 <= relMouseY && relMouseY <= 45 + width) {
                alpha = (relMouseY - 45) / (float)width;
                selected = 3;
            }
        }
        update();
    }

    @Override
    public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
        int width = getBounds().width - 60;
        float radius = width / 2f;
        float circleX = 5 + radius;
        float circleY = 45 + radius;
        {
            // check circle
            float dx = relMouseX - circleX;
            float dy = circleY - relMouseY;
            if (selected == 1) {
                double theta = (MathHelper.atan2(dx, dy) / Math.PI * 180 + 270) % 360;
                hsv[0] = (float) theta / 360f;
                hsv[1] = MathHelper.clamp_float(MathHelper.sqrt_float(dx * dx + dy * dy) / radius, 0, 1);
            }
        }
        {
            if (selected == 2) {
                hsv[2] = MathHelper.clamp_float((relMouseY - 45) / (float)width, 0, 1);
            }
            if (selected == 3) {
                alpha = MathHelper.clamp_float((relMouseY - 45) / (float)width, 0, 1);
            }
        }
        update();
    }

    @Override
    public void onBoundsUpdate() {
        int cnt = 0;
        for (MPanel panel :getChildComponents()){
            panel.setSize(new Dimension(getBounds().width, 20)); cnt++;
            if (cnt > 2) {
                panel.setPosition(new Point(0, getBounds().width + (cnt - 3) * 20));
            }
        }
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditAColor> {

        @Override
        public ValueEditAColor createValueEdit(Parameter parameter) {
            return new ValueEditAColor(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return 0;
        }

        @Override
        public Object cloneObj(Object object) {
            return object;
        }
    }
}
