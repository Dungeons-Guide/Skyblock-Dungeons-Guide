package kr.syeyoung.modapi.v1_21_5.gui;

import kr.syeyoung.modapi.rendering.URenderContext;
import net.minecraft.client.gui.DrawContext;

public class URenderContextImpl implements URenderContext {
    private DrawContext context;
    public URenderContextImpl(DrawContext context) {
        this.context = context;
    }
}
