package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

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
        DungeonRoomInfoRegistry.saveAll(e.getDungeonsGuide().getConfigDir());
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
