package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
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
    private String clazz;

    public WidgetLeapPlayer(WarpTarget target, TabListEntry entry) {
        super(new ResourceLocation("dungeonsguide:gui/features/spiritleap/leapplayer.gui"));
        this.backgroundColor.setValue(0xFF555555);
        this.borderColor.setValue(0xFFFFFFFF);

        this.playerName.setValue(target.getItemStack().getDisplayName());
        this.texture.setValue(entry != null ? entry.getLocationSkin().toString() : "dungeonsguide:map/maptexture.png");
        this.warpTarget = target;

        if (entry == null) {
            this.clazz = "";
        } else {
            int idx = entry.getEffectiveName().indexOf("§r§f(");
            String clazzThing = entry.getEffectiveName().substring(idx);

            this.clazz = TextUtils.stripColor(clazzThing).substring(1);
        }
        // based on Adaptive
        if (clazz.startsWith("Archer")) {
            this.borderColor.setValue(0xFF5cae76); // green
            this.playerName.setValue("§c[A] §r"+target.getItemStack().getDisplayName());
        } else if (clazz.startsWith("Berserk")) {
            this.borderColor.setValue(0xFFdb4d46); // red
            this.playerName.setValue("§c[B] §r"+target.getItemStack().getDisplayName());
        } else if (clazz.startsWith("Mage")) {
            this.borderColor.setValue(0xFFba75e6); // purple
            this.playerName.setValue("§c[M] §r"+target.getItemStack().getDisplayName());
        } else if (clazz.startsWith("Healer")) {
            this.borderColor.setValue(0xFFe4b64e); // yellow
            this.playerName.setValue("§c[H] §r"+target.getItemStack().getDisplayName());
        } else if (clazz.startsWith("Tank")) {
            this.borderColor.setValue(0xFF8fd1c9); // blue
            this.playerName.setValue("§c[T] §r"+target.getItemStack().getDisplayName());
        } else if (clazz.startsWith("DEAD")) {
            this.borderColor.setValue(0xFF333333); // black
            this.playerName.setValue("§c[Dead] §r"+target.getItemStack().getDisplayName());
        }
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        if (clazz.startsWith("DEAD")) return false;

        getDomElement().setCursor(EnumCursor.POINTING_HAND);

        this.backgroundColor.setValue(0xFF777777);

        return true;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (clazz.startsWith("DEAD")) return;
        this.backgroundColor.setValue(0xFF555555);
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (clazz.startsWith("DEAD")) return false;
        getDomElement().obtainFocus();

        this.backgroundColor.setValue(0xFF888888);

        return true;
    }

    @Override
    public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
        if (clazz.startsWith("DEAD")) return;
        if (getDomElement().getAbsBounds().contains(absMouseX, absMouseY) && getDomElement().isFocused()) {
            this.backgroundColor.setValue(0xFF777777);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            GuiScreenAdapterChestOverride.getAdapter(getDomElement()).emulateClick(this.warpTarget.getSlotId(), 0, 0);
        } else {
            this.backgroundColor.setValue(0xFF555555);
        }
    }
}
