package v1_21_11.mixins;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.v1_21_5.ModAPIImpl;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MessageHandler.class, priority = 550)
public class ChatMessageMixins {

    @WrapMethod(method = "onGameMessage")
    private void onGameMessage(Text message, boolean actionBar, Operation<Void> original) {
        ((ModAPIImpl)ModAPI.getAPI()).getEventListener().onGameMessage(message, actionBar, original);
    }
}