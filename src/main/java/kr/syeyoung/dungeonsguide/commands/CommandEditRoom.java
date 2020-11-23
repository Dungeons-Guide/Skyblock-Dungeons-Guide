package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.utils.MapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;

public class CommandEditRoom extends CommandBase {
    @Override
    public String getCommandName() {
        return "editroom";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "editroom";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
        if (!skyblockStatus.isOnDungeon()) {
            sender.addChatMessage(new ChatComponentText("You're not in dungeons"));
            return;
        }

        if (skyblockStatus.getContext() == null) {
            sender.addChatMessage(new ChatComponentText("Dungeon Context is null"));
            return;
        }


        DungeonContext context = skyblockStatus.getContext();
        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
        Point roomPt = context.getMapProcessor().worldPointToRoomPoint(thePlayer.getPosition());
        DungeonRoom dungeonRoom = context.getRoomMapper().get(roomPt);

        if (dungeonRoom == null) {
            sender.addChatMessage(new ChatComponentText("Can't determine the dungeon room you're in"));
            return;
        }

        openit = new GuiDungeonRoomEdit(dungeonRoom);
    }

    GuiScreen openit;

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent tick){
        if ( openit != null &&tick.phase == TickEvent.Phase.END && tick.side == Side.CLIENT && tick.type == TickEvent.Type.CLIENT) {
            Minecraft.getMinecraft().displayGuiScreen(openit);
            openit = null;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
