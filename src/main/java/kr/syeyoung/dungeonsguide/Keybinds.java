package kr.syeyoung.dungeonsguide;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class Keybinds
{
    public static KeyBinding editingSession;
    public static KeyBinding sendBombdefuse;
    public static KeyBinding ringMenuForSecretEdit;

    public static void register()
    {
        editingSession = new KeyBinding("start editing session", Keyboard.KEY_NONE, "key.categories.misc");
        ClientRegistry.registerKeyBinding(editingSession);
        sendBombdefuse = new KeyBinding("send and save bombdefuse solution", Keyboard.KEY_F, "key.categories.misc");
        ClientRegistry.registerKeyBinding(sendBombdefuse);
    }
}
