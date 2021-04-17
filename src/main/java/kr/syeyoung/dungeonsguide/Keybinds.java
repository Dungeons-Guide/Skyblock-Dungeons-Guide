package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
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
    public static KeyBinding freezeLines;

    public static void register()
    {
        editingSession = new KeyBinding("start editing session", Keyboard.KEY_NONE, "key.categories.dungeonsguide");
        ClientRegistry.registerKeyBinding(editingSession);
        sendBombdefuse = new KeyBinding("send and save bombdefuse solution", Keyboard.KEY_F, "key.categories.dungeonsguide");
        ClientRegistry.registerKeyBinding(sendBombdefuse);
        nextSecret = new KeyBinding("navigate to next secret. (Req option enabled at /dg)", Keyboard.KEY_NONE, "key.categories.dungeonsguide");
        ClientRegistry.registerKeyBinding(nextSecret);
        togglePathfind = new KeyBinding("toggle Pathfind. (Req option enabled at /dg)", Keyboard.KEY_NONE, "key.categories.dungeonsguide");
        ClientRegistry.registerKeyBinding(togglePathfind);
        freezeLines = new KeyBinding("Toggle freeze pathfind lines", Keyboard.KEY_NONE, "key.categories.dungeonsguide");
        ClientRegistry.registerKeyBinding(freezeLines);
    }

    public static boolean togglePathfindStatus = false;

    @SubscribeEvent
    public void onTogglePathfindStatus(InputEvent.KeyInputEvent keyInputEvent) {
        if (togglePathfind.isKeyDown())
            togglePathfindStatus = !togglePathfindStatus;

        if (freezeLines.isKeyDown()) {
            FeatureRegistry.SECRET_FREEZE_LINES.setEnabled(!FeatureRegistry.SECRET_FREEZE_LINES.isEnabled());
            try {
                Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §fToggled Pathfind Freeze to §e"+(FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() ? "on":"off")));
            } catch (Exception ignored) {}
        }
    }
}
