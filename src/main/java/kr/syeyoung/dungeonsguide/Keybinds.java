package kr.syeyoung.dungeonsguide;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class Keybinds
{
    public static KeyBinding opengui;

    public static void register()
    {
        opengui = new KeyBinding("ay", Keyboard.KEY_R, "key.categories.misc");

        ClientRegistry.registerKeyBinding(opengui);
    }
}
