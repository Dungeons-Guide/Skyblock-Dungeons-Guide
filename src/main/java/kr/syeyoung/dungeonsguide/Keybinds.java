package kr.syeyoung.dungeonsguide;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class Keybinds
{
    public static KeyBinding editingSession;
    public static KeyBinding ringMenuForSecretEdit;

    public static void register()
    {
        if (e.DEBUG) {
            editingSession = new KeyBinding("start editing session", Keyboard.KEY_R, "key.categories.misc");
            ClientRegistry.registerKeyBinding(editingSession);
        }
    }
}
