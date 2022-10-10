package kr.syeyoung.dungeonsguide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class YoMamaOutdated {

    Logger logger = LogManager.getLogger("YoMamaOutdated");

    public boolean isUsingOutdatedDg = true;
    String outdatedMessage;

    public YoMamaOutdated() {
        MinecraftForge.EVENT_BUS.register(this);
        this.check();
    }

    void check() {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet( "https://dungeonsguide.kingstefan26.workers.dev/outdated");
            Header[] haeders = {new BasicHeader("User-Agent", "DungeonsGuide/" + Main.VERSION), new BasicHeader("Player-Uuid", Minecraft.getMinecraft().getSession().getPlayerID())};
            httpget.setHeaders(haeders);
            HttpResponse httpresponse = httpclient.execute(httpget);

            if (httpresponse.getStatusLine().getStatusCode() != 200) {
                outdatedMessage = IOUtils.toString(httpresponse.getEntity().getContent(), StandardCharsets.UTF_8);
            }else {
                isUsingOutdatedDg = false;
            }

        } catch (Exception ignored) {
        }

    }

    private boolean showedError = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!showedError && isUsingOutdatedDg) {
            showedError = true;
            event.gui = new GuiScreen() {
                @Override
                public void initGui() {
                    ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                    this.buttonList.add(new GuiButton(0, sr.getScaledWidth()/2-100,sr.getScaledHeight()-70 ,"Close Minecraft"));
                }

                @Override
                protected void actionPerformed(GuiButton button) throws IOException {
                    super.actionPerformed(button);
                    if (button.id == 0) {
                        FMLCommonHandler.instance().exitJava(-1,true);
                    }
                }

                @Override
                public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                    super.drawBackground(1);

                    ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                    FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
                    String text = "This DungeonsGuide installation seems to be invalid";
                    fontRenderer.drawString(text, (sr.getScaledWidth()-fontRenderer.getStringWidth(text))/2,40,0xFFFF0000);
                    String text1 = "Message from our server:";
                    fontRenderer.drawString(text1, (sr.getScaledWidth()-fontRenderer.getStringWidth(text1))/2, (int) (40+fontRenderer.FONT_HEIGHT*1.5),0xFFFF0000);

                    int tenth = sr.getScaledWidth() / 10;

                    Gui.drawRect(tenth, 70,sr.getScaledWidth()-tenth, sr.getScaledHeight()-80, 0xFF5B5B5B);
                    clip(sr, tenth, 70,sr.getScaledWidth()-2*tenth, sr.getScaledHeight()-150);
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);


                    fontRenderer.drawString(outdatedMessage, tenth+2,fontRenderer.FONT_HEIGHT + 72, 0xFFFFFFFF);

                    GL11.glDisable(GL11.GL_SCISSOR_TEST);

                    super.drawScreen(mouseX, mouseY, partialTicks);
                }

                public void clip(ScaledResolution resolution, int x, int y, int width, int height) {
                    if (width < 0 || height < 0) return;

                    int scale = resolution.getScaleFactor();
                    GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
                }
            };
        }
    }

}
