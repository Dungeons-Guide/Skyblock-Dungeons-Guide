package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlay;
import kr.syeyoung.dungeonsguide.mod.gui.CustomGuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.paralleluniverse.tablist.UTabListEntry;
import kr.syeyoung.modapi.world.UMapData;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.*;

public class MapOverlayPlayerClickable implements MapOverlay {
    private UTabListEntry entry;
    private String name;
    private MapConfiguration.PlayerHeadSettings settings;
    private WarpTarget target;
    private String clazz;

    public MapOverlayPlayerClickable(UTabListEntry entry, MapConfiguration.PlayerHeadSettings headSettings, WarpTarget target) {
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
        UEntityPlayer entityplayer = ModAPI.getAPI().getWorld().getUPlayerEntityByName(name);

        Vector2d pt2 = null;
        double yaw2 = 0;

        if (entityplayer != null && (!entityplayer.isInvisible() || entityplayer.equals(ModAPI.getAPI().getPlayer()))) {
            // getting location from player entity
            Vector3D playerPos = entityplayer.getPositionEyes(partialTicks);
            yaw2 = entityplayer.getPrevRotationYawHead() + (entityplayer.getRotationYawHead() - entityplayer.getPrevRotationYawHead()) * partialTicks;
            if(DungeonsGuide.getDungeonsGuide().verbose) System.out.println("Got player location from entity");
            return new Vector3d(playerPos.x, playerPos.z, yaw2);
        } else {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context == null) return new Vector3d(0,0,0);
            // getting player location from map
            String iconName = context.getMapPlayerMarkerProcessor().getMapIconToPlayerMap().get(name);
            if (iconName != null) {
                UMapData.MapMarker vec = context.getScaffoldParser().getLatestMapData().getMarkers().get(iconName);
                if (vec != null) {
                    VectorI3D worldPt = context.getScaffoldParser().getDungeonMapLayout().mapPointToWorldPoint(new Point(vec.getX() / 2 + 64, vec.getY()/2 + 64));
                    return new Vector3d(worldPt.getX(), worldPt.getZ(), vec.getRotation() * 360 / 16.0f);
                }
            }
        }
        return new Vector3d(0,0,0);

    }

    @Override
    public double getX(float partialTicks) {
        return getLocation(partialTicks).getX();
    }

    @Override
    public double getZ(float partialTicks) {
        return getLocation(partialTicks).getY();
    }

    @Override
    public int priority() {
        return 0;
    }

    private final ResourceIdentifier resourceLocation = new ResourceIdentifier("dungeonsguide:map/maptexture.png");

    @Override
    public void doRender(RenderingContext context, float rotation, float partialTicks, double scale, double relMouseX, double relMouseY) {
        Vector3d vec = getLocation(partialTicks);
        double yaw = vec.getZ();
        if (vec.getX() == 0 && vec.getZ() == 0) return;

        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.NONE) return;
        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.ARROW) {
//            GlStateManager.enableTexture2D();
//            Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);

            context.ctx().rotate((float) yaw, 0, 0, 1);
            context.ctx().scale(settings.getIconSize(), settings.getIconSize(), 0);
            context.drawScaledCustomSizeModalRect(resourceLocation, -4, -4, 128 - 16,
                    name.equals(ModAPI.getAPI().getPlayer().getName()) ? 128 - 16 : 128 - 0, 16, -16, 8, 8, 128, 128);
        } else {
            boolean flag1 = settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.HEAD_FLIP;
//            GlStateManager.enableTexture2D();
//            Minecraft.getMinecraft().getTextureManager().bindTexture(
//                    new ResourceLocation(entry.getLocationSkin().toString())
//            );
            int l2 = 8 + (flag1 ? 8 : 0);
            int i3 = 8 * (flag1 ? -1 : 1);


            context.ctx().rotate((float) yaw, 0, 0, 1);
            context.ctx().scale(settings.getIconSize(), settings.getIconSize(), 0);

            // cutting out the player head out of the skin texture
            if (relMouseX > -4 * settings.getIconSize() && relMouseX < 4 * settings.getIconSize() && relMouseY > -4 * settings.getIconSize() && relMouseY < 4 * settings.getIconSize()) {
                context.drawRect(-5, -5, 5, 5, 0xFF00FF00);
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
                context.drawRect(-5, -5, 5, 5, color);
            }
//            GlStateManager.color(1,1,1,1);

            // backside of head
            context.ctx().pushMatrix();
            context.ctx().scale(9.0 / 8, 9.0 / 8.0, 1.0);
            context.drawScaledCustomSizeModalRect( new ResourceIdentifier(entry.getLocationSkin().toString()), -4, -4, 56.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            context.ctx().popMatrix();
            context.drawScaledCustomSizeModalRect(new ResourceIdentifier(entry.getLocationSkin().toString()), -4, -4, 8.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            context.ctx().scale(9.0 / 8, 9.0 / 8.0, 1.0);
            context.drawScaledCustomSizeModalRect(new ResourceIdentifier(entry.getLocationSkin().toString()), -4, -4, 40.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);

        }

    }

    @Override
    public boolean onClick(double relMouseX, double relMouseY, DomElement domElement) {
        if (target == null) return false;
        if (relMouseX < -4 * settings.getIconSize() || relMouseX > 4 * settings.getIconSize() || relMouseY < -4 * settings.getIconSize() || relMouseY > 4 * settings.getIconSize()) return false;
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        CustomGuiScreenAdapterChestOverride.getAdapter(domElement).emulateClick(this.target.getSlotId(), 0, 0);
        return true;
    }
}
