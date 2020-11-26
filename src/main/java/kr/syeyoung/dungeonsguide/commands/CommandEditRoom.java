package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.roomedit.gui.GuiDungeonRoomEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;

public class CommandEditRoom extends CommandBase {
    @Override
    public String getCommandName() {
        return "editsession";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "editsession";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
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

        if (EditingContext.getEditingContext() != null) {
            sender.addChatMessage(new ChatComponentText("There is an editing session currently open."));
            return;
        }

        EditingContext.createEditingContext(dungeonRoom);
        openGuiReq = true;
    }

    private boolean openGuiReq = false;

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent tick){
        if ( openGuiReq &&tick.phase == TickEvent.Phase.END && tick.side == Side.CLIENT && tick.type == TickEvent.Type.CLIENT) {
            DungeonRoom dr = EditingContext.getEditingContext().getRoom();
            EditingContext.getEditingContext().openGui(new GuiDungeonRoomEdit(dr));
            openGuiReq = false;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
