package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.roomedit.GuiDungeonRoomEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;

public class CommandSaveData extends CommandBase {
    @Override
    public String getCommandName() {
        return "saverooms";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "saverooms";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        DungeonRoomInfoRegistry.saveAll(DungeonsGuide.getDungeonsGuide().getConfigDir());
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
