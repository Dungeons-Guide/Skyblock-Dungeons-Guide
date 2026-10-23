package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlay;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

import org.joml.Vector2d;
import org.joml.Vector3d;
import java.awt.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class MapOverlayPlayerClickable implements MapOverlay {
    private TabListEntry entry;
    private String name;
    private MapConfiguration.PlayerHeadSettings settings;
    private WarpTarget target;
    private String clazz;

    public MapOverlayPlayerClickable(TabListEntry entry, MapConfiguration.PlayerHeadSettings headSettings, WarpTarget target) {
        this.name = TabListUtil.getPlayerNameWithChecks(entry);
        this.entry = entry;
        this.settings = headSettings;
        this.target = target;


        if (entry == null) {
            this.clazz = "";
        } else {
            int idx = entry.getEffectiveName().indexOf("§r§f(");
            String clazzThing = entry.getEffectiveName().substring(idx);

            this.clazz = TextUtils.stripColor(clazzThing).substring(1);
        }
    }

    public Vector3d getLocation(float partialTicks) {
        Player entityplayer = Minecraft.getInstance().level.players().stream().filter(p -> p.getGameProfile().name().equals(name)).findFirst().orElse(null);

        Vector2d pt2 = null;
        double yaw2 = 0;

        if (entityplayer != null && (!entityplayer.isInvisible() || entityplayer == Minecraft.getInstance().player)) {
            // getting location from player entity
            Vec3 playerPos = entityplayer.getEyePosition(partialTicks);
            yaw2 = entityplayer.yHeadRotO + (entityplayer.getYHeadRot() - entityplayer.yHeadRotO) * partialTicks;
            if(DungeonsGuide.getDungeonsGuide().verbose) System.out.println("Got player location from entity");
            return new Vector3d(playerPos.x, playerPos.z, yaw2);
        } else {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context == null) return new Vector3d(0,0,0);
            // getting player location from map
            String iconName = context.getMapPlayerMarkerProcessor().getMapIconToPlayerMap().get(name);
            if (iconName != null) {
                MapDecoration vec = context.getScaffoldParser().getLatestMapData().decorations.get(iconName);
                if (vec != null) {
                    BlockPos worldPt = context.getScaffoldParser().getDungeonMapLayout().mapPointToWorldPoint(new Point(vec.x() / 2 + 64, vec.y()/2 + 64));
                    return new Vector3d(worldPt.getX(), worldPt.getZ(), vec.rot() * 360 / 16.0f);
                }
            }
        }
        return new Vector3d(0,0,0);

    }

    @Override
    public double getX(float partialTicks) {
        return getLocation(partialTicks).x;
    }

    @Override
    public double getZ(float partialTicks) {
        return getLocation(partialTicks).y;
    }

    @Override
    public int priority() {
        return 0;
    }

    private final Identifier resourceLocation = Identifier.parse("dungeonsguide:map/maptexture.png");

    @Override
    public void doRender(RenderingContext ctx, float rotation, float partialTicks, double scale, double relMouseX, double relMouseY) {
        Vector3d vec = getLocation(partialTicks);
        double yaw = vec.z;
        if (vec.x == 0 && vec.z == 0) return;

        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.NONE) return;
        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.ARROW) {
            ctx.pose().rotate((float) Math.toRadians(yaw));
            ctx.pose().scale((float) settings.getIconSize(), (float) settings.getIconSize());
            ctx.drawScaledCustomSizeModalRect(resourceLocation, -4, -4, 128 - 16,
                    name.equals(Minecraft.getInstance().player.getGameProfile().name()) ? 128 - 16 : 128 - 0, 16, -16, 8, 8, 128, 128);
        } else {
            boolean flag1 = settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.HEAD_FLIP;
            int l2 = 8 + (flag1 ? 8 : 0);
            int i3 = 8 * (flag1 ? -1 : 1);


            ctx.pose().rotate((float) Math.toRadians(yaw));
            ctx.pose().scale((float) settings.getIconSize(), (float) settings.getIconSize());

            // cutting out the player head out of the skin texture
            if (relMouseX > -4 * settings.getIconSize() && relMouseX < 4 * settings.getIconSize() && relMouseY > -4 * settings.getIconSize() && relMouseY < 4 * settings.getIconSize()) {
                ctx.drawRect(-5, -5, 5, 5, 0xFF00FF00);
            } else {
                int color = 0xFFFFFFFF;
                if (clazz.startsWith("Archer")) {
                    color = (0xFF5cae76); // green
                } else if (clazz.startsWith("Berserk")) {
                    color = (0xFFdb4d46); // red
                } else if (clazz.startsWith("Mage")) {
                    color = (0xFFba75e6); // purple
                } else if (clazz.startsWith("Healer")) {
                    color = (0xFFe4b64e); // yellow
                } else if (clazz.startsWith("Tank")) {
                    color = (0xFF8fd1c9); // blue
                } else if (clazz.startsWith("DEAD")) {
                    color = (0xFF333333); // black
                }
                ctx.drawRect(-5, -5, 5, 5, color);
            }

            // backside of head
            ctx.pose().pushMatrix();
            ctx.pose().scale(9.0f / 8, 9.0f / 8.0f);
            ctx.drawScaledCustomSizeModalRect(entry.getLocationSkin(), -4, -4, 56.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            ctx.pose().popMatrix();
            ctx.drawScaledCustomSizeModalRect(entry.getLocationSkin(), -4, -4, 8.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            ctx.pose().scale(9.0f / 8, 9.0f / 8.0f);
            ctx.drawScaledCustomSizeModalRect(entry.getLocationSkin(), -4, -4, 40.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);

        }

    }

    @Override
    public boolean onClick(double relMouseX, double relMouseY, DomElement domElement) {
        if (target == null) return false;
        if (relMouseX < -4 * settings.getIconSize() || relMouseX > 4 * settings.getIconSize() || relMouseY < -4 * settings.getIconSize() || relMouseY > 4 * settings.getIconSize()) return false;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(domElement).emulateClick(this.target.getSlotId(), 0, 0);
        return true;
    }
}
