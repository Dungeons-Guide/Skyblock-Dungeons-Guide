package kr.syeyoung.modapi.v1_8_9.mod;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TextureLoader {

    public static final TextureLoader INSTANCE = new TextureLoader();

    public TextureAtlasSprite sprite;

    @SubscribeEvent()
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        sprite = event.map.registerSprite(new ResourceLocation("dungeonsguide", "arrow"));
    }


}
