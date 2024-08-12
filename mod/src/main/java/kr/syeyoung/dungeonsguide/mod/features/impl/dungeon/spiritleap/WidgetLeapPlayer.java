package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class WidgetLeapPlayer extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "name")
    public final BindableAttribute<String> playerName = new BindableAttribute<>(String.class);
    @Bind(variableName = "texture")
    public final BindableAttribute<String> texture = new BindableAttribute<>(String.class);

    @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Integer> backgroundColor = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "borderColor")
    public final BindableAttribute<Integer> borderColor = new BindableAttribute<>(Integer.class);

    private WarpTarget warpTarget;

    public WidgetLeapPlayer(WarpTarget target, TabListEntry entry) {
        super(new ResourceLocation("dungeonsguide:gui/features/spiritleap/leapplayer.gui"));
        this.backgroundColor.setValue(0xFF555555);
        this.borderColor.setValue(0xFFFFFFFF);

        this.playerName.setValue(target.getItemStack().getDisplayName());
        this.texture.setValue(entry != null ? entry.getLocationSkin().toString() : "dungeonsguide:map/maptexture.png");
        this.warpTarget = target;
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {

        getDomElement().setCursor(EnumCursor.POINTING_HAND);

        this.backgroundColor.setValue(0xFF777777);

        return true;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        this.backgroundColor.setValue(0xFF555555);
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        getDomElement().obtainFocus();

        this.backgroundColor.setValue(0xFF888888);

        return true;
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (getDomElement().getAbsBounds().contains(absMouseX, absMouseY) && getDomElement().isFocused()) {
            this.backgroundColor.setValue(0xFF777777);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(this.warpTarget.getSlotId(), 0, 0);
        } else {
            this.backgroundColor.setValue(0xFF555555);
        }
    }
}
