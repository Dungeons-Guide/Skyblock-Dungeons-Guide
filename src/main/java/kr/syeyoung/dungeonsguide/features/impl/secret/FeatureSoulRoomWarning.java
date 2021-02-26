package kr.syeyoung.dungeonsguide.features.impl.secret;

import com.google.common.collect.Lists;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.GuiClickListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiPreRenderListener;
import kr.syeyoung.dungeonsguide.features.listener.TickListener;
import kr.syeyoung.dungeonsguide.features.listener.WorldRenderListener;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonParameterEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

public class FeatureSoulRoomWarning extends TextHUDFeature implements TickListener {

    public FeatureSoulRoomWarning() {
        super("Secret","Secret Soul Alert", "Alert if there is an fairy soul in the room", "secret.fairysoulwarn", true, getFontRenderer().getStringWidth("There is a fairy soul in this room!"), getFontRenderer().FONT_HEIGHT);
        getStyles().add(new TextStyle("warning", new AColor(0xFF, 0x69,0x17,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();
    @Override
    public boolean isHUDViewable() {
        return warning > System.currentTimeMillis();
    }

    @Override
    public List<String> getUsedTextStyle() {
        return Collections.singletonList("warning");
    }

    private UUID lastRoomUID = UUID.randomUUID();
    private long warning = 0;

    private static final List<StyledText> text = new ArrayList<StyledText>();
    static {
        text.add(new StyledText("There is a fairy soul in this room!", "warning"));
    }

    @Override
    public List<StyledText> getText() {
        return text;
    }


    @Override
    public void onTick() {
        if (!skyblockStatus.isOnDungeon()) return;
        if (skyblockStatus.getContext() == null || !skyblockStatus.getContext().getMapProcessor().isInitialized()) return;
        DungeonContext context = skyblockStatus.getContext();

        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);
        if (dungeonRoom == null) return;
        if (!(dungeonRoom.getRoomProcessor() instanceof GeneralRoomProcessor)) return;

        if (!dungeonRoom.getDungeonRoomInfo().getUuid().equals(lastRoomUID)) {
            for (DungeonMechanic value : dungeonRoom.getDungeonRoomInfo().getMechanics().values()) {
                if (value instanceof DungeonFairySoul)
                    warning = System.currentTimeMillis() + 2500;
            }
            lastRoomUID = dungeonRoom.getDungeonRoomInfo().getUuid();
        }
    }
}
