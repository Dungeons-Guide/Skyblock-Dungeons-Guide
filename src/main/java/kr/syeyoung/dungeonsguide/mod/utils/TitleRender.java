package kr.syeyoung.dungeonsguide.mod.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * STOLEN FROM VANILLA 1.8.9, DONT @ ME
 * CREDS TO MOJANG AND THE CREW
 */
public class TitleRender {

    Logger logger = LogManager.getLogger("TitleRender");

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
        if (!(e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || e.type == RenderGameOverlayEvent.ElementType.JUMPBAR)) return;
        GlStateManager.enableBlend();
        draw(e.partialTicks);
    }


    void tick(){
        if (titlesTimer > 0) {
            --titlesTimer;
            if (titlesTimer <= 0) {
                displayedTitle = "";
                displayedSubTitle = "";
            }
        }
    }


    public static void clearTitle(){
        displayedTitle = "";
        displayedSubTitle = "";
        titlesTimer = 0;
    }


    public static void displayTitle(@NotNull String title, String subTitle, int timeFadeIn, int displayTime, int timeFadeOut) {
        displayedTitle = title;
        displayedSubTitle = subTitle;
        titleFadeIn = timeFadeIn;
        titleDisplayTime = displayTime;
        titleFadeOut = timeFadeOut;
        titlesTimer = titleFadeIn + titleDisplayTime + titleFadeOut;
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
                GlStateManager.scale(6.0f, 6.0f, 6.0f);
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
