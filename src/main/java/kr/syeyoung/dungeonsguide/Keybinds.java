package kr.syeyoung.dungeonsguide;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class Keybinds
{
    public static KeyBinding editingSession;
    public static KeyBinding sendBombdefuse;
    public static KeyBinding nextSecret;
    public static KeyBinding togglePathfind;

    public static void register()
    {
        editingSession = new KeyBinding("start editing session", Keyboard.KEY_NONE, "key.categories.misc");
        ClientRegistry.registerKeyBinding(editingSession);
        sendBombdefuse = new KeyBinding("send and save bombdefuse solution", Keyboard.KEY_F, "key.categories.misc");
        ClientRegistry.registerKeyBinding(sendBombdefuse);
        nextSecret = new KeyBinding("navigate to next secret. (Req option enabled at /dg)", Keyboard.KEY_NONE, "key.categories.misc");
        ClientRegistry.registerKeyBinding(nextSecret);
        togglePathfind = new KeyBinding("toggle Pathfind. (Req option enabled at /dg)", Keyboard.KEY_NONE, "key.categories.misc");
        ClientRegistry.registerKeyBinding(togglePathfind);
    }

    public static boolean togglePathfindStatus = false;

    @SubscribeEvent
    public void onTogglePathfindStatus(InputEvent.KeyInputEvent keyInputEvent) {
        if (togglePathfind.isKeyDown())
            togglePathfindStatus = !togglePathfindStatus;
    }
}
