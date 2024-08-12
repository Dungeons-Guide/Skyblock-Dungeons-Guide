package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.spiritleap;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.overlay.MapOverlay;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapterChestOverride;
import kr.syeyoung.dungeonsguide.mod.parallelUniverse.tab.TabListEntry;
import kr.syeyoung.dungeonsguide.mod.utils.TabListUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec4b;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.*;

public class MapOverlayPlayerClickable implements MapOverlay {
    private TabListEntry entry;
    private String name;
    private MapConfiguration.PlayerHeadSettings settings;
    private WarpTarget target;

    public MapOverlayPlayerClickable(TabListEntry entry, MapConfiguration.PlayerHeadSettings headSettings, WarpTarget target) {
        this.name = TabListUtil.getPlayerNameWithChecks(entry);
        this.entry = entry;
        this.settings = headSettings;
        this.target = target;
    }

    public Vector3d getLocation(float partialTicks) {
        EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);

        Vector2d pt2 = null;
        double yaw2 = 0;

        if (entityplayer != null && (!entityplayer.isInvisible() || entityplayer == Minecraft.getMinecraft().thePlayer)) {
            // getting location from player entity
            Vec3 playerPos = entityplayer.getPositionEyes(partialTicks);
            yaw2 = entityplayer.prevRotationYawHead + (entityplayer.rotationYawHead - entityplayer.prevRotationYawHead) * partialTicks;
            if(DungeonsGuide.getDungeonsGuide().verbose) System.out.println("Got player location from entity");
            return new Vector3d(playerPos.xCoord, playerPos.zCoord, yaw2);
        } else {
            DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (context == null) return new Vector3d(0,0,0);
            // getting player location from map
            String iconName = context.getMapPlayerMarkerProcessor().getMapIconToPlayerMap().get(name);
            if (iconName != null) {
                Vec4b vec = context.getScaffoldParser().getLatestMapData().mapDecorations.get(iconName);
                if (vec != null) {
                    BlockPos worldPt = context.getScaffoldParser().getDungeonMapLayout().mapPointToWorldPoint(new Point(vec.func_176112_b() / 2 + 64, vec.func_176113_c()/2 + 64));
                    return new Vector3d(worldPt.getX(), worldPt.getZ(), vec.func_176111_d() * 360 / 16.0f);
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

    private final ResourceLocation resourceLocation = new ResourceLocation("dungeonsguide:map/maptexture.png");

    @Override
    public void doRender(float rotation, float partialTicks, double scale, double relMouseX, double relMouseY) {
        Vector3d vec = getLocation(partialTicks);
        double yaw = vec.getZ();
        if (vec.getX() == 0 && vec.getZ() == 0) return;

        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.NONE) return;
        if (settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.ARROW) {
            GlStateManager.enableTexture2D();
            Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);

            GlStateManager.rotate((float) yaw, 0, 0, 1);
            GlStateManager.scale(settings.getIconSize(), settings.getIconSize(), 0);
            Gui.drawScaledCustomSizeModalRect(-4, -4, 128 - 16,
                    name.equals(Minecraft.getMinecraft().thePlayer.getName()) ? 128 - 16 : 128 - 0, 16, -16, 8, 8, 128, 128);
        } else {
            boolean flag1 = settings.getIconType() == MapConfiguration.PlayerHeadSettings.IconType.HEAD_FLIP;
            GlStateManager.enableTexture2D();
            Minecraft.getMinecraft().getTextureManager().bindTexture(
                    entry.getLocationSkin()
            );
            int l2 = 8 + (flag1 ? 8 : 0);
            int i3 = 8 * (flag1 ? -1 : 1);


            GlStateManager.rotate((float) yaw, 0, 0, 1);
            GlStateManager.scale(settings.getIconSize(), settings.getIconSize(), 0);

            // cutting out the player head out of the skin texture
            if (relMouseX > -4 * settings.getIconSize() && relMouseX < 4 * settings.getIconSize() && relMouseY > -4 * settings.getIconSize() && relMouseY < 4 * settings.getIconSize()) {
                Gui.drawRect(-5, -5, 5, 5, 0xFF00FF00);
            }
            GlStateManager.color(1,1,1,1);

            // backside of head
            GlStateManager.pushMatrix();
            GlStateManager.scale(9.0 / 8, 9.0 / 8.0, 1.0);
            Gui.drawScaledCustomSizeModalRect(-4, -4, 56.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            GlStateManager.popMatrix();
            Gui.drawScaledCustomSizeModalRect(-4, -4, 8.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);
            GlStateManager.scale(9.0 / 8, 9.0 / 8.0, 1.0);
            Gui.drawScaledCustomSizeModalRect(-4, -4, 40.0F, l2, 8, i3, 8, 8, 64.0F, 64.0F);

        }

    }

    @Override
    public boolean onClick(double relMouseX, double relMouseY, DomElement domElement) {
        if (target == null) return false;
        if (relMouseX < -4 * settings.getIconSize() || relMouseX > 4 * settings.getIconSize() || relMouseY < -4 * settings.getIconSize() || relMouseY > 4 * settings.getIconSize()) return false;
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        GuiScreenAdapterChestOverride.getAdapter(domElement).emulateClick(this.target.getSlotId(), 0, 0);
        return true;
    }
}
