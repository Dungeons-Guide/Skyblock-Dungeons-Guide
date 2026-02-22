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
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.rendering.UFontCalculator;

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


    private ResourceIdentifier sampleBlock = new ResourceIdentifier("minecraft:textures/blocks/diamond_block.png");


    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        Size size = buildContext.getSize();
        double halfWidth = size.getWidth() / 2;

        double offset = etherwarpOffset.getValue();
        double leeway = etherwarpLeeway.getValue();

        double scaleFactor = buildContext.getAbsBounds().getWidth() / size.getWidth();

        context.ctx().pushMatrix();
        context.ctx().translate(halfWidth, size.getHeight() - 24, 0);

        context.drawScaledCustomSizeModalRect(sampleBlock, -8, 0, 0, 0, 16, 16,
                16, 16, 16, 16);

        context.drawEtherwarpPreviewBackground(halfWidth, offset, leeway, (float) buildContext.getAbsBounds().getWidth() * 1/3,  (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2), ModAPI.getAPI().getDisplayHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight() - 16*scaleFactor));


        context.drawLine(0, 8, 2*halfWidth/3, 8, 0xFFFFFFFF, 1.0f);
        context.drawLine(0, 0, offset*16, 0, 0xFFFFFFFF, 1.0f);
        context.drawLine(24-leeway*16, -80, 24, -80, 0xFFFFFFFF, 1.0f);
        context.drawLine(40, -64, 40+leeway*16, -64, 0xFFFFFFFF, 1.0f);
        context.drawLine(24-leeway*8, -83, 50, -90, 0xFFFFFFFF, 1.0f);
        context.drawLine(50, -90, 40+leeway*8, -67, 0xFFFFFFFF, 1.0f);
        context.drawLine(-8, 0, 8, 0, 0xFFFFFF00, 1.0f);

        UFontCalculator fr = ModAPI.getAPI().getFontCalculator();
        context.drawString(maxEtherwarp.getValue()+" Blocks", (int) (halfWidth/3) - fr.getStringWidth(maxEtherwarp.getValue()+" Blocks")/2, 10, 0xFFFFFFFF);
        context.drawString(String.format("%.2f Blocks", offset), -fr.getStringWidth(String.format("%.2f Blocks", offset))-3, 0, 0xFFFFFFFF);
        context.drawString(String.format("%.4f Blocks", leeway), 52, -94, 0xFFFFFFFF);
        context.drawString("Can etherwarp to", (int) (-halfWidth/3), (int) -(halfWidth/3), 0xFFFFFFFF);
        context.drawString("Yellow Face", (int) (-halfWidth/3), (int) -(halfWidth/3)+8, 0xFFFFFFFF);
        context.drawString("Can't etherwarp", 30, (int) -10, 0xFFFFFFFF);

        context.drawDonut(
                (int) -halfWidth, (int) -size.getHeight(),
                halfWidth, 40, 0xFFFFFFFF,
                (float) buildContext.getAbsBounds().getWidth() * 1/3,
                1.0f,
                (float) (buildContext.getAbsBounds().getX()+buildContext.getAbsBounds().getWidth()/2),
                ModAPI.getAPI().getDisplayHeight() - (float) (buildContext.getAbsBounds().getY() + buildContext.getAbsBounds().getHeight() - 16*scaleFactor),
                0.0f);

        context.drawScaledCustomSizeModalRect(sampleBlock, 24, -80, 0, 0, 16, 16,
                16, 16, 16, 16);


        context.ctx().popMatrix();
    }
}
