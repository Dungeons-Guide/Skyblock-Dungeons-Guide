package kr.syeyoung.dungeonsguide.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TitleRender {


    private static TitleRender instance;
    public static TitleRender getInstance() {
        return instance == null ? instance = new TitleRender() : instance;
    }

    private TitleRender() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected static int titlesTimer;
    /**
     * The current title displayed
     */
    protected static String displayedTitle = "";
    /**
     * The current sub-title displayed
     */
    protected static String displayedSubTitle = "";
    /**
     * The time that the title take to fade in
     */
    protected static int titleFadeIn;
    /**
     * The time that the title is display
     */
    protected static int titleDisplayTime;

    /**
     * The time that the title take to fade out
     */
    protected static int titleFadeOut;
    Minecraft mc = Minecraft.getMinecraft();

    FontRenderer fontRenderer = mc.fontRendererObj;


    @SubscribeEvent
    public void onClinetTick(TickEvent.ClientTickEvent e){
        if(e.phase == TickEvent.Phase.START){
            tick();
        }
    }

    @SubscribeEvent
    public void onGameOverLay(RenderGameOverlayEvent.Post e){

        if(e.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH){
            GlStateManager.enableBlend();
            draw(e.partialTicks);
        }
    }


    void tick(){
        if (this.titlesTimer > 0) {
            --this.titlesTimer;
            if (this.titlesTimer <= 0) {
                this.displayedTitle = "";
                this.displayedSubTitle = "";
            }
        }
    }


    public static void displayTitle(String title, String subTitle, int timeFadeIn, int displayTime, int timeFadeOut) {
        if (title == null && subTitle == null && timeFadeIn < 0 && displayTime < 0 && timeFadeOut < 0) {
            displayedTitle = "";
            displayedSubTitle = "";
            titlesTimer = 0;
            return;
        }
        if (title != null) {
            displayedTitle = title;
            titlesTimer = titleFadeIn + titleDisplayTime + titleFadeOut;
            return;
        }
        if (subTitle != null) {
            displayedSubTitle = subTitle;
            return;
        }
        if (timeFadeIn >= 0) {
            titleFadeIn = timeFadeIn;
        }
        if (displayTime >= 0) {
            titleDisplayTime = displayTime;
        }
        if (timeFadeOut >= 0) {
            titleFadeOut = timeFadeOut;
        }
        if (titlesTimer > 0) {
            titlesTimer = titleFadeIn + titleDisplayTime + titleFadeOut;
        }
    }

    void draw(float partialTicks){
        float g;
        int l;
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int i = scaledResolution.getScaledWidth();
        int j = scaledResolution.getScaledHeight();
        if (titlesTimer > 0) {
            g = titlesTimer - partialTicks;
            l = 255;
            if (titlesTimer > titleFadeOut + titleDisplayTime) {
                float h = (float)(titleFadeIn + titleDisplayTime + titleFadeOut) - g;
                l = (int)(h * 255.0f / (float)titleFadeIn);
            }
            if (titlesTimer <= titleFadeOut) {
                float h = g;
                l = (int)(h * 255.0f / (float)titleFadeOut);
            }
            if ((l = MathHelper.clamp_int(l, 0, 255)) > 8) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(i / 2, j / 2, 0.0f);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4.0f, 4.0f, 4.0f);
                int m = l << 24 & 0xFF000000;
                fontRenderer.drawString(displayedTitle, -fontRenderer.getStringWidth(displayedTitle) / 2, -10.0f, 0xFFFFFF | m, true);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.0f, 2.0f, 2.0f);
                fontRenderer.drawString(displayedSubTitle, -fontRenderer.getStringWidth(displayedSubTitle) / 2, 5.0f, 0xFFFFFF | m, true);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

}
