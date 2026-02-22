package kr.syeyoung.modapi.v1_8_9.mod;

import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public class TextureLoader {

    public static final TextureLoader INSTANCE = new TextureLoader();

    public TextureAtlasSprite sprite;

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        sprite = event.map.registerSprite(new ResourceLocation("dungeonsguide", "arrow"));
    }


}
