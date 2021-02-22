package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.DungeonActionManager;
import kr.syeyoung.dungeonsguide.dungeon.actions.ActionComplete;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecret;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.awt.*;
import java.util.*;

public class GeneralRoomProcessor implements RoomProcessor {

    @Getter
    @Setter
    private DungeonRoom dungeonRoom;
    public GeneralRoomProcessor(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }
    private boolean ticked = false;

    @Override
    public void tick() {
        if (!ticked && FeatureRegistry.SECRET_AUTO_START.isEnabled())
            searchForNextTarget();

        ticked = true;
        if (path != null) {
            path.onTick();
            if (FeatureRegistry.SECRET_AUTO_BROWSE_NEXT.isEnabled() && path.getCurrentAction() instanceof ActionComplete) {
                if (!path.getState().equals("found")) return;
                if (!(dungeonRoom.getDungeonRoomInfo().getMechanics().get(path.getMechanic()) instanceof DungeonSecret)) return;
                searchForNextTarget();
            }
        }
    }
    private Set<String> visited = new HashSet<String>();

    public void searchForNextTarget() {
        BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();

        double lowestCost = 99999999999999.0;
        Map.Entry<String, DungeonMechanic> lowestWeightMechanic = null;
        for (Map.Entry<String, DungeonMechanic> mech: dungeonRoom.getDungeonRoomInfo().getMechanics().entrySet()) {
            if (!(mech.getValue() instanceof DungeonSecret)) continue;
            if (visited.contains(mech.getKey())) continue;
            if (((DungeonSecret) mech.getValue()).getSecretStatus(getDungeonRoom()) != DungeonSecret.SecretStatus.FOUND) {
                double cost = 0;
                if (((DungeonSecret) mech.getValue()).getSecretType() == DungeonSecret.SecretType.BAT &&
                        ((DungeonSecret) mech.getValue()).getPreRequisite().size() == 0) {
                    cost += -100000000;
                }
                if (mech.getValue().getRepresentingPoint() == null) continue;
                BlockPos blockpos = mech.getValue().getRepresentingPoint().getBlockPos(getDungeonRoom());

                cost += blockpos.distanceSq(pos);
                cost += ((DungeonSecret) mech.getValue()).getPreRequisite().size() * 100;

                if (cost < lowestCost) {
                    lowestCost = cost;
                    lowestWeightMechanic = mech;
                }
            }
        }
        if (lowestWeightMechanic != null) {
            visited.add(lowestWeightMechanic.getKey());
            pathfind(lowestWeightMechanic.getKey(), "found");
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (path != null) path.onRenderScreen(partialTicks);

        if (FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && FeatureRegistry.DEBUG.isEnabled()) {
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

            Entity en = Minecraft.getMinecraft().objectMouseOver.entityHit;
            if (en == null) return;

            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            if (DungeonActionManager.getSpawnLocation().containsKey(en.getEntityId())) {
                fr.drawString("Spawned at " + DungeonActionManager.getSpawnLocation().get(en.getEntityId()), sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        if (FeatureRegistry.DEBUG.isEnabled() && (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit)) {
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getDungeonRoomInfo().getMechanics().entrySet()) {
                if (value.getValue() == null) continue;;
                value.getValue().highlight(new Color(0,255,255,50), value.getKey(), dungeonRoom, partialTicks);
            }
        }
        if (path != null) path.onRenderWorld(partialTicks);
    }

    @Override
    public void chatReceived(IChatComponent chat) {

    }

    private int stack = 0;
    private long secrets2 = 0;
    @Override
    public void actionbarReceived(IChatComponent chat) {
        if (!e.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) return;
        if (dungeonRoom.getTotalSecrets() == -1) {
            e.sendDebugChat(new ChatComponentText(chat.getFormattedText().replace('§', '&') + " - received"));
        }
        if (!chat.getFormattedText().contains("/")) return;
        BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();
        DungeonContext context = e.getDungeonsGuide().getSkyblockStatus().getContext();
        Point pt1 = context.getMapProcessor().worldPointToRoomPoint(pos.add(2, 0, 2));
        Point pt2 = context.getMapProcessor().worldPointToRoomPoint(pos.add(-2, 0, -2));
        if (!pt1.equals(pt2)) {
            stack = 0;
            secrets2 = -1;
            return;
        }
        BlockPos pos2 = dungeonRoom.getMin().add(5, 0, 5);

        String text = chat.getFormattedText();
        int secretsIndex = text.indexOf("Secrets");
        int secrets = 0;
        if (secretsIndex != -1) {
            int theindex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theindex = i;
                }
            }
            String it = text.substring(theindex + 2, secretsIndex - 1);
     
            secrets = Integer.parseInt(it.split("/")[1]);
        }

        if (secrets2 == secrets) stack++;
        else {
            stack = 0;
            secrets2 = secrets;
        }

        if (stack == 4 && dungeonRoom.getTotalSecrets() != secrets) {
            dungeonRoom.setTotalSecrets(secrets);
            if (FeatureRegistry.DUNGEON_INTERMODCOMM.isEnabled())
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pchat $DG-Comm " + pos2.getX() + "/" + pos2.getZ() + " " + secrets);
        }
    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }

    @Getter
    private ActionRoute path;

    public void pathfind(String mechanic, String state) {
        path = new ActionRoute(getDungeonRoom(), mechanic, state);
    }
    public void cancel() {
        path = null;
    }

    @Override
    public void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {

    }

    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand &&
                updateEvent.entityLiving.getName() != null &&
                updateEvent.entityLiving.getName().contains("Mimic") &&
                !dungeonRoom.getContext().isGotMimic()) {
            dungeonRoom.getContext().setGotMimic(true);
//            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc $DG-Mimic");
        }
    }

    @Override
    public void onKeyPress(InputEvent.KeyInputEvent keyInputEvent) {
        if (FeatureRegistry.SECRET_NEXT_KEY.isEnabled() && Keybinds.nextSecret.isKeyDown()) {
            searchForNextTarget();
        }
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {
        if (path != null) path.onLivingInteract(event);
    }

    private boolean last = false;
    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        if (path != null) path.onPlayerInteract(event);

        if (event.entityPlayer.getHeldItem() != null &&
            event.entityPlayer.getHeldItem().getItem() == Items.stick &&
                FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() &&
                FeatureRegistry.DEBUG.isEnabled()) {
            EditingContext ec = EditingContext.getEditingContext();
            if (ec == null) return;
            if (!(ec.getCurrent() instanceof GuiDungeonAddSet)) return;
            GuiDungeonAddSet gdas = (GuiDungeonAddSet) ec.getCurrent();
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                if (last)
                    gdas.getEnd().setPosInWorld(getDungeonRoom(), event.pos);
                else
                    gdas.getStart().setPosInWorld(getDungeonRoom(), event.pos);

                last = !last;
            }
        }
    }

    @Override
    public void onEntityDeath(LivingDeathEvent deathEvent) {
        if (path != null) path.onLivingDeath(deathEvent);
        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getRoom() == getDungeonRoom()) {
            if (deathEvent.entity instanceof EntityBat) {
                for (GuiScreen screen : EditingContext.getEditingContext().getGuiStack()) {
                    if (screen instanceof GuiDungeonRoomEdit) {
                        DungeonSecret secret = new DungeonSecret();
                        secret.setSecretType(DungeonSecret.SecretType.BAT);
                        secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                                DungeonActionManager.getSpawnLocation().get(deathEvent.entity.getEntityId())
                        ));
                        ((GuiDungeonRoomEdit) screen).getSep().createNewMechanic("BAT-"+UUID.randomUUID().toString(),
                                secret);
                        return;
                    }
                }
                if (EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit) {
                    DungeonSecret secret = new DungeonSecret();
                    secret.setSecretType(DungeonSecret.SecretType.BAT);
                    secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                            DungeonActionManager.getSpawnLocation().get(deathEvent.entity.getEntityId())
                    ));
                    ((GuiDungeonRoomEdit) EditingContext.getEditingContext().getCurrent()).getSep().createNewMechanic("BAT-"+UUID.randomUUID().toString(),
                            secret);
                }
            }
        }
    }

    public static class Generator implements RoomProcessorGenerator<GeneralRoomProcessor> {
        @Override
        public GeneralRoomProcessor createNew(DungeonRoom dungeonRoom) {
            GeneralRoomProcessor defaultRoomProcessor = new GeneralRoomProcessor(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
