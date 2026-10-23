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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.List;
import kr.syeyoung.dungeonsguide.mod.shader.EtherwarpPreviewUniforms;
import kr.syeyoung.dungeonsguide.mod.shader.DonutUniforms;
import net.minecraft.client.gui.Font;

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


    private Identifier sampleBlock = Identifier.parse("minecraft:textures/blocks/diamond_block.png");


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        Size size = buildContext.getSize();
        double halfWidth = size.getWidth() / 2;

        double offset = etherwarpOffset.getValue();
        double leeway = etherwarpLeeway.getValue();

        double scaleFactor = buildContext.getAbsBounds().getWidth() / size.getWidth();

        context.pose().pushMatrix();
        context.pose().translate((float) halfWidth, (float) (size.getHeight() - 24));

        context.drawScaledCustomSizeModalRect(sampleBlock, -8, 0, 0, 0, 16, 16,
                16, 16, 16, 16);

        ShaderProgram shaderProgram = ShaderManager.getShader("shaders/etherwarppreview");
        EtherwarpPreviewUniforms etherwarpUniforms = new EtherwarpPreviewUniforms((float) buildContext.getAbsBounds().getWidth() * 1/3,
                (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                context.getImageHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight() - 16*scaleFactor),
                0.0f);

        float hw = (float) halfWidth, off = (float) offset, lw = (float) leeway;
        context.drawShapeTriangleFan(shaderProgram, etherwarpUniforms, 0x4DFF0000,
                0, off * 16,
                -hw, -off/0.5f * hw + off* 16,
                -hw, off * 16);
        context.drawShapeTriangleFan(shaderProgram, etherwarpUniforms, 0x4DFF0000,
                0, off * 16,
                hw, -off/0.5f * hw + off* 16,
                hw, off * 16);
        context.drawShape(shaderProgram, etherwarpUniforms, -halfWidth, offset* 16, halfWidth, 40, 0x4DFF0000);


        // leeway...
        // sample block is 2 right, 5 up
        // top left: 1.5, -5 =>
        // bottom right: 2.5, -4

        {
            float slope1 = (float) ((5+offset) / (1.5 - leeway));
            float slope2 = (float) ((4+offset) / (2.5 + leeway));


            context.drawShapeTriangleFan(shaderProgram, etherwarpUniforms, 0x4D00FF00,
                    0, off * 16,
                    -hw, -off/0.5f * hw + off* 16,
                    hw, -slope1 * hw + off* 16,
                    24-lw*16, -80,
                    24, -80,
                    24, -64,
                    40+lw*16, -64,
                    hw, -slope2 * hw + off* 16,
                    hw, -off/0.5f * hw + off* 16);

            context.drawShapeTriangleStrip(shaderProgram, etherwarpUniforms, 0x4DFF0000,
                    24-lw*16, -80,
                    40, -80,
                    hw, -slope1 * hw + off* 16,
                    40, -64,
                    hw, -slope2 * hw + off* 16,
                    40+lw*16, -64);
        }

        // 1px gl lines
        double lineWidth = 1 / scaleFactor;
        context.drawLine(0, 8, 2*halfWidth/3, 8, lineWidth, 0xFFFFFFFF);
        context.drawLine(0, 0, 0, offset* 16, lineWidth, 0xFFFFFFFF);
        context.drawLine(24-leeway*16, -80, 24, -80, lineWidth, 0xFFFFFFFF);
        context.drawLine(40, -64, 40+leeway*16, -64, lineWidth, 0xFFFFFFFF);

        context.drawLine(24-leeway*8, -83, 50, -90, lineWidth, 0xFFFFFFFF);
        context.drawLine(50, -90, 40+leeway*8, -67, lineWidth, 0xFFFFFFFF);

        context.drawLine(-8, 0, 8, 0, lineWidth, 0xFFFFFF00);

        Font fr = Minecraft.getInstance().font;
        context.drawString(maxEtherwarp.getValue()+" Blocks", (int) (halfWidth/3) - fr.width(maxEtherwarp.getValue()+" Blocks")/2, 10, 0xFFFFFFFF, false);
        context.drawString(String.format("%.2f Blocks", offset), -fr.width(String.format("%.2f Blocks", offset))-3, 0, 0xFFFFFFFF, false);
        context.drawString(String.format("%.4f Blocks", leeway), 52, -94, 0xFFFFFFFF, false);

        context.drawString("Can etherwarp to", (int) (-halfWidth/3), (int) -(halfWidth/3), 0xFFFFFFFF, false);
        context.drawString("Yellow Face", (int) (-halfWidth/3), (int) -(halfWidth/3)+8, 0xFFFFFFFF, false);


        context.drawString("Can't etherwarp", 30, (int) -10, 0xFFFFFFFF, false);

        shaderProgram = ShaderManager.getShader("shaders/donut");
        context.drawShape(shaderProgram, new DonutUniforms((float) buildContext.getAbsBounds().getWidth() * 1/3, 1.0f,
                        (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                        context.getImageHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight() - 16*scaleFactor),
                        0.0f),
                -halfWidth,-size.getHeight(),halfWidth, 40, 0xFFFFFFFF);


        context.drawScaledCustomSizeModalRect(sampleBlock, 24, -80, 0, 0, 16, 16,
                16, 16, 16, 16);


        context.pose().popMatrix();
    }
}
