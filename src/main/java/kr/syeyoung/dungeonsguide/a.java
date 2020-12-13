package kr.syeyoung.dungeonsguide;

import com.mojang.authlib.exceptions.AuthenticationException;
import kr.syeyoung.dungeonsguide.d.c;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Mod(modid = a.b, version = a.c)
public class a
{
    public static final String b = "skyblock_dungeons_guide";
    public static final String c = "1.0";

    private static a a;

    private kr.syeyoung.dungeonsguide.c d;

    @EventHandler
    public void a(FMLInitializationEvent a)
    {

        this.a = this;
        d.init(a);
    }

    @EventHandler
    public void a(FMLPreInitializationEvent a) {
        b b = new b();
        String c = null;
        try {
            c = b.b();
            if (c != null) {
                this.a = this;
                URL.setURLStreamHandlerFactory(new c(b));
                LaunchClassLoader d = (LaunchClassLoader) a.class.getClassLoader();
                d.addURL(new URL("z:///"));

                try {
                    this.d = new e(b);
                    this.d.pre(a);
                } catch (Exception e) {
                    e.printStackTrace();

                    a(new String[]{
                            "Couldn't load Dungeons Guide",
                            "Please contact developer if this problem persists after restart"
                    });
                }
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        a(new String[]{
                "Can't validate current installation of Dungeons Guide",
                "Steps to fix",
                "1. check if other people can't join minecraft servers. If they can't it's impossible to validate",
                "2. restart minecraft launcher",
                "3. make sure you're on the right account",
                "4. restart your computer",
                "If the problem persists after following these steps, please contact developer",
                "If you haven't purchased the mod, please consider doing so"
        });
    }
//    @EventHandler
//    public void pre(FMLPreInitializationEvent event) {
//                dungeonsGuideMain = this;
//                URL.setURLStreamHandlerFactory(new DGURLStreamHandlerFactory(null));
//                try {
//                    dungeonsGuideInterface = new DungeonsGuide(null);
//                    dungeonsGuideInterface.pre(event);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//    }

    public void a(final String[] a) {
        final GuiScreen b = new GuiErrorScreen(null, null) {

            @Override
            public void handleMouseInput() {
            }

            @Override
            public void handleKeyboardInput() {
            }

            @Override
            public void drawScreen(int par1, int par2, float par3) {
                drawDefaultBackground();
                for (int i = 0; i < a.length; ++i) {
                    drawCenteredString(fontRendererObj, a[i], width / 2, height / 3 + 12 * i, 0xFFFFFFFF);
                }
            }
        };
        @SuppressWarnings("serial") CustomModLoadingErrorDisplayException e = new CustomModLoadingErrorDisplayException() {

            @Override
            public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
                Minecraft.getMinecraft().displayGuiScreen(b);
            }

            @Override
            public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
            }
        };
        throw e;
    }
    public static a a() {
        return a;
    }
}
