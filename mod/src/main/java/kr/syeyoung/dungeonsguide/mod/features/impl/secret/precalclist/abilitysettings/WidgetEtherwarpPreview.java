package kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.abilitysettings;

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderManager;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import kr.syeyoung.modapi.ModAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Collections;
import java.util.List;

public class WidgetEtherwarpPreview extends AnnotatedExportOnlyWidget implements Renderer, Layouter {

    @Export(attributeName = "offset")
    public final BindableAttribute<Double> etherwarpOffset = new BindableAttribute<>(Double.class);
    @Export(attributeName = "leeway")
    public final BindableAttribute<Double> etherwarpLeeway = new BindableAttribute<>(Double.class);
    @Export(attributeName = "length")
    public final BindableAttribute<Integer> maxEtherwarp = new BindableAttribute<>(Integer.class);


    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return SingleChildPassingLayouter.INSTANCE.layout(buildContext, constraintBox);
    }


    private ResourceLocation sampleBlock = new ResourceLocation("minecraft:textures/blocks/diamond_block.png");


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        Size size = buildContext.getSize();
        double halfWidth = size.getWidth() / 2;

        double offset = etherwarpOffset.getValue();
        double leeway = etherwarpLeeway.getValue();

        double scaleFactor = buildContext.getAbsBounds().getWidth() / size.getWidth();

        GlStateManager.pushMatrix();
        GlStateManager.translate(halfWidth, size.getHeight() - 24, 0);

        Minecraft.getMinecraft().getTextureManager().bindTexture(sampleBlock);
        context.drawScaledCustomSizeModalRect(-8, 0, 0, 0, 16, 16,
                16, 16, 16, 16);

        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/etherwarppreview");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", (float) buildContext.getAbsBounds().getWidth() * 1/3);
        shaderProgram.uploadUniform("centerPos",
                (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                ModAPI.getAPI().getDisplayHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight() - 16*scaleFactor));
        shaderProgram.uploadUniform("smoothness", 0.0f);

        GlStateManager.color(1.0f, 0f, 0f, 0.3f);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldRenderer.pos(0,offset * 16, 0).endVertex();
        worldRenderer.pos(-halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();
        worldRenderer.pos(-halfWidth, offset * 16, 0).endVertex();
        tessellator.draw();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldRenderer.pos(0,offset * 16, 0).endVertex();
        worldRenderer.pos(halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();
        worldRenderer.pos(halfWidth, offset * 16, 0).endVertex();
        tessellator.draw();
        context.drawRect(-halfWidth, offset* 16, halfWidth, 40, 0x4DFF0000);


        // leeway...
        // sample block is 2 right, 5 up
        // top left: 1.5, -5 =>
        // bottom right: 2.5, -4

        {
            double slope1 = (5+offset) / (1.5 - leeway);
            double slope2 = (4+offset) / (2.5 + leeway);


            GlStateManager.color(0.0f, 1f, 0f, 0.3f);
            worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
            worldRenderer.pos(0,offset * 16, 0).endVertex();
            worldRenderer.pos(-halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();
            worldRenderer.pos(halfWidth, -slope1 * halfWidth + offset* 16, 0).endVertex();
            worldRenderer.pos(24-leeway*16, -80, 0).endVertex();
            worldRenderer.pos(24, -80, 0).endVertex();
            worldRenderer.pos(24, -64, 0).endVertex();
            worldRenderer.pos(40+leeway*16, -64, 0).endVertex();
            worldRenderer.pos(halfWidth, -slope2 * halfWidth + offset* 16, 0).endVertex();
            worldRenderer.pos(halfWidth, -offset/0.5 * halfWidth + offset* 16, 0).endVertex();

            tessellator.draw();

            GlStateManager.color(1.0f, 0f, 0f, 0.3f);
            worldRenderer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION);
            worldRenderer.pos(24-leeway*16, -80, 0).endVertex();
            worldRenderer.pos(40, -80, 0).endVertex();
            worldRenderer.pos(halfWidth, -slope1 * halfWidth + offset* 16, 0).endVertex();
            worldRenderer.pos(40, -64, 0).endVertex();
            worldRenderer.pos(halfWidth, -slope2 * halfWidth + offset* 16, 0).endVertex();
            worldRenderer.pos(40+leeway*16, -64, 0).endVertex();
//            worldRenderer.pos(halfWidth, -slope1 * halfWidth + offset* 16, 0).endVertex();
//            worldRenderer.pos(halfWidth, -slope2 * halfWidth + offset* 16, 0).endVertex();

            tessellator.draw();
        }

        GL20.glUseProgram(0);
        GlStateManager.disableTexture2D();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldRenderer.pos(0, 8, 0).endVertex();
        worldRenderer.pos(2*halfWidth/3, 8, 0).endVertex();
        worldRenderer.pos(0, 0, 0).endVertex();
        worldRenderer.pos(0, offset* 16, 0).endVertex();
        worldRenderer.pos(24-leeway*16, -80, 0).endVertex();
        worldRenderer.pos(24, -80, 0).endVertex();
        worldRenderer.pos(40, -64, 0).endVertex();
        worldRenderer.pos(40+leeway*16, -64, 0).endVertex();

        worldRenderer.pos(24-leeway*8, -83, 0).endVertex();
        worldRenderer.pos(50, -90, 0).endVertex();
        worldRenderer.pos(50, -90, 0).endVertex();
        worldRenderer.pos(40+leeway*8, -67, 0).endVertex();

        tessellator.draw();

        GlStateManager.color(1.0f, 1.0f, 0f, 1.0f);
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        worldRenderer.pos(-8,0,0).endVertex();
        worldRenderer.pos(8,0,0).endVertex();
        tessellator.draw();


        GlStateManager.enableTexture2D();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString(maxEtherwarp.getValue()+" Blocks", (int) (halfWidth/3) - fr.getStringWidth(maxEtherwarp.getValue()+" Blocks")/2, 10, 0xFFFFFFFF);
        fr.drawString(String.format("%.2f Blocks", offset), -fr.getStringWidth(String.format("%.2f Blocks", offset))-3, 0, 0xFFFFFFFF);
        fr.drawString(String.format("%.4f Blocks", leeway), 52, -94, 0xFFFFFFFF);

        fr.drawString("Can etherwarp to", (int) (-halfWidth/3), (int) -(halfWidth/3), 0xFFFFFFFF);
        fr.drawString("Yellow Face", (int) (-halfWidth/3), (int) -(halfWidth/3)+8, 0xFFFFFFFF);


        fr.drawString("Can't etherwarp", 30, (int) -10, 0xFFFFFFFF);

        shaderProgram = ShaderManager.getShader("shaders/donut");
        shaderProgram.useShader();
        shaderProgram.uploadUniform("radius", (float) buildContext.getAbsBounds().getWidth() * 1/3);
        shaderProgram.uploadUniform("centerPos",
                (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                ModAPI.getAPI().getDisplayHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight() - 16*scaleFactor));
        shaderProgram.uploadUniform("smoothness", 0.0f);
        shaderProgram.uploadUniform("thickness", 1.0f);
        context.drawRect(-halfWidth,-size.getHeight(),halfWidth, 40, 0xFFFFFFFF);
        GL20.glUseProgram(0);


        Minecraft.getMinecraft().getTextureManager().bindTexture(sampleBlock);
        context.drawScaledCustomSizeModalRect(24, -80, 0, 0, 16, 16,
                16, 16, 16, 16);

        GlStateManager.enableCull();


        GlStateManager.popMatrix();
    }
}
